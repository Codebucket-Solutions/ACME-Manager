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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import static in.codebuckets.acmemanager.common.utils.StringUtil.maskString;

@Component
public class EnvironmentLogger implements ApplicationListener<ApplicationStartedEvent> {

    private static final Logger logger = LogManager.getLogger();

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        Environment environment = event.getApplicationContext().getEnvironment();

        logger.info("Agent Version: {}", environment.getProperty("app.version"));
        logger.info("API Key: {}", maskString(environment.getProperty("app.apiKey")));
        logger.info("Certificate Directory: {}", environment.getProperty("app.certificateDir"));
    }
}
