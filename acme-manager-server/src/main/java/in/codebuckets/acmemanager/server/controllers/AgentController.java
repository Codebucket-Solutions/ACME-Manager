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

import in.codebuckets.acmemanager.server.AvailableFilters;
import in.codebuckets.acmemanager.server.Filters;
import in.codebuckets.acmemanager.server.PagedResponse;
import in.codebuckets.acmemanager.server.dto.AgentRegister;
import in.codebuckets.acmemanager.server.jpa.Agent;
import in.codebuckets.acmemanager.server.jpa.AgentRepository;
import in.codebuckets.acmemanager.server.services.AgentService;
import in.codebuckets.acmemanager.server.services.FilterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static in.codebuckets.acmemanager.common.json.Responses.ok;
import static in.codebuckets.acmemanager.common.json.Responses.responseError;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RequestMapping("/v1/agent")
@RestController
public class AgentController {

    private final AgentRepository agentRepository;
    private final FilterService filterService;
    private final AgentService agentService;

    public AgentController(AgentRepository agentRepository, FilterService filterService, AgentService agentService) {
        this.agentRepository = agentRepository;
        this.filterService = filterService;
        this.agentService = agentService;
    }

    @PutMapping(value = "/register", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public CompletableFuture<ResponseEntity<String>> register(@RequestBody AgentRegister agentRegister) {
        return supplyAsync(() -> {
            Optional<Agent> optionalAgent = agentRepository.findAgentByToken(agentRegister.token());
            if (optionalAgent.isEmpty()) {
                return ok("Agent not found");
            }

            Agent agent = optionalAgent.get();

            if (agentService.register(agent)) {
                agent.url(agentRegister.url());
                agentRepository.save(agent);
                return ok(agent);
            } else {
                return responseError(CONFLICT, "Agent already registered");
            }
        });
    }

    @DeleteMapping(value = "/deregister", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public CompletableFuture<ResponseEntity<String>> deregister(@RequestBody AgentRegister agentRegister) {
        return supplyAsync(() -> {
            Optional<Agent> optionalAgent = agentRepository.findAgentByToken(agentRegister.token());
            if (optionalAgent.isEmpty()) {
                return ok("Agent not found");
            }

            Agent agent = optionalAgent.get();

            if (agentService.unregister(agent)) {
                agentRepository.save(agent);
                return ok(agent);
            } else {
                return responseError(CONFLICT, "Agent not registered");
            }
        });
    }

    @PostMapping(value = "/get", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public CompletableFuture<ResponseEntity<String>> get(@RequestBody Filters filters) {
        return supplyAsync(() -> {
            PagedResponse<Agent> pagedResponse = filterService.filter(filters, AvailableFilters.CERTIFICATE_FILTER_KEYS, Agent.class);
            return ok(pagedResponse);
        });
    }

    @PostMapping(value = "/add", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public CompletableFuture<ResponseEntity<String>> add(@RequestBody String agentName) {
        return supplyAsync(() -> {
            Agent agent = Agent.builder()
                    .name(agentName)
                    .token(UUID.randomUUID().toString())
                    .build();

            return ok(agentRepository.save(agent));
        });
    }
}
