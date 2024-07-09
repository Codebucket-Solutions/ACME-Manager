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

import in.codebuckets.acme.manager.acme.AcmeProvider;
import in.codebuckets.acme.manager.acme.ChallengeType;

import java.util.List;

/**
 * Represents a request to place an order for a certificate
 *
 * @param acmeProvider      {@link AcmeProvider} to use
 * @param domains            List of domains to order the certificate for
 * @param challengeType      {@link ChallengeType} to use
 * @param forceChallengeType {@code true} if the challenge type should be forced, {@code false} to select the first supported challenge type
 * @param saveKeyPair        {@code true} if the key pair should be saved, {@code false} otherwise
 */
public record PlaceOrder(AcmeProvider acmeProvider, List<String> domains, ChallengeType challengeType, boolean forceChallengeType, boolean saveKeyPair) {

}
