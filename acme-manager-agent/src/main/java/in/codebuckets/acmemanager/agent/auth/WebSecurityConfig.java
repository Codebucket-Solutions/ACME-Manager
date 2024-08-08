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

package in.codebuckets.acmemanager.agent.auth;

import in.codebuckets.acmemanager.common.auth.CustomAccessDeniedHandler;
import in.codebuckets.acmemanager.common.auth.CustomAuthenticationEntryPoint;
import in.codebuckets.acmemanager.common.auth.TokenAuthenticationManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class WebSecurityConfig {

    @Value("${app.apiKey}")
    private String apiKey;

    public static final String AUTH_HEADER = "X-Api-Key";

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http.authorizeExchange(exchanges -> exchanges
                        .pathMatchers(HttpMethod.GET, "/.well-known/acme-challenge/*").permitAll() // For ACME client to verify domain ownership
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
                        String value = exchange.getRequest().getHeaders().getFirst(AUTH_HEADER);
                        if (value != null && value.equals(apiKey)) {
                            return Mono.just(new SecurityContextImpl(new TokenAuthentication(value, true)));
                        } else {
                            return Mono.just(new SecurityContextImpl(TokenAuthentication.UNAUTHENTICATED));
                        }
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
