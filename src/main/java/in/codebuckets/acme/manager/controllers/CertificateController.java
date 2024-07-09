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

package in.codebuckets.acme.manager.controllers;

import in.codebuckets.acme.manager.ApplicableFilters;
import in.codebuckets.acme.manager.FilterService;
import in.codebuckets.acme.manager.Filters;
import in.codebuckets.acme.manager.PagedResponse;
import in.codebuckets.acme.manager.jpa.Certificate;
import in.codebuckets.acme.manager.jpa.CertificateRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

import static in.codebuckets.acme.manager.Responses.badRequest;
import static in.codebuckets.acme.manager.Responses.ok;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/v1/certificate")
public class CertificateController {

    private final CertificateRepository certificateRepository;
    private final FilterService filterService;

    public CertificateController(CertificateRepository certificateRepository, FilterService filterService) {
        this.certificateRepository = certificateRepository;
        this.filterService = filterService;
    }

    @GetMapping(value = "/get", produces = APPLICATION_JSON_VALUE)
    public CompletableFuture<ResponseEntity<String>> get(@RequestParam(required = false) Long certificateId, @RequestParam(required = false) String orderId) {
        return supplyAsync(() -> {
            if (certificateId != null) {
                return ok(certificateRepository.findById(certificateId));
            } else if (orderId != null) {
                return ok(certificateRepository.findByOrderId(orderId));
            } else {
                return badRequest("Either certificateId or orderId is required");
            }
        });
    }

    @PostMapping(value = "/get", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public CompletableFuture<ResponseEntity<String>> get(@RequestBody Filters filters) {
        return supplyAsync(() -> {
            PagedResponse<Certificate> pagedResponse = filterService.filter(filters, ApplicableFilters.CERTIFICATE_FILTER_KEYS, Certificate.class);
            return ok(pagedResponse);
        });
    }
}
