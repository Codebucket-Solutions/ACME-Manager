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

import in.codebuckets.acmemanager.server.jpa.Certificate;

import java.util.List;
import java.util.Set;

import static in.codebuckets.acmemanager.server.utils.SetUtil.newSet;


public record AvailableFilters(String friendlyName, String key, String type, Set<Filters.Type> types) {

    private static final Set<Filters.Type> GENERAL_FILTERS = Set.of(Filters.Type.EXACT, Filters.Type.CONTAINS, Filters.Type.NOT_EXACT, Filters.Type.NOT_CONTAINS);
    private static final Set<Filters.Type> DATES_FILTER = Set.of(Filters.Type.LESS_THAN_DATE, Filters.Type.GREATER_THAN_DATE, Filters.Type.EQUALS_DATE, Filters.Type.BETWEEN_DATES);

    /**
     * {@link Certificate}
     */
    public static final List<AvailableFilters> CERTIFICATE_FILTER_KEYS = List.of(
            // General and Search
            new AvailableFilters("ID", "id", "input", Set.of(Filters.Type.EXACT)),
            new AvailableFilters("Order ID", "orderId", "input", newSet(GENERAL_FILTERS, Filters.Type.SEARCH_STRING)),
            new AvailableFilters("Domains", "domains", "input", newSet(GENERAL_FILTERS, Filters.Type.SEARCH_STRING)),

            // Dates
            new AvailableFilters("Created At", "createdAt", "date", DATES_FILTER),
            new AvailableFilters("Updated At", "updatedAt", "date", DATES_FILTER)
    );
}
