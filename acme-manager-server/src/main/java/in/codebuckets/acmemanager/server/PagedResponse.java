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

import java.util.List;

/**
 * {@link PagedResponse} is used to return paginated data.
 *
 * @param content       The list of data
 * @param pages         The number of pages
 * @param totalElements The total number of elements in this list
 * @param <T>           The type of data
 */
public record PagedResponse<T>(List<T> content, int pages, long totalElements) {
    // NO-OP
}
