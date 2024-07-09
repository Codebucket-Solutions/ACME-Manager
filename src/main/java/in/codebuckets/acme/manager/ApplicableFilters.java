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

package in.codebuckets.acme.manager;

import in.codebuckets.acme.manager.jpa.Certificate;

import java.util.List;
import java.util.Set;

import static in.codebuckets.acme.manager.Filters.Type.BETWEEN_DATES;
import static in.codebuckets.acme.manager.Filters.Type.CONTAINS;
import static in.codebuckets.acme.manager.Filters.Type.EQUALS_DATE;
import static in.codebuckets.acme.manager.Filters.Type.EXACT;
import static in.codebuckets.acme.manager.Filters.Type.GREATER_THAN_DATE;
import static in.codebuckets.acme.manager.Filters.Type.LESS_THAN_DATE;
import static in.codebuckets.acme.manager.Filters.Type.NOT_CONTAINS;
import static in.codebuckets.acme.manager.Filters.Type.NOT_EXACT;
import static in.codebuckets.acme.manager.Filters.Type.SEARCH_STRING;
import static in.codebuckets.acme.manager.utils.SetUtil.newSet;

public record ApplicableFilters(String friendlyName, String key, String type, Set<Filters.Type> types) {

    private static final Set<Filters.Type> GENERAL_FILTERS = Set.of(EXACT, CONTAINS, NOT_EXACT, NOT_CONTAINS);
    private static final Set<Filters.Type> DATES_FILTER = Set.of(LESS_THAN_DATE, GREATER_THAN_DATE, EQUALS_DATE, BETWEEN_DATES);

    /**
     * {@link Certificate}
     */
    public static final List<ApplicableFilters> CERTIFICATE_FILTER_KEYS = List.of(
            // General and Search
            new ApplicableFilters("ID", "id", "input", Set.of(EXACT)),
            new ApplicableFilters("Order ID", "orderId", "input", newSet(GENERAL_FILTERS, SEARCH_STRING)),
            new ApplicableFilters("Domains", "domains", "input", newSet(GENERAL_FILTERS, SEARCH_STRING)),

            // Dates
            new ApplicableFilters("Created At", "createdAt", "date", DATES_FILTER),
            new ApplicableFilters("Updated At", "updatedAt", "date", DATES_FILTER)
    );
}
