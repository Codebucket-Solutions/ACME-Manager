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

import in.codebuckets.acmemanager.server.AvailableFilters;
import in.codebuckets.acmemanager.server.Filters;
import in.codebuckets.acmemanager.server.InvalidFilterKeysException;
import in.codebuckets.acmemanager.server.PagedResponse;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

@Service
public final class FilterService {

    private final EntityManagerFactory entityManagerFactory;

    @Autowired
    public FilterService(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    /**
     * Apply {@link Filters} on a given table and return the paginated response.
     *
     * @param filters           The {@link Filters} to apply
     * @param availableFilters The list of {@link AvailableFilters} that can be applied
     * @param clazz             The class of the table
     * @param <T>               The type of data
     * @return {@link PagedResponse} containing the paginated data
     * @throws InvalidFilterKeysException if the filter keys are invalid
     */
    public <T> PagedResponse<T> filter(Filters filters, List<AvailableFilters> availableFilters, Class<T> clazz) {
        SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);

        try (Session contentSession = sessionFactory.openSession()) {
            CriteriaBuilder criteriaBuilder = sessionFactory.getCriteriaBuilder();
            CriteriaQuery<T> criteriaQuery = criteriaBuilder.createQuery(clazz);
            Root<T> root = criteriaQuery.from(clazz);

            // Apply the predicate to the query
            applyFiltersToRootQuery(root, criteriaBuilder, criteriaQuery, filters, availableFilters);

            int offset = (filters.page() - 1) * filters.size();

            List<T> content = contentSession.createQuery(criteriaQuery)
                    .setFirstResult(offset)
                    .setMaxResults(filters.size())
                    .getResultList();

            try (Session countSession = sessionFactory.openSession()) {
                // Count total elements
                CriteriaBuilder criteriaBuilderCount = countSession.getCriteriaBuilder();
                CriteriaQuery<Long> criteriaQueryCount = criteriaBuilderCount.createQuery(Long.class);
                Root<T> rootCount = criteriaQueryCount.from(clazz);
                criteriaQueryCount.select(criteriaBuilderCount.count(rootCount));
                criteriaQueryCount.where(applyFilters(rootCount, criteriaBuilderCount, filters, availableFilters));

                Long totalElements = countSession.createQuery(criteriaQueryCount).getSingleResult();

                int totalPages = (int) Math.ceil((double) totalElements / filters.size());
                return new PagedResponse<>(content, totalPages, totalElements);
            }
        }
    }

    private static Predicate applyFilters(Root<?> root, CriteriaBuilder criteriaBuilder, Filters saasFilter, List<AvailableFilters> validKeys) {
        return applyFiltersToRootQuery(root, criteriaBuilder, null, saasFilter, validKeys);
    }

    private static Predicate applyFiltersToRootQuery(Root<?> root, CriteriaBuilder criteriaBuilder, CriteriaQuery<?> criteriaQuery, Filters saasFilter, List<AvailableFilters> validKeys) {
        List<Predicate> predicates = new ArrayList<>();

        // If the 'search' field is not null, apply 'search' to the query
        if (saasFilter.search() != null && !saasFilter.search().isBlank()) {
            List<Predicate> searchPredicates = new ArrayList<>();

            for (AvailableFilters availableFilters : validKeys) {
                if (availableFilters.types().contains(Filters.Type.SEARCH_STRING) && !NUMBER_REGEX.matcher(saasFilter.search()).matches()) {
                    Predicate predicate = criteriaBuilder.like(criteriaBuilder.lower(keyExpression(root, availableFilters.key())), '%' + saasFilter.search().toLowerCase() + '%');
                    searchPredicates.add(predicate);
                } else if (availableFilters.types().contains(Filters.Type.SEARCH_NUMBER) && NUMBER_REGEX.matcher(saasFilter.search()).matches()) {
                    Predicate predicate = criteriaBuilder.equal(keyExpression(root, availableFilters.key()), saasFilter.search());
                    searchPredicates.add(predicate);
                }
            }

            predicates.add(criteriaBuilder.or(searchPredicates.toArray(Predicate[]::new)));
        }

        for (Filters.Filter filter : saasFilter.filters()) {
            boolean wasFound = false;
            for (AvailableFilters availableFilters : validKeys) {
                if (availableFilters.key().equals(filter.key()) && availableFilters.types().contains(filter.type())) {
                    wasFound = true;
                    break;
                }
            }

            if (!wasFound) {
                throw new InvalidFilterKeysException(validKeys);
            }

            switch (filter.type()) {
                case EXACT -> predicates.add(filterExact(root, criteriaBuilder, filter));
                case NOT_EXACT -> predicates.add(filterNotExact(root, criteriaBuilder, filter));
                case CONTAINS -> predicates.add(filterContains(root, criteriaBuilder, filter));
                case NOT_CONTAINS -> predicates.add(filterNotContains(root, criteriaBuilder, filter));
                case GREATER_THAN_DATE -> predicates.add(filterGreaterThanDate(root, criteriaBuilder, filter));
                case LESS_THAN_DATE -> predicates.add(filterLessThanDate(root, criteriaBuilder, filter));
                case BETWEEN_DATES -> predicates.add(filterBetweenDates(root, criteriaBuilder, filter));
                case EQUALS_DATE -> predicates.add(filterExactDate(root, criteriaBuilder, filter));
                default -> throw new IllegalArgumentException("Invalid filter type: " + filter.type());
            }
        }

        Predicate predicate = criteriaBuilder.and(predicates.toArray(Predicate[]::new));

        // If the 'criteriaQuery' is not null, apply 'where' and 'order by' to the query
        if (criteriaQuery != null) {
            List<Order> orders = new ArrayList<>();
            Filters.Sort sort = saasFilter.sort();

            if (sort != null) {
                boolean wasFound = false;
                for (AvailableFilters availableFilters : validKeys) {
                    if (availableFilters.key().equals(sort.key())) {
                        wasFound = true;
                        break;
                    }
                }

                if (!wasFound) {
                    throw new InvalidFilterKeysException(validKeys);
                }

                orders.add(sort.order() == Filters.SortOrder.ASC ? criteriaBuilder.asc(keyExpression(root, sort.key())) :
                        criteriaBuilder.desc(root.get(sort.key())));
            }

            // Apply 'where' and 'order by' to the query
            criteriaQuery.where(predicate);
            if (!orders.isEmpty()) {
                criteriaQuery.orderBy(orders);
            }
        }

        return predicate;
    }

    private static Predicate filterExact(Root<?> root, CriteriaBuilder criteriaBuilder, Filters.Filter filter) {
        if (NUMBER_REGEX.matcher(filter.value()).matches()) {
            return criteriaBuilder.equal(keyExpression(root, filter.key()), Long.parseLong(filter.value()));
        } else {
            if (filter.ignoreCase()) {
                return criteriaBuilder.equal(criteriaBuilder.lower(keyExpression(root, filter.key())), filter.value().toLowerCase());
            } else {
                return criteriaBuilder.equal(keyExpression(root, filter.key()), filter.value());
            }
        }
    }

    private static Predicate filterNotExact(Root<?> root, CriteriaBuilder criteriaBuilder, Filters.Filter filter) {
        if (NUMBER_REGEX.matcher(filter.value()).matches()) {
            return criteriaBuilder.notEqual(keyExpression(root, filter.key()), Long.parseLong(filter.value()));
        } else {
            if (filter.ignoreCase()) {
                return criteriaBuilder.notEqual(criteriaBuilder.lower(keyExpression(root, filter.key())), filter.value().toLowerCase());
            } else {
                return criteriaBuilder.notEqual(keyExpression(root, filter.key()), filter.value());
            }
        }
    }

    private static Predicate filterContains(Root<?> root, CriteriaBuilder criteriaBuilder, Filters.Filter filter) {
        if (filter.ignoreCase()) {
            return criteriaBuilder.like(criteriaBuilder.lower(keyExpression(root, filter.key())), '%' + filter.value().toLowerCase() + '%');
        } else {
            return criteriaBuilder.like(keyExpression(root, filter.key()), '%' + filter.value() + '%');
        }
    }

    private static Predicate filterNotContains(Root<?> root, CriteriaBuilder criteriaBuilder, Filters.Filter filter) {
        if (filter.ignoreCase()) {
            return criteriaBuilder.notLike(criteriaBuilder.lower(keyExpression(root, filter.key())), '%' + filter.value().toLowerCase() + '%');
        } else {
            return criteriaBuilder.notLike(keyExpression(root, filter.key()), '%' + filter.value() + '%');
        }
    }

    private static Predicate filterLessThanDate(Root<?> root, CriteriaBuilder criteriaBuilder, Filters.Filter filter) {
        return criteriaBuilder.lessThan(keyExpression(root, filter.key()), new Date(Long.parseLong(filter.value())));
    }

    private static Predicate filterGreaterThanDate(Root<?> root, CriteriaBuilder criteriaBuilder, Filters.Filter filter) {
        return criteriaBuilder.greaterThan(keyExpression(root, filter.key()), new Date(Long.parseLong(filter.value())));
    }

    private static Predicate filterBetweenDates(Root<?> root, CriteriaBuilder criteriaBuilder, Filters.Filter filter) {
        String[] dayRange = HYPHEN_REGEX.split(filter.value());
        if (dayRange.length != 2) {
            throw new IllegalArgumentException("Invalid date range: " + filter.value() + " for key: " + filter.key());
        }

        long start;
        long end;
        try {
            start = Long.parseLong(dayRange[0]);
            end = Long.parseLong(dayRange[1]);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid date range: " + filter.value() + " for key: " + filter.key());
        }

        LocalDateTime startDateTime = Instant.ofEpochMilli(start).atZone(ZoneId.systemDefault()).toLocalDateTime();
        LocalDateTime endDateTime = Instant.ofEpochMilli(end).atZone(ZoneId.systemDefault()).toLocalDateTime();

        Date startDate = Date.from(startDateTime.atZone(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(endDateTime.atZone(ZoneId.systemDefault()).toInstant());

        return criteriaBuilder.between(keyExpression(root, filter.key()), startDate, endDate);
    }

    private static Predicate filterExactDate(Root<?> root, CriteriaBuilder criteriaBuilder, Filters.Filter filter) {
        Instant filterInstant = Instant.ofEpochMilli(Long.parseLong(filter.value()));

        // Convert the Instant to the appropriate time zone (you may need to adjust the ZoneId)
        ZoneId zoneId = ZoneId.systemDefault();
        ZonedDateTime zonedDateTime = filterInstant.atZone(zoneId);

        // Calculate the start and end of the day in the given time zone
        LocalDateTime startOfDay = zonedDateTime.toLocalDate().atStartOfDay();
        ZonedDateTime startOfDayInZone = startOfDay.atZone(zoneId);
        ZonedDateTime endOfDayInZone = startOfDayInZone.plusDays(1).minusSeconds(1);

        // Convert to Instant for database comparison
        Instant startInstant = startOfDayInZone.toInstant();
        Instant endInstant = endOfDayInZone.toInstant();

        return criteriaBuilder.between(keyExpression(root, filter.key()), startInstant, endInstant);
    }

    private static <T> Expression<T> keyExpression(Path<?> root, String key) {
        String[] splitKeys = DOT_REGEX.split(key);
        Path<T> expression = root.get(splitKeys[0]);

        // If we have nested properties, concatenate them into a single expression
        for (int i = 1; i < splitKeys.length; i++) {
            expression = expression.get(splitKeys[i]);
        }

        return expression;
    }

    private static final Pattern NUMBER_REGEX = Pattern.compile("-?\\d+(\\.\\d+)?");
    private static final Pattern DOT_REGEX = Pattern.compile("\\.");
    private static final Pattern HYPHEN_REGEX = Pattern.compile("-");
}
