/*
 *    Copyright 2024, Codebucket Solutions Private Limited
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package in.codebuckets.acme.manager.acme;

import in.codebuckets.acme.manager.ServiceException;
import in.codebuckets.acme.manager.dto.ChallengeMessage;
import in.codebuckets.acme.manager.dto.ExecuteOrderResult;
import in.codebuckets.acme.manager.dto.PlaceOrder;
import in.codebuckets.acme.manager.jpa.Certificate;
import in.codebuckets.acme.manager.jpa.CertificateRepository;
import in.codebuckets.acme.manager.jpa.ValidationRequest;
import in.codebuckets.acme.manager.jpa.ValidationRequestRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.shredzone.acme4j.Account;
import org.shredzone.acme4j.AccountBuilder;
import org.shredzone.acme4j.Authorization;
import org.shredzone.acme4j.Login;
import org.shredzone.acme4j.Order;
import org.shredzone.acme4j.Problem;
import org.shredzone.acme4j.Session;
import org.shredzone.acme4j.Status;
import org.shredzone.acme4j.challenge.Challenge;
import org.shredzone.acme4j.challenge.Dns01Challenge;
import org.shredzone.acme4j.challenge.Http01Challenge;
import org.shredzone.acme4j.exception.AcmeException;
import org.shredzone.acme4j.util.KeyPairUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.security.KeyPair;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import static in.codebuckets.acme.manager.utils.CryptoUtils.sha256;
import static in.codebuckets.acme.manager.acme.ChallengeType.DNS;
import static in.codebuckets.acme.manager.acme.ChallengeType.HTTP;
import static org.shredzone.acme4j.Status.VALID;
import static org.shredzone.acme4j.challenge.Dns01Challenge.toRRName;

@Service
public class AcmeService {

    private static final Logger logger = LogManager.getLogger();

    // Maximum attempts of status polling until VALID/INVALID is expected
    private static final int MAX_ATTEMPTS = 10;

    private final ValidationRequestRepository validationRequestRepository;
    private final CertificateRepository certificateRepository;

    public AcmeService(ValidationRequestRepository validationRequestRepository, CertificateRepository certificateRepository) {
        this.validationRequestRepository = validationRequestRepository;
        this.certificateRepository = certificateRepository;
    }

    /**
     * Find or register an account at the ACME server.
     *
     * @param session      {@link Session} to bind the account to
     * @param accountKey   {@link KeyPair} to use
     * @param accountEmail E-Mail address for the account
     * @return {@link Login} that was found or created
     * @throws AcmeException if the account could not be found or created
     */
    public static Login findOrRegisterAccount(Session session, KeyPair accountKey, String accountEmail) throws AcmeException {
        return new AccountBuilder()
                .agreeToTermsOfService()
                .useKeyPair(accountKey)
                .addEmail(accountEmail)
                .createLogin(session);
    }

    /**
     * Place an order for a certificate
     *
     * @param account    {@link Account} to associate this order with
     * @param placeOrder {@link PlaceOrder} to use
     * @return {@link Certificate} instance that was created
     * @throws ServiceException if the order could not be placed
     * @throws AcmeException    if the order could not be placed due to ACME provider error
     */
    public Certificate placeOrder(Account account, PlaceOrder placeOrder) throws AcmeException {
        // Order the certificate
        Order order = account.newOrder()
                .domains(placeOrder.domains())
                .create();

        String orderId = sha256(order.getLocation().toString());

        Optional<Certificate> optionalCertificate = certificateRepository.findByOrderId(orderId);
        Certificate certificate;

        if (optionalCertificate.isPresent()) {
            logger.info("Certificate already exists for order ID: {}", orderId);
            certificate = optionalCertificate.get();
        } else {
            certificate = Certificate.builder()
                    .orderId(orderId)
                    .orderUrl(order.getLocation().toString())
                    .domains(placeOrder.domains())
                    .saveKeyPair(placeOrder.saveKeyPair())
                    .acmeProvider(placeOrder.acmeProvider())
                    .build();

            certificateRepository.save(certificate);
        }

        List<ValidationRequest> validationRequests = new ArrayList<>();

        // Select challenges to solve
        for (Authorization authorization : order.getAuthorizations()) {
            ChallengeMessage challengeMessage = selectChallenge(authorization, placeOrder.challengeType(), placeOrder.forceChallengeType());

            // Create a new certificate request
            ValidationRequest validationRequest = ValidationRequest.builder()
                    .domain(authorization.getIdentifier().getDomain())
                    .status(order.getStatus())
                    .expiresAt(order.getExpires().orElse(null))
                    .orderUrl(order.getLocation().toString())
                    .orderId(orderId)
                    .challengeToken(challengeMessage.challengeToken())
                    .challengeAuthorization(challengeMessage.challengeAuthorization())
                    .challengeType(challengeMessage.challengeType().getName())
                    .certificate(certificate)
                    .build();

            validationRequest = validationRequestRepository.save(validationRequest);
            validationRequests.add(validationRequest);

            logger.info("Created validation request for domain: {} with Order Id: {}", validationRequest.domain(), orderId);
        }

        return certificate;
    }

    /**
     * Execute the order for the certificate and validate the challenges
     *
     * @param login {@link Login} to use
     * @return Map of domain to {@link ExecuteOrderResult}
     * @throws AcmeException if the order could not be executed
     * @throws IOException   if an I/O error occurs
     */
    public Map<String, ExecuteOrderResult> executeOrder(Login login, Certificate certificate) throws AcmeException, IOException {
        KeyPair domainKeyPair = generateKey();

        Map<String, ExecuteOrderResult> results = new HashMap<>();
        Order order = login.bindOrder(new URL(certificate.orderId()));

        // Authorize the order
        for (ValidationRequest validationRequest : certificate.validationRequests()) {
            String domain = validationRequest.domain();

            // Get the authorization for the Order
            for (Authorization authorization : order.getAuthorizations()) {

                // Find all challenges for the authorization
                for (Challenge challenge : authorization.getChallenges()) {

                    if (domain.equals(authorization.getIdentifier().getDomain()) && challenge.getType().equals(validationRequest.challengeType())) {
                        logger.info("Triggering challenge for domain: {} with challenge type: {}", domain, challenge.getType());

                        // If the challenge is already verified, there's no need to execute it again.
                        if (challenge.getStatus() == VALID) {
                            logger.info("Challenge already verified for domain: {} with type: {}", domain, challenge.getType());
                            continue;
                        }

                        try {
                            challenge.trigger();

                            // Poll for the challenge to complete.
                            Status status = waitForCompletion(challenge::getStatus, challenge::fetch);
                            if (status != VALID) {
                                logger.error("Challenge has failed, reason: {}", challenge.getError()
                                        .map(Problem::toString)
                                        .orElse("unknown"));

                                throw new AcmeException("Challenge failed... Giving up.");
                            }

                            results.put(domain, new ExecuteOrderResult(true, null));

                            logger.info("Triggered challenge for domain: {}", domain);
                        } catch (AcmeException acmeException) {
                            logger.error("Failed to trigger challenge for domain: {}", domain, acmeException);
                            results.put(domain, new ExecuteOrderResult(false, acmeException.getMessage()));
                            break;
                        }
                    }
                }
            }
        }

        // Order the certificate
        order.execute(domainKeyPair);

        // Wait for the order to complete
        Status status = waitForCompletion(order::getStatus, order::fetch);

        // Update Status for all certificate requests
        certificate.validationRequests().forEach(validationRequest -> {
            validationRequest.status(status);
            validationRequestRepository.save(validationRequest);
        });

        logger.info("Order {} completed with status: {}", certificate.orderId(), status);

        // If the order is valid, save the key pair
        if (status == VALID && certificate.saveKeyPair()) {
            logger.info("Saving private key for order: {}", certificate.orderId());

            if (!new File("certs/" + certificate.orderId()).mkdirs()) {
                throw new IOException("Failed to create directory for certificate request: " + certificate.orderId());
            }

            try (FileWriter fileWriter = new FileWriter("certs/" + certificate.orderId() + "/private.key")) {
                KeyPairUtils.writeKeyPair(domainKeyPair, fileWriter);
            }
        }

        return results;
    }

    /**
     * Select a challenge for the given authorization
     *
     * @param authorization      {@link Authorization} to find the challenge for
     * @param challengeType      {@link ChallengeType} to use
     * @param forceChallengeType {@code true} if the challenge type should be forced, {@code false} to select the first supported challenge type
     * @return {@link ChallengeMessage} that was selected
     * @throws ServiceException if the challenge could not be selected
     */
    private static ChallengeMessage selectChallenge(Authorization authorization, ChallengeType challengeType, boolean forceChallengeType) {
        logger.info("Authorization for domain {} with challenge type {}", authorization.getIdentifier().getDomain(), challengeType);

        // If a challenge type is forced, check if the authorization supports it
        // and throw an exception if it doesn't.
        //
        // If no challenge type is forced, select the first supported challenge type.
        // If no supported challenge type is found, throw an exception.
        if (forceChallengeType) {
            if (challengeType == HTTP && authorization.findChallenge(Http01Challenge.class).isEmpty()) {
                throw new ServiceException("HTTP challenge not available for domain " + authorization.getIdentifier().getDomain());
            } else if (challengeType == DNS && authorization.findChallenge(Dns01Challenge.class).isEmpty()) {
                throw new ServiceException("DNS challenge not available for domain " + authorization.getIdentifier().getDomain());
            }
        } else {
            if (authorization.findChallenge(Http01Challenge.class).isPresent()) {
                challengeType = HTTP;
            } else if (authorization.findChallenge(Dns01Challenge.class).isPresent()) {
                challengeType = DNS;
            } else {
                throw new ServiceException("Found no supported challenge for domain " + authorization.getIdentifier().getDomain());
            }
        }

        if (challengeType == HTTP) {
            // "http://" + auth.getIdentifier().getDomain() + "/.well-known/acme-challenge/" + challenge.getToken();
            Http01Challenge challenge = authorization.findChallenge(Http01Challenge.class).get();

            return new ChallengeMessage(HTTP, authorization.getIdentifier().getDomain(), authorization.getStatus(), challenge.getToken(), challenge.getAuthorization());
        } else if (challengeType == DNS) {
            Dns01Challenge challenge = authorization.findChallenge(Dns01Challenge.class).get();

            System.out.println("Please create a DNS TXT record for " + toRRName(authorization.getIdentifier()) + " with the following value:");
            System.out.println(challenge.getDigest());

            return new ChallengeMessage(DNS, authorization.getIdentifier().getDomain(), authorization.getStatus(), toRRName(authorization.getIdentifier()), challenge.getDigest());
        } else {
            throw new ServiceException("Unexpected challenge type: " + challengeType);
        }
    }

    private static Status waitForCompletion(Supplier<Status> statusSupplier, UpdateMethod statusUpdater) throws AcmeException {
        // A set of terminating status values
        Set<Status> acceptableStatus = EnumSet.of(VALID, Status.INVALID);

        // Limit the number of checks, to avoid endless loops
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            logger.info("Checking current status, attempt {} of {}", attempt, MAX_ATTEMPTS);

            Instant now = Instant.now();

            // Update the status property
            Instant retryAfter = statusUpdater.updateAndGetRetryAfter().orElse(now.plusSeconds(3L));

            // Check the status
            Status currentStatus = statusSupplier.get();

            if (acceptableStatus.contains(currentStatus)) {
                // Reached VALID or INVALID, we're done here
                return currentStatus;
            }

            // Wait before checking again
            try {
                Thread.sleep(now.until(retryAfter, ChronoUnit.MILLIS));
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new AcmeException("interrupted");
            }
        }

        throw new AcmeException("Too many update attempts, status did not change");
    }

    /**
     * Functional interface that refers to a resource update method that returns an
     * optional retry-after instant and is able to throw an {@link AcmeException}.
     */
    @FunctionalInterface
    private interface UpdateMethod {

        Optional<Instant> updateAndGetRetryAfter() throws AcmeException;
    }

    /**
     * Generate a new key pair for the using the prime256v1 curve.
     *
     * @return {@link KeyPair} that was generated
     */
    public static KeyPair generateKey() {
        return generateKey(false);
    }

    /**
     * Generate a new key pair for the using the prime256v1 or secp384r1 curve.
     *
     * @param secp384r1 {@code true} if the secp384r1 curve should be used, {@code false} for prime256v1
     * @return {@link KeyPair} that was generated
     */
    public static KeyPair generateKey(boolean secp384r1) {
        if (secp384r1) {
            return KeyPairUtils.createECKeyPair("secp384r1");
        } else {
            return KeyPairUtils.createECKeyPair("prime256v1");
        }
    }
}
