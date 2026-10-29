/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.integration.repo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.tctalent.server.integration.helper.BaseDBIntegrationTest;
import org.tctalent.server.repository.db.read.cache.CandidateRedisCache;

/**
 * Runs every read-only query method of our Spring Data repositories against the database, with
 * representative arguments, and records whether it succeeds or fails.
 * <p>
 * JPQL is validated when the application starts, but native SQL, parameter binding and result
 * mapping are only checked when a query runs. The expected outcomes
 * (repository/repository-query-outcomes.txt) were recorded with Spring Boot 3.5.16
 * (Hibernate 6.6, Spring Data JPA 3.5) before the Spring Boot 4 upgrade - TC-1189.
 * </p>
 * <p>
 * Set TC_REPOSITORY_QUERIES_OUT to a file path to write the actual outcomes instead. Setting
 * TC_REPOSITORY_QUERIES_DETAIL as well also records the types of the results (which depend on
 * the data), and error messages.
 * </p>
 */
@SpringBootTest
class RepositoryQueryCompatibilityIntegrationTest extends BaseDBIntegrationTest {

    private static final String EXPECTED = "repository/repository-query-outcomes.txt";

    private static final List<String> MODIFYING_PREFIXES = List.of(
        "save", "delete", "remove", "update", "insert", "set", "increment", "decrement", "mark",
        "clear", "reset", "archive", "add", "create", "upsert", "purge", "refresh", "evict",
        "flush", "lock", "touch", "apply");

    private static final OffsetDateTime DATE_TIME =
        OffsetDateTime.of(2025, 4, 15, 10, 30, 0, 0, ZoneOffset.UTC);

    @MockitoBean
    private CandidateRedisCache candidateRedisCache;

    @Autowired private ApplicationContext applicationContext;
    @Autowired private EntityManager entityManager;
    @Autowired private PlatformTransactionManager transactionManager;

    private final boolean detail = System.getenv("TC_REPOSITORY_QUERIES_DETAIL") != null;

    @Test
    @DisplayName("every read-only repository query runs as before the Spring Boot 4 upgrade")
    void repositoryQueries() throws Exception {
        TreeMap<String, String> outcomes = new TreeMap<>();
        for (Object repository : applicationContext.getBeansOfType(Repository.class).values()) {
            for (Class<?> repositoryInterface : repositoryInterfaces(repository)) {
                for (Method method : repositoryInterface.getDeclaredMethods()) {
                    if (isReadOnlyQuery(method)) {
                        outcomes.put(signature(repositoryInterface, method), run(repository, method));
                    }
                }
            }
        }

        StringBuilder actual = new StringBuilder();
        outcomes.forEach((method, outcome) ->
            actual.append(method).append(" -> ").append(outcome).append('\n'));

        String out = System.getenv("TC_REPOSITORY_QUERIES_OUT");
        if (out != null) {
            Files.writeString(Path.of(out), actual.toString());
            return;
        }
        String expected = new ClassPathResource(EXPECTED).getContentAsString(StandardCharsets.UTF_8);
        assertEquals(expected, actual.toString());
    }

    private String run(Object repository, Method method) {
        Object[] args;
        try {
            args = arguments(method);
        } catch (UnsupportedOperationException e) {
            return "SKIPPED " + e.getMessage();
        }
        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        try {
            return tx.execute(status -> {
                status.setRollbackOnly();
                try {
                    Object result = method.invoke(repository, args);
                    return "OK" + (detail ? " " + describe(result) : "");
                } catch (InvocationTargetException e) {
                    return error(e.getCause());
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException(e);
                }
            });
        } catch (RuntimeException e) {
            return error(e);
        }
    }

    private String error(Throwable e) {
        Throwable root = e;
        while (root.getCause() != null && root.getCause() != root) {
            root = root.getCause();
        }
        String s = "ERROR " + e.getClass().getSimpleName() + " (" + root.getClass().getSimpleName()
            + ")";
        if (detail) {
            String message = String.valueOf(root.getMessage()).replaceAll("\\s+", " ");
            s += ": " + message.substring(0, Math.min(200, message.length()));
        }
        return s;
    }

    private static String describe(Object result) {
        if (result == null) {
            return "null";
        }
        if (result instanceof Optional<?> optional) {
            return "Optional[" + optional.map(RepositoryQueryCompatibilityIntegrationTest::describe)
                .orElse("empty") + "]";
        }
        if (result instanceof Stream<?> stream) {
            try (stream) {
                return "Stream" + describeElements(stream.limit(3).toList());
            }
        }
        if (result instanceof Slice<?> slice) {
            return "Page" + describeElements(slice.getContent());
        }
        if (result instanceof Collection<?> collection) {
            return "Collection" + describeElements(collection);
        }
        if (result instanceof Object[] array) {
            return "Object[" + Arrays.stream(array)
                .map(o -> o == null ? "null" : o.getClass().getSimpleName())
                .reduce((a, b) -> a + "," + b).orElse("") + "]";
        }
        return result.getClass().getSimpleName().replaceAll("\\$.*", "");
    }

    private static String describeElements(Collection<?> elements) {
        return elements.isEmpty() ? "(empty)" : "(" + describe(elements.iterator().next()) + ")";
    }

    /** Our repository interfaces implemented by the repository proxy */
    private static List<Class<?>> repositoryInterfaces(Object repository) {
        List<Class<?>> interfaces = new ArrayList<>();
        for (Class<?> i : AopUtils.getTargetClass(repository).getInterfaces()) {
            if (i.getName().startsWith("org.tctalent.") && Repository.class.isAssignableFrom(i)) {
                interfaces.add(i);
            }
        }
        for (Class<?> i : repository.getClass().getInterfaces()) {
            if (i.getName().startsWith("org.tctalent.") && Repository.class.isAssignableFrom(i)
                && !interfaces.contains(i)) {
                interfaces.add(i);
            }
        }
        return interfaces;
    }

    private static boolean isReadOnlyQuery(Method method) {
        if (method.isDefault() || Modifier.isStatic(method.getModifiers()) || method.isSynthetic()
            || method.isBridge() || method.isAnnotationPresent(Modifying.class)) {
            return false;
        }
        Query query = method.getAnnotation(Query.class);
        if (query != null) {
            String q = query.value().trim().toLowerCase();
            if (q.startsWith("update") || q.startsWith("delete") || q.startsWith("insert")) {
                return false;
            }
        }
        String name = method.getName().toLowerCase();
        return MODIFYING_PREFIXES.stream().noneMatch(name::startsWith);
    }

    private static String signature(Class<?> repositoryInterface, Method method) {
        return repositoryInterface.getSimpleName() + "." + method.getName() + "("
            + Arrays.stream(method.getParameterTypes()).map(Class::getSimpleName)
                .reduce((a, b) -> a + "," + b).orElse("") + ")";
    }

    private Object[] arguments(Method method) {
        Parameter[] parameters = method.getParameters();
        Object[] args = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            args[i] = value(ResolvableType.forMethodParameter(method, i));
        }
        return args;
    }

    private Object value(ResolvableType type) {
        Class<?> c = type.resolve(Object.class);
        if (c == Long.class || c == long.class) {
            return 1L;
        }
        if (c == Integer.class || c == int.class) {
            return 1;
        }
        if (c == Short.class || c == short.class) {
            return (short) 1;
        }
        if (c == Double.class || c == double.class) {
            return 1.0;
        }
        if (c == Boolean.class || c == boolean.class) {
            return false;
        }
        if (c == String.class) {
            return "tc1189";
        }
        if (c.isEnum()) {
            return c.getEnumConstants()[0];
        }
        if (c == OffsetDateTime.class) {
            return DATE_TIME;
        }
        if (c == LocalDateTime.class) {
            return DATE_TIME.toLocalDateTime();
        }
        if (c == LocalDate.class) {
            return DATE_TIME.toLocalDate();
        }
        if (c == Instant.class) {
            return DATE_TIME.toInstant();
        }
        if (c == Date.class) {
            return Date.from(DATE_TIME.toInstant());
        }
        if (c == Pageable.class) {
            return PageRequest.of(0, 2);
        }
        if (c == Sort.class) {
            return Sort.unsorted();
        }
        if (c == List.class || c == Collection.class || c == Iterable.class) {
            return List.of(value(type.getGeneric(0)));
        }
        if (c == Set.class) {
            return Set.of(value(type.getGeneric(0)));
        }
        if (c.isAnnotationPresent(Entity.class)) {
            return anyEntity(c);
        }
        throw new UnsupportedOperationException("parameter type " + c.getSimpleName());
    }

    /** Any existing entity of the given type, or null if there are none */
    private Object anyEntity(Class<?> entityClass) {
        try {
            List<?> entities = entityManager
                .createQuery("select e from " + entityClass.getName() + " e order by e.id")
                .setMaxResults(1)
                .getResultList();
            return entities.isEmpty() ? null : entities.get(0);
        } catch (RuntimeException e) {
            return null;
        }
    }
}
