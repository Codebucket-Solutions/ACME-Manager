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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    public static final String AUTH_HEADER = "X-Api-Key";

    @Value("${app.apiKey}")
    private String apiKey;

    /**
     * Authenticate the API key
     *
     * @param apiKey API key to authenticate
     * @return true if the API key is valid, false otherwise
     */
    public boolean authenticate(String apiKey) {
        return this.apiKey.equals(apiKey);
    }

    public String apiKey() {
        return apiKey;
    }
}
