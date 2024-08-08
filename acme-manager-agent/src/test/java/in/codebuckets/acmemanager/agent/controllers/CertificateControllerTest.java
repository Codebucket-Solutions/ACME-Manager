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

import in.codebuckets.acmemanager.agent.AbstractTest;
import in.codebuckets.acmemanager.agent.dto.CertificateKeyPairEntry;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static in.codebuckets.acmemanager.agent.auth.WebSecurityConfig.AUTH_HEADER;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@TestMethodOrder(OrderAnnotation.class)
class CertificateControllerTest extends AbstractTest {

    private static final String certificate = """
            MIIB4DCCAYWgAwIBAgIUFI695ODcseSfG86cNWerCB4QKIgwCgYIKoZIzj0EAwIw
            RTELMAkGA1UEBhMCQVUxEzARBgNVBAgMClNvbWUtU3RhdGUxITAfBgNVBAoMGElu
            dGVybmV0IFdpZGdpdHMgUHR5IEx0ZDAeFw0yNDA4MDgxMTUxNTlaFw0yNTA4MDgx
            MTUxNTlaMEUxCzAJBgNVBAYTAkFVMRMwEQYDVQQIDApTb21lLVN0YXRlMSEwHwYD
            VQQKDBhJbnRlcm5ldCBXaWRnaXRzIFB0eSBMdGQwWTATBgcqhkjOPQIBBggqhkjO
            PQMBBwNCAATw3MjDNLQNI9SOza3o+5iTCgQWnbjvXlSTd/H/euFLjJUT40+P2/eD
            rX0vONptpmj8dQ5u7eDpOnKmDdyNFatpo1MwUTAdBgNVHQ4EFgQUGShWnSvad66B
            GSEx1xFl99J4UR4wHwYDVR0jBBgwFoAUGShWnSvad66BGSEx1xFl99J4UR4wDwYD
            VR0TAQH/BAUwAwEB/zAKBggqhkjOPQQDAgNJADBGAiEAiizgMY5uKB49nMQsBVee
            roL46+KU8yF1VxLz0WG6IFcCIQDtjlMmSeNqsWVkPq+l5MsxADe//unWjAPktBfI
            yZQNWg==
            """;

    private static final String key = """
            MHcCAQEEIJ9o6BuxlOdMXoRcWxmhMyIB9nrEcnX/cQwdc3XNwmZ+oAoGCCqGSM49
            AwEHoUQDQgAE8NzIwzS0DSPUjs2t6PuYkwoEFp24715Uk3fx/3rhS4yVE+NPj9v3
            g619LzjabaZo/HUObu3g6Tpypg3cjRWraQ==
            """;

    private static final CertificateKeyPairEntry certificateKeyPairEntry = new CertificateKeyPairEntry("example.com", certificate, key);

    @Value("${app.apiKey}")
    private String apiKey;

    @Value("${app.certificateDir}")
    private String certificateDirectory;

    protected CertificateControllerTest(@LocalServerPort int serverPort) {
        super(serverPort);
    }

    @Order(1)
    @Test
    void addCertificateKeyPair() {
        ResponseEntity<String> responseEntity = webClient.post()
                .uri("/v1/agent/certificate/add")
                .header(AUTH_HEADER, apiKey)
                .accept(APPLICATION_JSON)
                .contentType(APPLICATION_JSON)
                .bodyValue(certificateKeyPairEntry)
                .retrieve()
                .toEntity(String.class)
                .block();

        assertNotNull(responseEntity);
        assertEquals(CREATED, responseEntity.getStatusCode());
    }

    @Order(2)
    @Test
    void validateFiles() throws IOException {
        String cert = Files.readString(new File(certificateKeyPairEntry.directory(certificateDirectory) + "certificate.crt").toPath());
        String privateKey = Files.readString(new File(certificateKeyPairEntry.directory(certificateDirectory) + "private.key").toPath());

        assertEquals(certificate, cert);
        assertEquals(key, privateKey);
    }
}
