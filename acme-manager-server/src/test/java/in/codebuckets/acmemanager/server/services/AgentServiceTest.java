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

import in.codebuckets.acmemanager.server.controllers.AbstractTest;
import in.codebuckets.acmemanager.server.jpa.Agent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.web.server.LocalServerPort;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AgentServiceTest extends AbstractTest {

    @InjectMocks
    private AgentService agentService;

    protected AgentServiceTest(@LocalServerPort int serverPort) {
        super(serverPort);
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRegister() {
        Agent agent = new Agent();
        agent.isConnected(false);

        assertTrue(agentService.register(agent));
        assertTrue(agent.isConnected());

        agent.isConnected(true);
        assertFalse(agentService.register(agent));
    }

    @Test
    void testUnregister() {
        Agent agent = new Agent();
        agent.isConnected(true);

        assertTrue(agentService.unregister(agent));
        assertFalse(agent.isConnected());

        agent.isConnected(false);
        assertFalse(agentService.unregister(agent));
    }
}
