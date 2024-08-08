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

import in.codebuckets.acmemanager.agent.dto.CertificateKeyPairEntry;
import in.codebuckets.acmemanager.common.json.Responses;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.CompletableFuture;

import static in.codebuckets.acmemanager.common.json.Responses.badRequest;
import static in.codebuckets.acmemanager.common.json.Responses.internalServerError;
import static in.codebuckets.acmemanager.common.json.Responses.responseMessage;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RequestMapping("/v1/agent/certificate")
@RestController
public class CertificateController {

    private static final Logger logger = LogManager.getLogger();

    @Value("${app.certificateDir}")
    private String certificateDirectory;

    @PostMapping(value = "/add", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public CompletableFuture<ResponseEntity<String>> addCertificateKeyPair(@RequestBody CertificateKeyPairEntry certificateKeyPairEntry) {
        return supplyAsync(() -> {
            String directory = certificateKeyPairEntry.directory(certificateDirectory);

            // Create a directory if it does not exist
            try {
                Files.createDirectories(new File(directory).toPath());
            } catch (IOException e) {
                logger.error("Failed to create directory", e);
                return internalServerError("Failed to create directory");
            }

            // Create certificate and private key files
            try (FileWriter certificateWriter = new FileWriter(directory + "certificate.crt", false);
                 FileWriter privateKeyWriter = new FileWriter(directory + "private.key", false)) {

                certificateWriter.write(certificateKeyPairEntry.certificate());
                privateKeyWriter.write(certificateKeyPairEntry.privateKey());

                return responseMessage(CREATED, "Certificate added successfully");
            } catch (Exception e) {
                logger.error("Failed to add certificate", e);
                return badRequest("Failed to add certificate");
            }
        });
    }
}
