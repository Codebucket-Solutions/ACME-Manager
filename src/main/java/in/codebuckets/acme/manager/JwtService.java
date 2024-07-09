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

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import in.codebuckets.acme.manager.jpa.Account;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static java.time.temporal.ChronoUnit.HOURS;
import static java.util.concurrent.CompletableFuture.supplyAsync;

@Service
public class JwtService {

    private final Algorithm algorithm;

    public JwtService(@Value("${jwtKey}") String jwtKey) {
        algorithm = Algorithm.HMAC256(jwtKey);
    }

    /**
     * Generate a JWT for the given user.
     *
     * @param account {@link Account}
     * @return the JWT
     */
    public String generateJwt(Account account) {
        return generateJwt(Map.of("sub", account.id()));
    }

    /**
     * Generate a JWT for the given payload.
     *
     * @param payload the payload
     * @return the JWT
     */
    public String generateJwt(Map<String, ?> payload) {
        return JWT.create()
                .withIssuer("ACME Manager Server")
                .withAudience("ACME Manager User")
                .withPayload(payload)
                .withIssuedAt(Instant.now())
                .withExpiresAt(Instant.now().plus(12, HOURS))
                .withJWTId(UUID.randomUUID().toString())
                .sign(algorithm);
    }

    public DecodedJWT decodeJwt(String jwt) {
        return JWT.require(algorithm)
                .withIssuer("ACME Manager Server")
                .withAudience("ACME Manager User")
                .build()
                .verify(jwt);
    }

    public boolean isJwtValid(String jwt) {
        try {
            decodeJwt(jwt);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public CompletableFuture<Void> isAuthValid(String jwt) {
        return supplyAsync(() -> {
            try {
                decodeJwt(jwt);
            } catch (Exception e) {
                throw new ServiceException("Invalid Authentication");
            }
            return null;
        });
    }

    /**
     * Get the account ID from the given JWT.
     *
     * @param jwt the JWT
     * @return the account ID
     */
    public Long accountId(String jwt) {
        return decodeJwt(extractToken(jwt)).getClaim("sub").asLong();
    }

    /**
     * Get Bearer token from the given Authorization header value string
     */
    public static String extractToken(String bearer) {
        if (bearer == null || !bearer.startsWith("Bearer ") || bearer.length() < 8) {
            return null;
        }
        return bearer.substring(7);
    }
}
