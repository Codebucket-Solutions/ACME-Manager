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

package in.codebuckets.acmemanager.server.services.dns;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import in.codebuckets.acmemanager.server.ServiceException;
import in.codebuckets.acmemanager.server.dto.AddDnsRecord;
import in.codebuckets.acmemanager.server.dto.CloudflareDnsRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CloudflareDnsService {

    private static final Logger logger = LogManager.getLogger();
    private static final String BASE_URL = "https://api.cloudflare.com/client/v4";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    @Value("${cloudflare.api.token}")
    private String cloudflareApiToken;

    public CloudflareDnsService(HttpClient httpClient, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    /**
     * Get all the domains in the Cloudflare account
     *
     * @return a map of domain name to domain id
     */
    public Map<String, String> domains() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(BASE_URL + "/zones"))
                    .header("Authorization", "Bearer " + cloudflareApiToken)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode jsonNode = objectMapper.readTree(response.body());
                ArrayNode arrayNode = (ArrayNode) jsonNode.get("result");

                Map<String, String> domains = new HashMap<>();
                arrayNode.forEach(node -> {
                    domains.put(node.get("name").asText(), node.get("id").asText());
                });

                return domains;
            } else {
                throw new RuntimeException("Failed to get domains: " + response.body());
            }
        } catch (URISyntaxException | IOException | InterruptedException e) {
            logger.error("Error while fetching domains", e);
            throw new ServiceException(e.getCause().getMessage());
        }
    }

    /**
     * Add a DNS record to the specified zone
     *
     * @param zoneId  Cloudflare zone id
     * @param addDnsRecord the DNS record to add
     * @return the id of the created DNS record
     */
    public String addDnsRecord(String zoneId, AddDnsRecord addDnsRecord) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(BASE_URL + "/zones/" + zoneId + "/dns_records"))
                    .header("Authorization", "Bearer " + cloudflareApiToken)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(Map.of(
                            "type", addDnsRecord.type(),
                            "name", addDnsRecord.name(),
                            "content", addDnsRecord.content(),
                            "proxied", false,
                            "ttl", addDnsRecord.ttl()
                    ))))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("Failed to add DNS record: " + response.body());
            }

            JsonNode jsonNode = objectMapper.readTree(response.body());
            ObjectNode result = (ObjectNode) jsonNode.get("result");
            return result.get("id").asText();
        } catch (URISyntaxException | IOException | InterruptedException e) {
            logger.error("Error while adding DNS record", e);
            throw new ServiceException(e.getCause().getMessage());
        }
    }

    /**
     * Delete a DNS record from the specified zone
     *
     * @param zoneId   Cloudflare zone id
     * @param recordId DNS record id
     */
    public boolean deleteDnsRecord(String zoneId, String recordId) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(BASE_URL + "/zones/" + zoneId + "/dns_records/" + recordId))
                    .header("Authorization", "Bearer " + cloudflareApiToken)
                    .DELETE()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("Failed to delete DNS record: " + response.body());
            } else {
                logger.info("Deleted DNS record: {} from Zone ID: {}", recordId, zoneId);
                return true;
            }
        } catch (URISyntaxException | IOException | InterruptedException e) {
            logger.error("Error while deleting DNS record", e);
            throw new ServiceException(e.getCause().getMessage());
        }
    }

    /**
     * List all DNS records in the specified zone
     *
     * @param zoneId Cloudflare zone id
     * @return a list of {@link CloudflareDnsRecord}
     */
    public List<CloudflareDnsRecord> listDnsRecords(String zoneId) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(BASE_URL + "/zones/" + zoneId + "/dns_records"))
                    .header("Authorization", "Bearer " + cloudflareApiToken)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode jsonNode = objectMapper.readTree(response.body());
                ArrayNode result = (ArrayNode) jsonNode.get("result");

                List<CloudflareDnsRecord> cloudflareDnsRecords = new ArrayList<>();
                result.forEach(node -> {
                    cloudflareDnsRecords.add(new CloudflareDnsRecord(
                            node.get("id").asText(),
                            node.get("name").asText(),
                            node.get("type").asText(),
                            node.get("content").asText(),
                            node.get("proxied").asBoolean(),
                            node.get("ttl").asInt()
                    ));
                });

                return cloudflareDnsRecords;
            } else {
                throw new RuntimeException("Failed to list DNS records: " + response.body());
            }
        } catch (URISyntaxException | IOException | InterruptedException e) {
            logger.error("Error while fetching DNS records", e);
            throw new ServiceException(e.getCause().getMessage());
        }
    }
}
