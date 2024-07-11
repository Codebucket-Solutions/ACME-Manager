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

package in.codebuckets.acmemanager.server.auth;

import in.codebuckets.acmemanager.server.jpa.Account;
import in.codebuckets.acmemanager.server.jpa.AccountRepository;
import in.codebuckets.acmemanager.server.services.JwtService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class WebSecurityConfig {

    private final JwtService jwtService;
    private final AccountRepository accountRepository;

    public WebSecurityConfig(JwtService jwtService, AccountRepository accountRepository) {
        this.jwtService = jwtService;
        this.accountRepository = accountRepository;
    }

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http.authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/v1/account/**").permitAll()
                        .pathMatchers("/v1/general/**").permitAll()
                        .pathMatchers("/v1/agent/register").permitAll()
                        .pathMatchers("/**").access(new TokenAuthenticationManager())
                        .anyExchange()
                        .permitAll())
                .csrf(csrfSpec -> csrfSpec.disable())
                .httpBasic(httpBasicSpec -> httpBasicSpec.disable())
                .securityContextRepository(new ServerSecurityContextRepository() {

                    @Override
                    public Mono<Void> save(ServerWebExchange exchange, SecurityContext context) {
                        return null;
                    }

                    @Override
                    public Mono<SecurityContext> load(ServerWebExchange exchange) {
                        return Mono.justOrEmpty(exchange.getRequest().getHeaders().getFirst(AUTHORIZATION))
                                .filter(jwtService::isJwtValid)
                                .flatMap(token -> {
                                    Long userId = jwtService.accountId(token);
                                    return Mono.fromCallable(() -> accountRepository.findById(userId))
                                            .subscribeOn(Schedulers.boundedElastic())
                                            .flatMap(optionalAccount -> optionalAccount
                                                    .map(account -> Mono.just(createAuthenticatedContext(account, token)))
                                                    .orElseGet(() -> Mono.just(new SecurityContextImpl(JwtAuthentication.UNAUTHENTICATED)))
                                            );
                                })
                                .switchIfEmpty(Mono.just(new SecurityContextImpl(JwtAuthentication.UNAUTHENTICATED)));
                    }

                    private static SecurityContext createAuthenticatedContext(Account account, String token) {
                        return new SecurityContextImpl(new JwtAuthentication(account, token, true));
                    }
                })
                .exceptionHandling(new Customizer<ServerHttpSecurity.ExceptionHandlingSpec>() {
                    @Override
                    public void customize(ServerHttpSecurity.ExceptionHandlingSpec exceptionHandlingSpec) {
                        exceptionHandlingSpec.accessDeniedHandler(new CustomAccessDeniedHandler());
                        exceptionHandlingSpec.authenticationEntryPoint(new CustomAuthenticationEntryPoint());
                    }
                }).build();
    }
}
