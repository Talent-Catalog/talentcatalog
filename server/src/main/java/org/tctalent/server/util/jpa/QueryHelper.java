/*
 * Copyright (c) 2024 Talent Beyond Boundaries.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.util.jpa;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import jakarta.persistence.Query;
import jakarta.persistence.Tuple;
import jakarta.persistence.TupleElement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * Utilities for JPA queries.
 *
 * @author John Cameron
 */
@Service
public class QueryHelper {

    private final ObjectMapper objectMapper;

    public QueryHelper() {
        objectMapper = new ObjectMapper()
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
    }

    /**
     * Converts the results of a JPA query into a List of objects belonging to the given class.
     * <p/>
     * Note that the JPA query must have been created to return {@link Tuple}'s.
     * <p/>
     * Sample Use:
     * <pre>
     *   public static class MyClass {
     *         private String param1;
     *         private Integer param2;
     *         private OffsetDateTime param3;
     *   }
     *  ...
     *   Query query = entityManager.createNativeQuery("SELECT param1, param2 param3 ... ", Tuple.class);
     *   List&lt;MyClass&gt; results = parseTupleResult(query.getResultList(), MyClass.class);
     *
     * </pre>
     *
     * @param queryResultList Results returned by a call to {@link Query#getResultList()}
     * @param clz             Class of objects used to store the results
     * @return List of instances belonging to the requested class
     * @throws RuntimeException if the results were not from a query created to return Tuples.
     */
    public <T> List<T> parseTupleResult(List<?> queryResultList, Class<T> clz) {
        List<T> result = new ArrayList<>();
        convertTuplesToMap(queryResultList).forEach(map -> {
            result.add(objectMapper.convertValue(map, clz));
        });
        return result;
    }

    /**
     * Converts list of tuples to a list of maps.
     * <p/>
     * Taken from
     * <a href="https://stackoverflow.com/questions/29082749/spring-data-jpa-map-the-native-query-result-to-non-entity-pojo">
     *     Stackoverflow</a>
     * @param tuples List of Tuple
     * @return List of Map
     */
    private List<Map<String, Object>> convertTuplesToMap(List<?> tuples) {
        List<Map<String, Object>> result = new ArrayList<>();

        tuples.forEach(object -> {
            if (object instanceof Tuple row) {
                Map<String, Object> tempMap = new HashMap<>();
                for (TupleElement<?> element : row.getElements()) {
                    tempMap.put(element.getAlias(), row.get(element));
                }
                result.add(tempMap);
            } else {
                throw new RuntimeException("Query should return instance of Tuple");
            }
        });

        return result;
    }
}
