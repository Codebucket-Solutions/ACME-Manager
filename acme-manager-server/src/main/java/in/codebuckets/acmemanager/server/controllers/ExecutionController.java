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

package in.codebuckets.acmemanager.server.controllers;

import in.codebuckets.acmemanager.server.jpa.Certificate;
import in.codebuckets.acmemanager.server.jpa.CertificateRepository;
import in.codebuckets.acmemanager.server.jpa.CertificateStatus;
import in.codebuckets.acmemanager.server.jpa.ValidationRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static in.codebuckets.acmemanager.common.json.Responses.notFound;
import static in.codebuckets.acmemanager.common.json.Responses.ok;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RequestMapping("/v1/execution")
@RestController
public class ExecutionController {

    private final CertificateRepository certificateRepository;

    public ExecutionController(CertificateRepository certificateRepository) {
        this.certificateRepository = certificateRepository;
    }

    @GetMapping(value = "/available-automatic-executions", produces = APPLICATION_JSON_VALUE)
    public CompletableFuture<ResponseEntity<String>> availableAutomaticExecutions(@RequestParam Long certificateId) {
        return supplyAsync(() -> {
            Optional<Certificate> optionalCertificate = certificateRepository.findById(certificateId);

            // Check if the certificate exists
            if (optionalCertificate.isEmpty()) {
                return notFound("Certificate not found");
            }

            Certificate certificate = optionalCertificate.get();

            if (certificate.certificateStatus() == CertificateStatus.VALIDATION_FAILED) {
                return ok("Certificate Validation has already failed");
            }

            if (certificate.certificateStatus() == CertificateStatus.VALIDATION_EXPIRED) {
                return ok("Certificate is already valid");
            }

            List<ValidationRequest> validationRequests = certificate.validationRequests();



            return null;
        });
    }
}
