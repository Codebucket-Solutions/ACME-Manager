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

package in.codebuckets.acme.manager.controllers;

import in.codebuckets.acme.manager.AccountService;
import in.codebuckets.acme.manager.JwtService;
import in.codebuckets.acme.manager.acme.AcmeService;
import in.codebuckets.acme.manager.dto.PlaceOrder;
import in.codebuckets.acme.manager.jpa.AccountRepository;
import in.codebuckets.acme.manager.jpa.Certificate;
import in.codebuckets.acme.manager.jpa.CertificateRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.shredzone.acme4j.Account;
import org.shredzone.acme4j.Login;
import org.shredzone.acme4j.exception.AcmeException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static in.codebuckets.acme.manager.Responses.badRequest;
import static in.codebuckets.acme.manager.Responses.notFound;
import static in.codebuckets.acme.manager.Responses.ok;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/v1/acme")
public class AcmeController {

    private static final Logger logger = LogManager.getLogger();

    private final AcmeService acmeService;
    private final JwtService jwtService;
    private final AccountRepository accountRepository;
    private final AccountService accountService;
    private final CertificateRepository certificateRepository;

    public AcmeController(AcmeService acmeService, JwtService jwtService, AccountRepository accountRepository, AccountService accountService, CertificateRepository certificateRepository) {
        this.acmeService = acmeService;
        this.jwtService = jwtService;
        this.accountRepository = accountRepository;
        this.accountService = accountService;
        this.certificateRepository = certificateRepository;
    }

    @PostMapping(value = "/place-order", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public CompletableFuture<ResponseEntity<String>> placeOrder(@RequestHeader(AUTHORIZATION) String auth, @RequestBody PlaceOrder placeOrder) {
        return supplyAsync(() -> {
            Long accountId = jwtService.accountId(auth);

            Optional<in.codebuckets.acme.manager.jpa.Account> optionalAccount = accountRepository.findById(accountId);
            if (optionalAccount.isEmpty()) {
                return notFound("Account not found");
            }

            Account account = accountService.loginAccount(placeOrder.acmeProvider().session(), optionalAccount.get()).getAccount();

            try {
                Certificate certificate = acmeService.placeOrder(account, placeOrder);
                return ok(certificate);
            } catch (AcmeException e) {
                logger.error("ACME error while placing order", e);
                return badRequest("ACME error while placing order: " + e.getMessage());
            }
        });
    }

    @PatchMapping(value = "execute-order", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public CompletableFuture<ResponseEntity<String>> executeOrder(@RequestParam String orderId) {
        return supplyAsync(() -> {
            Optional<Certificate> optionalCertificate = certificateRepository.findByOrderId(orderId);
            if (optionalCertificate.isEmpty()) {
                return notFound("Certificate not found");
            }

            Certificate certificate = optionalCertificate.get();

            Login login = accountService.loginAccount(certificate.acmeProvider().session(), certificate.account());

            try {
                acmeService.executeOrder(login, certificate);
                return ok(certificate);
            } catch (AcmeException e) {
                logger.error("ACME error while placing order", e);
                return badRequest("ACME error while placing order: " + e.getMessage());
            } catch (IOException e) {
                logger.error("Error while executing order for certificate with Order ID: {}", orderId, e);
                return badRequest("Error while executing order for certificate with Order ID: " + orderId);
            }
        });
    }
}
