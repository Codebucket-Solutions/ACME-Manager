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

package in.codebuckets.acme.manager.dto;

import in.codebuckets.acme.manager.acme.ChallengeType;
import org.shredzone.acme4j.Status;

/**
 * Generic challenge message
 *
 * <ul>
 * <li>
 *     For HTTP challenge:
 *      <p> - challengeToken: Filename for challenge file. e.g. {@code http://example.com/.well-known/acme-challenge/{KEY}} </p>
 *      <p> - challengeAuthorization: Content of the challenge file </p>
 * </li>
 *
 * <li>
 *     For DNS challenge:
 *     <p> - challengeToken: DNS name </p>
 *     <p> - challengeAuthorization: DNS record TXT value </p>
 * </li>
 * </ul>
 *
 * @param challengeType          Type of challenge
 * @param domain                 Domain for a challenge
 * @param challengeToken         Challenge token
 * @param challengeAuthorization Challenge authorization
 */
public record ChallengeMessage(ChallengeType challengeType, String domain, Status status, String challengeToken, String challengeAuthorization) {

    public ChallengeMessage(ChallengeType challengeType, String domain, Status status) {
        this(challengeType, domain, status, null, null);
    }
}
