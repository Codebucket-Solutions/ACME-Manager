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

import in.codebuckets.acmemanager.common.json.Responses;
import in.codebuckets.acmemanager.server.GlobalExceptionHandler;
import in.codebuckets.acmemanager.server.dto.AddDnsRecord;
import in.codebuckets.acmemanager.server.services.dns.CloudflareDnsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/v1/cloudflare-dns")
public class CloudflareDnsController {

    private final CloudflareDnsService cloudflareDnsService;

    public CloudflareDnsController(CloudflareDnsService cloudflareDnsService) {
        this.cloudflareDnsService = cloudflareDnsService;
    }

    @GetMapping(value = "/domains", produces = APPLICATION_JSON_VALUE)
    public CompletableFuture<ResponseEntity<String>> getDomains() {
        return CompletableFuture.supplyAsync(() -> {
            return Responses.ok(cloudflareDnsService.domains());
        }).exceptionally(GlobalExceptionHandler.GENERIC_EXCEPTION_HANDLER);
    }

    @GetMapping(value = "/records", produces = APPLICATION_JSON_VALUE)
    public CompletableFuture<ResponseEntity<String>> getRecords(@RequestParam("domainName") String domainName) {
        return CompletableFuture.supplyAsync(() -> {

            Map<String, String> domains = cloudflareDnsService.domains();

            if (!domains.containsKey(domainName)) {
                return Responses.badRequest("Domain not found");
            }

            return Responses.ok(cloudflareDnsService.listDnsRecords(domains.get(domainName)));
        }).exceptionally(GlobalExceptionHandler.GENERIC_EXCEPTION_HANDLER);
    }

    @PostMapping(value = "/add-record", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public CompletableFuture<ResponseEntity<String>> addRecord(@RequestParam String zoneId, @RequestBody AddDnsRecord addDnsRecord) {
        return CompletableFuture.supplyAsync(() -> {
            return Responses.ok(cloudflareDnsService.addDnsRecord(zoneId, addDnsRecord));
        }).exceptionally(GlobalExceptionHandler.GENERIC_EXCEPTION_HANDLER);
    }

    @DeleteMapping(value = "/delete-record", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public CompletableFuture<ResponseEntity<String>> deleteRecord(@RequestParam String zoneId, @RequestParam String recordId) {
        return CompletableFuture.supplyAsync(() -> {
            return Responses.ok(cloudflareDnsService.deleteDnsRecord(zoneId, recordId));
        }).exceptionally(GlobalExceptionHandler.GENERIC_EXCEPTION_HANDLER);
    }
}
