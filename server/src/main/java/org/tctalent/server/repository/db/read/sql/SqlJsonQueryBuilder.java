/*
 * Copyright (c) 2025 Talent Catalog.
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
package org.tctalent.server.repository.db.read.sql;

import static org.tctalent.server.util.RegexHelpers.camelToSnakeCase;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import org.tctalent.server.repository.db.read.annotation.JsonOneToMany;
import org.tctalent.server.repository.db.read.annotation.JsonOneToOne;
import org.tctalent.server.repository.db.read.annotation.SqlColumn;
import org.tctalent.server.repository.db.read.annotation.SqlDefaults;
import org.tctalent.server.repository.db.read.annotation.SqlIgnore;
import org.tctalent.server.repository.db.read.annotation.SqlTable;

@Component
public class SqlJsonQueryBuilder {

    /* ==========================================================
       Public entry point
       ========================================================== */

    public String buildByIdsQuery(Class<?> rootDtoClass, String idsParamName) {

        SqlTable rootTable = require(
            rootDtoClass.getAnnotation(SqlTable.class),
            "Root DTO must have @SqlTable"
        );

        String rootAlias = rootTable.alias();

        String rootJson = buildJsonObject(
            rootDtoClass,
            rootAlias,
            rootAlias + ".id"
        );

        return """
            select
              %s.id,
              %s as data
            from %s %s
            where %s.id in (:%s)
            """.formatted(
            rootAlias,
            rootJson,
            rootTable.name(), rootAlias,
            rootAlias, idsParamName
        ).strip();
    }

    /* ==========================================================
       Recursive JSON builder
       ========================================================== */

    private String buildJsonObject(
        Class<?> dtoType,
        String tableAlias,
        String idExpression
    ) {
        boolean mapDefaults = mapUnannotated(dtoType);
        List<String> fields = new ArrayList<>();

        for (Field f : dtoType.getDeclaredFields()) {
            if (ignore(f)) continue;

            /* ---------- one-to-many ---------- */
            JsonOneToMany otm = f.getAnnotation(JsonOneToMany.class);
            if (otm != null) {

                Class<?> childType = resolveOneToManyType(f, otm);
                SqlTable childTable = childType.getAnnotation(SqlTable.class);

                String childAlias = childTable.alias();

                String childJson = buildJsonObject(
                    childType,
                    childAlias,
                    childAlias + ".id"
                );

                fields.add("""
                    '%s', coalesce((
                        select jsonb_agg(%s%s)
                        from %s %s
                        where %s.%s = %s
                    ), '[]' :: jsonb)
                    """.formatted(
                    f.getName(),
                    childJson,
                    otm.orderBy().isBlank() ? "" : " order by " + otm.orderBy(),
                    childTable.name(), childAlias,
                    childAlias, otm.joinColumn(),
                    idExpression
                ).strip());

                continue;
            }

            /* ---------- one-to-one ---------- */
            JsonOneToOne oto = f.getAnnotation(JsonOneToOne.class);
            if (oto != null) {

                Class<?> targetType = resolveOneToOneType(f, oto);
                SqlTable targetTable = targetType.getAnnotation(SqlTable.class);

                String targetAlias = targetTable.alias();

                String nestedJson = buildJsonObject(
                    targetType,
                    targetAlias,
                    targetAlias + "." + oto.joinRightColumn()
                );

                fields.add("""
                    '%s', (
                        select %s
                        from %s %s
                        where %s = %s.%s
                    )
                    """.formatted(
                    f.getName(),
                    nestedJson,
                    targetTable.name(), targetAlias,
                    oto.joinLeftColumn(),
                    targetAlias, oto.joinRightColumn()
                ).strip());

                continue;
            }

            /* ---------- scalar ---------- */
            SqlColumn col = f.getAnnotation(SqlColumn.class);
            if (col == null && !mapDefaults) continue;

            String dbColumn = (col != null)
                ? col.name()
                : camelToSnakeCase(f.getName());

            String jsonKey = (col != null && !col.jsonKey().isBlank())
                ? col.jsonKey()
                : f.getName();

            fields.add("'" + jsonKey + "', " + tableAlias + "." + dbColumn);
        }

        if (fields.isEmpty()) {
            throw new IllegalStateException(
                "DTO " + dtoType.getName() + " has no mapped fields"
            );
        }

        return "jsonb_build_object(" + String.join(", ", fields) + ")";
    }

    /* ==========================================================
       Type resolution (safe deduction)
       ========================================================== */

    private Class<?> resolveOneToOneType(Field field, JsonOneToOne oto) {
        if (oto.type() != Void.class) {
            return validateConcreteType(oto.type(), field);
        }
        return validateConcreteType(field.getType(), field);
    }

    private Class<?> resolveOneToManyType(Field field, JsonOneToMany otm) {
        if (otm.type() != Void.class) {
            return validateConcreteType(otm.type(), field);
        }

        Type generic = field.getGenericType();
        if (!(generic instanceof ParameterizedType pt)) {
            throw new IllegalStateException(
                "@JsonOneToMany field must be parameterized: " + field
            );
        }

        Type arg = pt.getActualTypeArguments()[0];
        if (!(arg instanceof Class<?> clazz)) {
            throw new IllegalStateException(
                "Cannot determine element type for " + field
            );
        }

        return validateConcreteType(clazz, field);
    }

    private Class<?> validateConcreteType(Class<?> type, Field field) {

        if (type.isInterface()) {
            throw new IllegalStateException(
                "Type " + type.getName() +
                    " on field " + field.getName() +
                    " is an interface"
            );
        }

        if (Modifier.isAbstract(type.getModifiers())) {
            throw new IllegalStateException(
                "Type " + type.getName() +
                    " on field " + field.getName() +
                    " is abstract"
            );
        }

        if (type.getAnnotation(SqlTable.class) == null) {
            throw new IllegalStateException(
                "Type " + type.getName() +
                    " on field " + field.getName() +
                    " is missing @SqlTable"
            );
        }

        return type;
    }

    /* ==========================================================
       Helpers
       ========================================================== */

    private static boolean mapUnannotated(Class<?> type) {
        SqlDefaults d = type.getAnnotation(SqlDefaults.class);
        return d != null && d.mapUnannotatedColumns();
    }

    private static boolean ignore(Field f) {
        int m = f.getModifiers();
        return Modifier.isStatic(m)
            || Modifier.isTransient(m)
            || f.isAnnotationPresent(SqlIgnore.class);
    }

    private static <T> T require(T value, String msg) {
        if (value == null) throw new IllegalStateException(msg);
        return value;
    }
}
