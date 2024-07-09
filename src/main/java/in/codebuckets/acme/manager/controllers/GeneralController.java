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

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static in.codebuckets.acme.manager.Responses.ok;
import static in.codebuckets.acme.manager.acme.AcmeProvider.LETS_ENCRYPT;
import static in.codebuckets.acme.manager.acme.AcmeProvider.LETS_ENCRYPT_STAGING;
import static java.util.List.of;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/v1/general")
public class GeneralController {

    @GetMapping(value = "/acme-providers", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<String> acmeProviders() {
        return ok(of(LETS_ENCRYPT, LETS_ENCRYPT_STAGING));
    }
}
