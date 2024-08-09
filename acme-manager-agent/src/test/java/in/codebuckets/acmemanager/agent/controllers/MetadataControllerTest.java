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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.codebuckets.acmemanager.agent.AbstractTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.Map;

import static in.codebuckets.acmemanager.agent.auth.WebSecurityConfig.AUTH_HEADER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON;

class MetadataControllerTest extends AbstractTest {

    @Value("${app.apiKey}")
    private String apiKey;

    @Autowired
    private ObjectMapper objectMapper;

    protected MetadataControllerTest(@LocalServerPort int serverPort) {
        super(serverPort);
    }

    @Test
    void getMetadata_validApiKey_returnsMetadata() throws Exception {
        String version = "1.0.0";

        ResponseEntity<String> responseEntity = webClient.get()
                .uri("/v1/agent/metadata/")
                .header(AUTH_HEADER, apiKey)
                .accept(APPLICATION_JSON)
                .retrieve()
                .toEntity(String.class)
                .block();

        assertEquals(OK, responseEntity.getStatusCode());

        Map<String, Object> map = objectMapper.readValue(responseEntity.getBody(), new TypeReference<>() {
        });

        assertEquals(version, map.get("version"));
    }

    @Test
    void getMetadata_invalidApiKey_returnsUnauthorized() throws Exception {
        String invalidApiKey = "invalidApiKey";

        Mono<ResponseEntity<String>> responseEntity = webClient.get()
                .uri("/v1/agent/metadata/")
                .header(AUTH_HEADER, invalidApiKey)
                .accept(APPLICATION_JSON)
                .retrieve()
                .toEntity(String.class);

        assertThrows(WebClientResponseException.class, responseEntity::block);
    }
}
