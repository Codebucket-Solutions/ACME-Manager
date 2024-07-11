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

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.emptyList;

public class Filters {

    @Schema(description = "The search string to apply to the query", example = "awsEc2")
    @JsonProperty("search")
    private String search;

    @Schema(description = "The list of filters to apply to the query", example = "[{\"type\":\"EXACT\",\"key\":\"name\",\"value\":\"test\",\"ignore_case\":true}]")
    @JsonProperty("filters")
    private List<Filter> filters;

    @Schema(description = "The page number to return")
    @JsonProperty("page")
    private int page;

    @Schema(description = "The number of records to return per page")
    @JsonProperty("size")
    private int size;

    @Schema(description = "The sort order to apply to the query", example = "{\"key\":\"name\",\"order\":\"ASC\"}")
    @JsonProperty("sort")
    private Sort sort;

    public Filters() {
        // Empty constructor needed for Jackson.
    }

    public Filters(int page, int size) {
        this("", emptyList(), page, size, null);
    }

    public Filters(List<Filter> filters, int page, int size) {
        this("", filters, page, size, null);
    }

    public Filters(List<Filter> filters, int page, int size, Sort sort) {
        this("", filters, page, size, sort);
    }

    public Filters(String search, List<Filter> filters, int page, int size, Sort sort) {
        this.search = search;
        this.filters = new ArrayList<>(filters);
        this.page = page;
        this.size = size;
        this.sort = sort;
    }

    public String search() {
        return search;
    }

    public List<Filter> filters() {
        return Collections.unmodifiableList(filters);
    }

    public int page() {
        return page;
    }

    public int size() {
        return size;
    }

    public Sort sort() {
        return sort;
    }

    public static class Filter {
        @Schema(description = "The type of filter to apply")
        @JsonProperty("type")
        private Type type;

        @Schema(description = "The key to filter on")
        @JsonProperty("key")
        private String key;

        @Schema(description = "The value to filter on")
        @JsonProperty("value")
        private String value;

        @Schema(description = "Whether to ignore case when filtering", example = "true")
        @JsonProperty("ignore_case")
        private boolean ignoreCase;

        public Filter() {
            this(Type.EXACT, "", "", false);
        }

        public Filter(Type type, String key, String value) {
            this(type, key, value, false);
        }

        public Filter(Type type, String key, String value, boolean ignoreCase) {
            this.type = type;
            this.key = key;
            this.value = value;
            this.ignoreCase = ignoreCase;
        }

        public Type type() {
            return type;
        }

        public String key() {
            return key;
        }

        public String value() {
            return value;
        }

        public boolean ignoreCase() {
            return ignoreCase;
        }
    }

    public enum Type {
        // The following are valid for all fields
        EXACT,
        CONTAINS,

        // The following are valid for all fields
        NOT_EXACT,
        NOT_CONTAINS,

        // The following are only valid for Date fields
        LESS_THAN_DATE,
        EQUALS_DATE,
        BETWEEN_DATES,
        GREATER_THAN_DATE,

        // The following are only valid for searching fields (String)
        SEARCH_STRING,

        // The following are only valid for searching fields (Numbers)
        SEARCH_NUMBER
    }

    public record Sort(
            @JsonProperty(value = "key", required = true) String key,
            @JsonProperty(value = "order", required = true) SortOrder order) {
    }

    public enum SortOrder {
        ASC,
        DESC
    }
}
