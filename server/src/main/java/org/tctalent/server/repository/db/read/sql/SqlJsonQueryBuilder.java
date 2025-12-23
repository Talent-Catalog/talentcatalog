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
import org.tctalent.server.repository.db.read.annotation.SqlDefaults;
import org.tctalent.server.repository.db.read.annotation.SqlTable;

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

        String rootAlias = rootTable.alias();
        String rootTableName = rootTable.name();

        String rootJson = buildJsonForDto(
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
            rootTableName, rootAlias,
            rootAlias, idsParamName
        ).strip();
    }

    /* ==========================================================
       Recursive JSON builder
       ========================================================== */

    private String buildJsonForDto(
        Class<?> dtoType,
        String tableAlias,
        String idExpression
    ) {
        boolean mapDefaults = shouldMapUnannotated(dtoType);

        List<String> kv = new ArrayList<>();

        for (Field f : dtoType.getDeclaredFields()) {
            if (shouldIgnore(f)) continue;

            // 1️⃣ One-to-many (JSON array)
            JsonOneToMany otm = f.getAnnotation(JsonOneToMany.class);
            if (otm != null) {
                String childAlias = otm.alias();

                String childJson = buildJsonForDto(
                    otm.elementType(),
                    childAlias,
                    childAlias + ".id"
                );

                kv.add("""
                    '%s', coalesce((
                        select jsonb_agg(%s%s)
                        from %s %s
                        where %s.%s = %s
                    ), '[]' :: jsonb)
                    """.formatted(
                    f.getName(),
                    childJson,
                    otm.orderBy().isBlank() ? "" : " order by " + otm.orderBy(),
                    otm.table(), childAlias,
                    childAlias, otm.fkColumn(),
                    idExpression
                ).strip());

                continue;
            }

            // 2️⃣ One-to-one (JSON object)
            JsonOneToOne oto = f.getAnnotation(JsonOneToOne.class);
            if (oto != null) {
                String nestedAlias = oto.alias();

                String nestedJson = buildJsonForDto(
                    oto.targetType(),
                    nestedAlias,
                    nestedAlias + "." + oto.joinRightColumn()
                );

                kv.add("""
                    '%s', (
                        select %s
                        from %s %s
                        where %s = %s.%s
                    )
                    """.formatted(
                    f.getName(),
                    nestedJson,
                    oto.table(), nestedAlias,
                    oto.joinLeftColumn(),
                    nestedAlias, oto.joinRightColumn()
                ).strip());

                continue;
            }

            // 3️⃣ Scalar column: explicit OR (optional) default
            SqlColumn col = f.getAnnotation(SqlColumn.class);
            if (col == null && !mapDefaults) {
                // Skip unannotated scalar fields if defaults are disabled for this DTO
                continue;
            }

            String dbColumn = (col != null)
                ? col.name()
                : camelToSnakeCase(f.getName());

            String jsonKey = (col != null && !col.jsonKey().isBlank())
                ? col.jsonKey()
                : f.getName();

            kv.add("'" + jsonKey + "', " + tableAlias + "." + dbColumn);
        }

        if (kv.isEmpty()) {
            throw new IllegalStateException(
                "DTO " + dtoType.getName() + " has no mappable fields. " +
                    "Either add @SqlDefaults(mapUnannotatedColumns=true) or annotate fields."
            );
        }

        return "jsonb_build_object(" + String.join(", ", kv) + ")";
    }

    private static boolean shouldMapUnannotated(Class<?> dtoType) {
        SqlDefaults defaults = dtoType.getAnnotation(SqlDefaults.class);
        return defaults != null && defaults.mapUnannotatedColumns();
    }

    /* ==========================================================
       Helpers
       ========================================================== */

    private static boolean shouldIgnore(Field f) {
        int m = f.getModifiers();
        return Modifier.isStatic(m) || Modifier.isTransient(m);
    }

    private static <T> T require(T value, String message) {
        if (value == null) throw new IllegalStateException(message);
        return value;
    }
}


