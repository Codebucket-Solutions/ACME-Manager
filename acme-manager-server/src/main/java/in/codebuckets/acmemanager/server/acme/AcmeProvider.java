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

package in.codebuckets.acmemanager.server.acme;

import lombok.Getter;
import lombok.experimental.Accessors;
import org.shredzone.acme4j.Session;

@Getter
@Accessors(fluent = true)
public enum AcmeProvider {

    /**
     * Let's Encrypt ACME v2 production server.
     */
    LETS_ENCRYPT("LetsEncrypt", "acme://letsencrypt.org/", true),

    /**
     * Let's Encrypt ACME v2 staging server.
     */
    LETS_ENCRYPT_STAGING("LetsEncrypt Staging", "acme://letsencrypt.org/staging", false);

    private final String friendlyName;
    private final String serverUri;
    private final boolean production;
    private final Session session;

    AcmeProvider(String friendlyName, String serverUri, boolean production) {
        this.friendlyName = friendlyName;
        this.serverUri = serverUri;
        this.production = production;
        session = new Session(serverUri);
    }
}
