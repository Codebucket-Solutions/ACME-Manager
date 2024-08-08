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
import in.codebuckets.acmemanager.agent.dto.AddChallenge;
import in.codebuckets.acmemanager.common.json.Responses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.notFound;
import static org.springframework.http.ResponseEntity.ok;

@RequestMapping("/.well-known/acme-challenge/")
@RestController
public class AcmeController {

    private final HttpChallengeService httpChallengeService;

    public AcmeController(HttpChallengeService httpChallengeService) {
        this.httpChallengeService = httpChallengeService;
    }

    @GetMapping("/{challengeToken}")
    public ResponseEntity<String> getChallenge(@PathVariable String challengeToken) {
        String challengeAuthorization = httpChallengeService.getAuthorization(challengeToken);

        if (challengeAuthorization == null) {
            return notFound().build();
        } else {
            return ok(challengeAuthorization);
        }
    }

    @GetMapping(value = "/get-all", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getAllChallenges() {
        return Responses.ok(httpChallengeService.challengeMap());
    }

    @PostMapping(value = "/", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<String> addChallenge(@RequestBody AddChallenge addChallenge) {
        httpChallengeService.addChallenge(addChallenge.challengeToken(), addChallenge.challengeAuthorization());
        return Responses.ok("Challenge added successfully");
    }

    @DeleteMapping(value = "/", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<String> deleteChallenge(@RequestParam String challengeToken) {
        if (httpChallengeService.removeChallenge(challengeToken) != null) {
            return Responses.ok("Challenge deleted successfully");
        } else {
            return notFound().build();
        }
    }
}
