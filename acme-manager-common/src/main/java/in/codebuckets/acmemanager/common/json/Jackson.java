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

package in.codebuckets.acmemanager.common.json;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import static java.util.Collections.singletonMap;

public final class Jackson {

    /**
     * Jackson {@link ObjectMapper} configured with custom serializers and deserializers.
     */
    public static final ObjectMapper JSON_MAPPER = new ObjectMapper()
            .setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
            .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
            .registerModule(new JavaTimeModule());

    private Jackson() {
        // Prevent instantiation
    }

    public static String toJson(String message) {
        return toJson(singletonMap("message", message));
    }

    public static String toJson(Object object) {
        try {
            return JSON_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert " + object.getClass().getName() + " to Json String", e);
        }
    }

    public static JsonNode unsafeRead(Object o) {
        try {
            return JSON_MAPPER.readTree(JSON_MAPPER.writeValueAsString(o));
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert " + o.getClass().getName() + " to Json String", e);
        }
    }
}
