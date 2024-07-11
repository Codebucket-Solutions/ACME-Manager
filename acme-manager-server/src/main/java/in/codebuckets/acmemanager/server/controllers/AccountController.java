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

import in.codebuckets.acmemanager.common.Responses;
import in.codebuckets.acmemanager.server.GlobalExceptionHandler;
import in.codebuckets.acmemanager.server.dto.AccountOperation;
import in.codebuckets.acmemanager.server.dto.LoginResponse;
import in.codebuckets.acmemanager.server.jpa.Account;
import in.codebuckets.acmemanager.server.jpa.AccountRepository;
import in.codebuckets.acmemanager.server.services.AccountService;
import in.codebuckets.acmemanager.server.services.JwtService;
import org.shredzone.acme4j.Login;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.CompletableFuture.supplyAsync;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/v1/account")
public class AccountController {

    private final AccountRepository accountRepository;
    private final AccountService accountService;
    private final JwtService jwtService;

    public AccountController(AccountRepository accountRepository, AccountService accountService, JwtService jwtService) {
        this.accountRepository = accountRepository;
        this.accountService = accountService;
        this.jwtService = jwtService;
    }

    @PostMapping(value = "/create", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public CompletableFuture<ResponseEntity<String>> createAccount(@RequestBody AccountOperation accountOperation) {
        return supplyAsync(() -> {
            String email = accountOperation.emailAddress().toLowerCase();

            // Check if an account already exists
            if (accountRepository.findByEmailAddress(email).isPresent()) {
                return Responses.responseMessage(CONFLICT, "Account already exists");
            }

            Account account = accountService.createAccount(accountOperation.acmeProvider().session(), accountOperation.fullName(), email);
            return Responses.responseMessage(CREATED, accountRepository.save(account));
        }).exceptionally(GlobalExceptionHandler.GENERIC_EXCEPTION_HANDLER);
    }

    @PostMapping(value = "/login", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public CompletableFuture<ResponseEntity<String>> login(@RequestBody AccountOperation accountOperation) {
        return supplyAsync(() -> {
            String email = accountOperation.emailAddress().toLowerCase();

            Optional<Account> optionalAccount = accountRepository.findByEmailAddress(email);
            if (optionalAccount.isEmpty()) {
                return Responses.notFound("Account not found");
            }

            Account account = optionalAccount.get();

            Login login = accountService.loginAccount(accountOperation.acmeProvider().session(), account);
            if (!login.getAccountLocation().toString().equalsIgnoreCase(account.accountLocation())) {
                return Responses.responseError(CONFLICT, "Account location mismatch");
            }

            return Responses.ok(new LoginResponse(jwtService.generateJwt(account), account));
        }).exceptionally(GlobalExceptionHandler.GENERIC_EXCEPTION_HANDLER);
    }
}
