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

package in.codebuckets.acmemanager.server.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.codebuckets.acmemanager.server.acme.AcmeProvider;
import in.codebuckets.acmemanager.server.dto.AccountOperation;
import in.codebuckets.acmemanager.server.dto.LoginResponse;
import in.codebuckets.acmemanager.server.jpa.AccountRepository;
import in.codebuckets.acmemanager.server.services.AccountService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.Mockito;
import org.shredzone.acme4j.Login;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockReset;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;

import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AccountControllerTest extends AbstractTest {

    private static final Logger logger = LogManager.getLogger();

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean(reset = MockReset.NONE)
    private AccountService accountService;

    protected AccountControllerTest(@LocalServerPort int serverPort) {
        super(serverPort);
    }

    @BeforeAll
    void setUp() throws MalformedURLException {
        accountRepository.deleteAll();

        // Mock methods to return a Login object (to prevent calling the ACME server)
        Login login = Mockito.mock(Login.class);
        when(login.getAccountLocation()).thenReturn(new URL("https://lol.com"));

        when(accountService.findOrRegisterAccount(any(), any(), any())).thenReturn(login);
        when(accountService.loginAccount(any(), any())).thenReturn(login);
        when(accountService.createAccount(any(), any(), any())).thenCallRealMethod();
    }

    @AfterAll
    void tearDown() {
        accountRepository.deleteAll();
    }

    @Order(1)
    @Test
    void createAccount() {
        AccountOperation accountOperation = new AccountOperation(AcmeProvider.LETS_ENCRYPT_STAGING, "Test", "lol@example.com");
        ResponseEntity<String> responseEntity = createAccount(accountOperation);

        logger.info("Response: {}", responseEntity);
        assertEquals(CREATED, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertTrue(accountRepository.findByEmailAddress("lol@example.com").isPresent());
    }

    @Order(2)
    @Test
    void createAccountDuplicate() {
        AccountOperation accountOperation = new AccountOperation(AcmeProvider.LETS_ENCRYPT_STAGING, "Test", "lol@example.com");
        ResponseEntity<String> responseEntity = createAccount(accountOperation);

        logger.info("Response: {}", responseEntity);
        assertEquals(CONFLICT, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
    }

    @Order(3)
    @Test
    void login() throws Exception {
        AccountOperation accountOperation = new AccountOperation(AcmeProvider.LETS_ENCRYPT_STAGING, "Test", "lol@example.com");
        ResponseEntity<String> responseEntity = webClient.post()
                .uri("/v1/account/login")
                .bodyValue(accountOperation)
                .exchangeToMono(response -> response.toEntity(String.class))
                .block();

        logger.info("Response: {}", responseEntity);
        assertEquals(OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());

        LoginResponse loginResponse = objectMapper.readValue(responseEntity.getBody(), LoginResponse.class);
        assertNotNull(loginResponse.jwt());
        assertNotNull(loginResponse.account());
    }

    private ResponseEntity<String> createAccount(AccountOperation accountOperation) {
        return webClient.post()
                .uri("/v1/account/create")
                .bodyValue(accountOperation)
                .exchangeToMono(response -> response.toEntity(String.class))
                .block();
    }
}
