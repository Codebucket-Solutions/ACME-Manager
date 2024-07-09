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

package in.codebuckets.acme.manager.acme;

import lombok.Getter;

@Getter
public enum ChallengeType {

    /**
     * HTTP challenge
     * </br>
     *
     * Use the HTTP challenge to prove that you control a domain. This challenge requires you to place a file on a
     * specific path on your web server.
     *
     * <p></p>
     *
     * Valid for:
     * <ul>
     *     <li> Single FQDN </li>
     * </ul>
     */
    HTTP("http-01"),

    /**
     * DNS challenge
     * </br>
     *
     * Use the DNS challenge to prove that you control a domain. This challenge requires you to create a specific DNS
     * record for your domain.
     *
     * <p></p>
     *
     * Valid for:
     * <ul>
     *     <li> Single Domain </li>
     *     <li> Wildcard Domain </li>
     * </ul>
     */
    DNS("dns-01");

    private final String name;

    ChallengeType(String name) {
        this.name = name;
    }
}
