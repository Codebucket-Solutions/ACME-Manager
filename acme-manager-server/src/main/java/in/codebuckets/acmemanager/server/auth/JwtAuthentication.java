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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.io.Serial;
import java.util.Collection;
import java.util.List;

import static java.util.Collections.emptyList;

/**
 * {@link Authentication} implementation for JWT.
 */
public final class JwtAuthentication implements Authentication {

    public static final JwtAuthentication UNAUTHENTICATED = new JwtAuthentication(null, "", false);

    @Serial
    private static final long serialVersionUID = 9040528503631187457L;

    private final String jwtToken;
    private final Account account;
    private final Collection<? extends GrantedAuthority> authorities;
    private boolean isAuthenticated;

    public JwtAuthentication(Account account, String jwtToken, boolean isAuthenticated) {
        this.jwtToken = jwtToken;
        this.account = account;
        this.isAuthenticated = isAuthenticated;

        if (isAuthenticated) {
            authorities = List.of(new SimpleGrantedAuthority("ADMIN"));
        } else {
            authorities = emptyList();
        }
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getCredentials() {
        return jwtToken;
    }

    @Override
    public Account getDetails() {
        return account;
    }

    @Override
    public String getPrincipal() {
        return account.emailAddress();
    }

    @Override
    public boolean isAuthenticated() {
        return isAuthenticated;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) {
        this.isAuthenticated = isAuthenticated;
    }

    @Override
    public String getName() {
        return account.fullName();
    }
}
