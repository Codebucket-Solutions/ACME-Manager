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

package in.codebuckets.acmemanager.server.services;

import in.codebuckets.acmemanager.server.jpa.Agent;
import in.codebuckets.acmemanager.server.jpa.AgentRepository;
import in.codebuckets.acmemanager.server.jpa.BackpressureAwareRepositoryRetriever;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

import static java.util.concurrent.TimeUnit.SECONDS;

@Service
public class AgentService extends BackpressureAwareRepositoryRetriever<Agent> {

    private static final Logger logger = LogManager.getLogger();

    private final AgentRepository agentRepository;
    private final HttpClient httpClient;

    public AgentService(AgentRepository agentRepository, HttpClient httpClient) {
        this.agentRepository = agentRepository;
        this.httpClient = httpClient;
    }

    /**
     * Register an {@link Agent}
     *
     * @param agent {@link Agent} to register
     * @return true if the agent is registered, false otherwise
     */
    public boolean register(Agent agent) {
        if (agent.isConnected()) {
            return false;
        }

        agent.isConnected(true);
        return true;
    }

    /**
     * Unregister an {@link Agent}
     *
     * @param agent {@link Agent} to unregister
     * @return true if the agent is unregistered, false otherwise
     */
    public boolean unregister(Agent agent) {
        if (!agent.isConnected()) {
            return false;
        }

        agent.isConnected(false);
        return true;
    }

    @Scheduled(initialDelay = 0, fixedDelay = 30, timeUnit = SECONDS)
    public void watch() {
        processElements();
    }

    /**
     * Run health check on the agent
     *
     * @param agent {@link Agent} to check
     */
    @Override
    protected boolean handle(Agent agent) {
        try {
            boolean isSuccess = doHealthCheck(agent);

            // If the health check is successful and the agent is not connected, then connect the agent
            // If the health check is successful and the agent is connected, then disconnect the agent
            if (isSuccess && !agent.isConnected()) {
                agent.isConnected(true);
                agentRepository.save(agent);
            } else if (isSuccess && agent.isConnected()) {
                agent.isConnected(false);
                agentRepository.save(agent);
            }
        } catch (URISyntaxException | IOException | InterruptedException e) {
            logger.error("Failed to connect to agent", e);
        }

        return true;
    }

    @Override
    protected Page<Agent> doQuery(Pageable pageable) {
        return agentRepository.findAll(pageable);
    }

    /**
     * Perform health check on the agent by calling the metadata endpoint
     * and checking if the response is 200 OK, if so then the agent is healthy
     * otherwise the agent is unhealthy.
     *
     * @param agent {@link Agent} to check
     * @return true if the agent is healthy, false otherwise
     * @throws URISyntaxException   If the URI is invalid
     * @throws IOException          If an I/O error occurs
     * @throws InterruptedException If the operation is interrupted
     */
    boolean doHealthCheck(Agent agent) throws URISyntaxException, IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(agent.url() + "/v1/agent/metadata"))
                .GET()
                .timeout(Duration.of(10, ChronoUnit.SECONDS))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.statusCode() == 200;
    }
}
