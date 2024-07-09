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

package in.codebuckets.acme.manager;

import in.codebuckets.acme.manager.acme.AcmeService;
import in.codebuckets.acme.manager.jpa.Account;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.shredzone.acme4j.Login;
import org.shredzone.acme4j.Session;
import org.shredzone.acme4j.exception.AcmeException;
import org.shredzone.acme4j.util.KeyPairUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.security.KeyPair;

import static in.codebuckets.acme.manager.utils.CryptoUtils.sha256;

@Service
public class AccountService {

    private static final Logger logger = LogManager.getLogger();

    /**
     * Create a new account with the given email address
     *
     * @param session      the ACME session
     * @param emailAddress the email address to use for the account
     * @return the created account
     * @throws ServiceException if an error occurs
     */
    public Account createAccount(Session session, String emailAddress) {
        try {
            KeyPair accountKeyPair = AcmeService.generateKey(true);

            Login login = AcmeService.findOrRegisterAccount(session, accountKeyPair, emailAddress);

            StringWriter stringWriter = new StringWriter();
            KeyPairUtils.writeKeyPair(accountKeyPair, stringWriter);
            String privateKeyAsString = stringWriter.toString();

            return Account.builder()
                    .emailAddress(emailAddress)
                    .privateKey(privateKeyAsString)
                    .accountId(sha256(login.getAccountLocation().toString()))
                    .serverUri(session.getServerUri().toString())
                    .accountLocation(login.getAccountLocation().toString())
                    .build();
        } catch (AcmeException e) {
            logger.error("Error creating account", e);
            throw new ServiceException("Error creating account");
        } catch (IOException e) {
            logger.error("Error while writing key pair", e);
            throw new ServiceException("Error creating account");
        }
    }

    /**
     * Login to an existing account
     *
     * @param session the ACME session
     * @param account the account to log in to
     * @return {@link Login} instance
     * @throws ServiceException if an error occurs
     */
    public Login loginAccount(Session session, Account account) {
        try {
            KeyPair accountKeyPair = KeyPairUtils.readKeyPair(new StringReader(account.privateKey()));

            Login login = AcmeService.findOrRegisterAccount(session, accountKeyPair, account.emailAddress());
            String accountId = sha256(login.getAccountLocation().toString());

            // Check if the account ID matches
            if (!accountId.equals(account.accountId())) {
                throw new IllegalArgumentException("Account ID does not match");
            }

            return login;
        } catch (AcmeException e) {
            logger.error("Error logging into account", e);
            throw new ServiceException("Error logging into account");
        } catch (IOException e) {
            logger.error("Error while reading key pair", e);
            throw new ServiceException("Error logging into account");
        }
    }
}
