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

package in.codebuckets.acmemanager.agent;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

import static in.codebuckets.acmemanager.agent.AuthService.AUTH_HEADER;
import static in.codebuckets.acmemanager.common.Responses.ok;
import static in.codebuckets.acmemanager.common.Responses.unauthorized;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RequestMapping("/v1/agent/metadata")
@RestController
public class MetadataController {

    @Value("${app.version}")
    private String version;

    private final AuthService authService;

    public MetadataController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping(value = "/", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getMetadata(@RequestHeader(AUTH_HEADER) String apiKey) {
        if (!authService.authenticate(apiKey)) {
            return unauthorized("Invalid API Key");
        }

        Map<String, Object> map = new HashMap<>();
        map.put("version", version);
        return ok(map);
    }
}
