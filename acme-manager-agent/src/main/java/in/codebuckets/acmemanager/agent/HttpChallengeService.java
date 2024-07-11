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

package in.codebuckets.acmemanager.agent;

import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service to store and retrieve challenge tokens and authorizations
 * </br>
 *
 * challengeToken: Filename for a challenge file. e.g. {@code http://example.com/.well-known/acme-challenge/{KEY}}
 * </p>
 * challengeAuthorization: Content of the challenge file
 */
@Service
public class HttpChallengeService {

    private final Map<String, String> challengeMap = new ConcurrentHashMap<>();

    /**
     * Add challenge token and authorization
     */
    public void addChallenge(String challengeToken, String challengeAuthorization) {
        challengeMap.put(challengeToken, challengeAuthorization);
    }

    /**
     * Get challenge authorization
     */
    public String getAuthorization(String token) {
        return challengeMap.get(token);
    }

    /**
     * Remove challenge token and authorization
     */
    public String removeChallenge(String challengeToken) {
        return challengeMap.remove(challengeToken);
    }

    /**
     * Get all challenge tokens and authorizations
     */
    public Map<String, String> challengeMap() {
        return Collections.unmodifiableMap(challengeMap);
    }
}
