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
import in.codebuckets.acme.manager.dto.AccountOperation;
import in.codebuckets.acme.manager.dto.LoginResponse;
import in.codebuckets.acme.manager.jpa.Account;
import in.codebuckets.acme.manager.jpa.AccountRepository;
import org.shredzone.acme4j.Login;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static in.codebuckets.acme.manager.GlobalExceptionHandler.GENERIC_EXCEPTION_HANDLER;
import static in.codebuckets.acme.manager.Responses.notFound;
import static in.codebuckets.acme.manager.Responses.ok;
import static in.codebuckets.acme.manager.Responses.responseError;
import static in.codebuckets.acme.manager.Responses.responseMessage;
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
                return responseMessage(CONFLICT, "Account already exists");
            }

            Account account = accountService.createAccount(accountOperation.acmeProvider().session(), email);
            return responseMessage(CREATED, accountRepository.save(account));
        }).exceptionally(GENERIC_EXCEPTION_HANDLER);
    }

    @PostMapping(value = "/login", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public CompletableFuture<ResponseEntity<String>> login(@RequestBody AccountOperation accountOperation) {
        return supplyAsync(() -> {
            String email = accountOperation.emailAddress().toLowerCase();

            Optional<Account> optionalAccount = accountRepository.findByEmailAddress(email);
            if (optionalAccount.isEmpty()) {
                return notFound("Account not found");
            }

            Account account = optionalAccount.get();

            Login login = accountService.loginAccount(accountOperation.acmeProvider().session(), account);
            if (!login.getAccountLocation().toString().equalsIgnoreCase(account.accountLocation())) {
                return responseError(CONFLICT, "Account location mismatch");
            }

            return ok(new LoginResponse(jwtService.generateJwt(account), account));
        }).exceptionally(GENERIC_EXCEPTION_HANDLER);
    }
}
