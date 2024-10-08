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

package in.codebuckets.acmemanager.server;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletionException;

public final class InvalidFilterKeysException extends CompletionException {

    @Serial
    private static final long serialVersionUID = -7134882752377478157L;

    private final List<AvailableFilters> availableFilters;

    public InvalidFilterKeysException(List<AvailableFilters> availableFilters) {
        this.availableFilters = new ArrayList<>(availableFilters);
    }

    public List<AvailableFilters> applicableFilters() {
        return Collections.unmodifiableList(availableFilters);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
