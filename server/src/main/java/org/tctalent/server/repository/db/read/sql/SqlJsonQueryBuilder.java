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
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import org.tctalent.server.repository.db.read.annotation.JsonOneToMany;
import org.tctalent.server.repository.db.read.annotation.JsonOneToOne;
import org.tctalent.server.repository.db.read.annotation.SqlColumn;
import org.tctalent.server.repository.db.read.annotation.SqlTable;

/**
 * Builds Postgres SQL that returns:
 *  - one row per root DTO
 *  - JSON arrays for @JsonOneToMany
 *  - JSON objects for @JsonOneToOne
 *
 * Default behaviour:
 *  - any non-annotated, non-static field is treated as a scalar column
 *  - column name = camelToSnakeCase(fieldName)
 *  - JSON key/output alias = fieldName (unless @SqlColumn.jsonKey overrides)
 */
@Component
public class SqlJsonQueryBuilder {

    /* ==========================================================
       Public API
       ========================================================== */

    public String buildByIdsQuery(Class<?> rootDtoClass, String idsParamName) {

        SqlTable rootTable = require(
            rootDtoClass.getAnnotation(SqlTable.class),
            "Root DTO must have @SqlTable"
        );

        String rootTableName = rootTable.name();
        String rootAlias = rootTable.alias();

        List<String> scalarSelects = new ArrayList<>();
        List<String> jsonSelects = new ArrayList<>();

        for (Field field : rootDtoClass.getDeclaredFields()) {
            if (shouldIgnore(field)) continue;

            // Relationship annotations take precedence
            JsonOneToMany otm = field.getAnnotation(JsonOneToMany.class);
            if (otm != null) {
                jsonSelects.add(buildOneToMany(rootAlias, otm, field.getName()));
                continue;
            }

            JsonOneToOne oto = field.getAnnotation(JsonOneToOne.class);
            if (oto != null) {
                jsonSelects.add(buildRootOneToOne(oto, field.getName()));
                continue;
            }

            // Scalar column: explicit @SqlColumn OR default
            SqlColumn col = field.getAnnotation(SqlColumn.class);
            if (col == null) {
                //TODO JC For now only process annotated fields
                continue;
            }
            String dbColumn = (col != null) ? col.name() : camelToSnakeCase(field.getName());
            String outAlias = outputName(field, col);

            scalarSelects.add(rootAlias + "." + dbColumn + " as " + outAlias);
        }

        if (scalarSelects.isEmpty()) {
            throw new IllegalStateException("Root DTO has no scalar fields to select");
        }

        return """
            with base as (
              select %s.id
              from %s %s
              where %s.id in (:%s)
            )
            select
              %s
              %s
            from %s %s
            join base b on b.id = %s.id
            """.formatted(
            rootAlias,
            rootTableName, rootAlias,
            rootAlias, idsParamName,
            String.join(",\n  ", scalarSelects),
            jsonSelects.isEmpty() ? "" : ",\n  " + String.join(",\n  ", jsonSelects),
            rootTableName, rootAlias,
            rootAlias
        ).strip();
    }

    /* ==========================================================
       Root 1-to-1
       ========================================================== */

    private String buildRootOneToOne(JsonOneToOne oto, String outputColumn) {
        String nestedAlias = oto.alias();
        String jsonObject = buildJsonObjectForDto(oto.targetType(), nestedAlias);

        return """
            (
              select %s
              from %s %s
              where %s = %s.%s
            ) as %s
            """.formatted(
            jsonObject,
            oto.table(), nestedAlias,
            oto.joinLeftColumn(),
            nestedAlias, oto.joinRightColumn(),
            outputColumn
        ).strip();
    }

    /* ==========================================================
       Root 1-to-Many
       ========================================================== */

    private String buildOneToMany(String rootAlias, JsonOneToMany otm, String outputColumn) {
        Class<?> elementType = otm.elementType();
        String childAlias = otm.alias();

        String elementJson = buildJsonObjectForDto(elementType, childAlias);
        String joins = buildNestedJoins(elementType);

        String orderBy = otm.orderBy().isBlank() ? "" : " order by " + otm.orderBy();

        return """
            coalesce((
              select jsonb_agg(%s%s)
              from %s %s
              %s
              where %s.%s = %s.id
            ), '[]' :: jsonb) as %s
            """.formatted(
            elementJson,
            orderBy,
            otm.table(), childAlias,
            joins,
            childAlias, otm.fkColumn(),
            rootAlias,
            outputColumn
        ).strip();
    }

    /* ==========================================================
       JSON object builders
       ========================================================== */

    private String buildJsonObjectForDto(Class<?> dtoType, String alias) {
        List<String> kv = new ArrayList<>();

        for (Field f : dtoType.getDeclaredFields()) {
            if (shouldIgnore(f)) continue;

            // Nested relationships inside the JSON object
            JsonOneToOne oto = f.getAnnotation(JsonOneToOne.class);
            if (oto != null) {
                kv.add("'" + f.getName() + "', " + buildNestedOneToOne(oto));
                continue;
            }

            // Scalar value: explicit @SqlColumn OR default
            SqlColumn col = f.getAnnotation(SqlColumn.class);
            if (col == null) {
                //TODO JC Don't process unnotated columns
                continue;
            }
            String dbColumn = (col != null) ? col.name() : camelToSnakeCase(f.getName());
            String key = jsonKey(f, col);

            kv.add("'" + key + "', " + alias + "." + dbColumn);
        }

        if (kv.isEmpty()) {
            throw new IllegalStateException("DTO " + dtoType.getName() + " has no mappable fields");
        }

        return "jsonb_build_object(" + String.join(", ", kv) + ")";
    }

    private String buildNestedOneToOne(JsonOneToOne oto) {
        String nestedAlias = oto.alias();
        String nestedJson = buildJsonObjectForDto(oto.targetType(), nestedAlias);

        return """
            case
              when %s.%s is null then null
              else %s
            end
            """.formatted(
            nestedAlias, oto.joinRightColumn(),
            nestedJson
        ).strip();
    }

    /* ==========================================================
       JOIN builders (for nested 1-1 inside 1-many)
       ========================================================== */

    private String buildNestedJoins(Class<?> elementType) {
        List<String> joins = new ArrayList<>();

        for (Field f : elementType.getDeclaredFields()) {
            if (shouldIgnore(f)) continue;

            JsonOneToOne oto = f.getAnnotation(JsonOneToOne.class);
            if (oto != null) {
                joins.add(
                    "left join " + oto.table() + " " + oto.alias() +
                        " on " + oto.joinLeftColumn() +
                        " = " + oto.alias() + "." + oto.joinRightColumn()
                );
            }
        }

        return joins.isEmpty() ? "" : String.join("\n  ", joins);
    }

    /* ==========================================================
       Helpers
       ========================================================== */

    private static boolean shouldIgnore(Field f) {
        // ignore constants/loggers/etc; include private fields (normal DTO style)
        int m = f.getModifiers();
        return Modifier.isStatic(m) || Modifier.isTransient(m);
    }

    private static String outputName(Field f, SqlColumn col) {
        if (col == null) return f.getName();
        return col.jsonKey().isBlank() ? f.getName() : col.jsonKey();
    }

    private static String jsonKey(Field f, SqlColumn col) {
        if (col == null) return f.getName();
        return col.jsonKey().isBlank() ? f.getName() : col.jsonKey();
    }

    private static <T> T require(T value, String message) {
        if (value == null) throw new IllegalStateException(message);
        return value;
    }
}
