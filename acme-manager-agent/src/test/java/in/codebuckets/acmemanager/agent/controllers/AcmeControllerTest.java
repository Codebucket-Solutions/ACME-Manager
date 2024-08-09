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
package in.codebuckets.acmemanager.agent.controllers;

import in.codebuckets.acmemanager.agent.HttpChallengeService;
import in.codebuckets.acmemanager.agent.MessageDto;
import in.codebuckets.acmemanager.agent.dto.AddChallenge;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Collections;
import java.util.Map;

import static in.codebuckets.acmemanager.agent.auth.WebSecurityConfig.AUTH_HEADER;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AcmeControllerTest {

    @Autowired
    private WebTestClient webClient;

    @Autowired
    private HttpChallengeService httpChallengeService;

    @Value("${app.apiKey}")
    private String apiKey;

    @Order(2)
    @Test
    void getChallenge() {
        String challengeToken = "testToken";
        String challengeAuthorization = "testAuthorization";

        httpChallengeService.addChallenge(challengeToken, challengeAuthorization);

        webClient.get()
                .uri("/.well-known/acme-challenge/" + challengeToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo(challengeAuthorization);
    }

    @Order(3)
    @Test
    void getChallenge_NotFound() {
        String challengeToken = "invalidToken";

        webClient.get()
                .uri("/.well-known/acme-challenge/{challengeToken}", challengeToken)
                .header(AUTH_HEADER, apiKey)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Order(4)
    @Test
    void getAllChallenges() {
        webClient.get()
                .uri("/.well-known/acme-challenge/get-all")
                .header(AUTH_HEADER, apiKey)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Map.class).isEqualTo(Collections.emptyMap());
    }

    @Order(1)
    @Test
    void addChallenge() {
        AddChallenge addChallenge = new AddChallenge("testToken", "testAuthorization");

        webClient.post()
                .uri("/.well-known/acme-challenge/")
                .header(AUTH_HEADER, apiKey)
                .bodyValue(addChallenge)
                .exchange()
                .expectStatus().isOk()
                .expectBody(MessageDto.class).isEqualTo(new MessageDto("Challenge added successfully"));
    }

    @Order(5)
    @Test
    void deleteChallenge() {
        String challengeToken = "testToken";
        String challengeAuthorization = "testAuthorization";

        httpChallengeService.addChallenge(challengeToken, challengeAuthorization);

        webClient.delete()
                .uri(uriBuilder -> uriBuilder.path("/.well-known/acme-challenge/")
                        .queryParam("challengeToken", challengeToken)
                        .build())
                .header(AUTH_HEADER, apiKey)
                .exchange()
                .expectStatus().isOk()
                .expectBody(MessageDto.class).isEqualTo(new MessageDto("Challenge deleted successfully"));
    }

    @Order(6)
    @Test
    void deleteChallenge_NotFound() {
        String challengeToken = "invalidToken";

        webClient.delete()
                .uri(uriBuilder -> uriBuilder.path("/.well-known/acme-challenge/")
                        .queryParam("challengeToken", challengeToken)
                        .build())
                .header(AUTH_HEADER, apiKey)
                .exchange()
                .expectStatus().isNotFound();
    }
}
