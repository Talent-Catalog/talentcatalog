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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import org.tctalent.server.repository.db.read.annotation.JsonOneToMany;
import org.tctalent.server.repository.db.read.annotation.JsonOneToOne;
import org.tctalent.server.repository.db.read.annotation.SqlColumn;
import org.tctalent.server.repository.db.read.annotation.SqlTable;


/**
 * TODO JC Doc
 *
 * @author John Cameron
 */
@Component
public class SqlJsonQueryBuilder {

    public String buildByIdsQuery(Class<?> rootDtoClass, String idsParamName) {
        SqlTable rootTable = require(rootDtoClass.getAnnotation(SqlTable.class),
            "Root DTO must have @SqlTable");

        String rootAlias = rootTable.alias();
        String rootTableName = rootTable.name();

        // parent scalar selects
        List<String> parentCols = new ArrayList<>();
        // json collection selects
        List<String> jsonSelects = new ArrayList<>();

        for (Field f : rootDtoClass.getDeclaredFields()) {
            SqlColumn col = f.getAnnotation(SqlColumn.class);
            if (col != null) {
                String outName = outColumnAlias(f, col);
                parentCols.add(rootAlias + "." + col.name() + " as " + outName);
                continue;
            }

            JsonOneToMany otm = f.getAnnotation(JsonOneToMany.class);
            if (otm != null) {
                jsonSelects.add(buildOneToManyJson(rootAlias, otm, f.getName()));
            }
        }

        if (parentCols.isEmpty()) {
            throw new IllegalArgumentException("No @SqlColumn fields found on " + rootDtoClass.getName());
        }

        // Base CTE so the rest of the query can refer to b.<pk> cleanly
        // You can expand base to include more parent scalar columns if you want;
        // here we keep it minimal: select only ids then join back to root table.
        String sql = """
            with base as (
              select %s as id
              from %s %s
              where %s.id = any(:%s)
            )
            select
              %s
              %s
            from %s %s
            join base b on b.id = %s.id
            """.formatted(
            rootAlias + ".id",
            rootTableName, rootAlias,
            rootAlias, idsParamName,
            String.join(",\n  ", parentCols),
            jsonSelects.isEmpty() ? "" : ",\n  " + String.join(",\n  ", jsonSelects),
            rootTableName, rootAlias,
            rootAlias
        );

        return sql.strip();
    }

    private String buildOneToManyJson(String rootAlias, JsonOneToMany otm, String outputColumnName) {
        Class<?> elemType = otm.elementType();
        SqlTable childTableAnn = require(elemType.getAnnotation(SqlTable.class),
            "Element DTO must have @SqlTable: " + elemType.getName());

        String childTable = otm.table();
        String childAlias = otm.alias();

        // Build the JSON object for each child row (including nested 1-1 objects)
        String childJsonObject = buildJsonObjectForDto(elemType, childAlias);

        String orderBy = otm.orderBy().isBlank() ? "" : " order by " + otm.orderBy();

        // Correlated subquery: returns jsonb array for this parent
        // IMPORTANT: Note the spaces around '::' to avoid Spring named-param parsing issues.
        return """
            coalesce((
              select jsonb_agg(%s%s)
              from %s %s
              %s
              where %s.%s = %s.id
            ), '[]' :: jsonb) as %s
            """.formatted(
            childJsonObject,
            orderBy,
            childTable, childAlias,
            buildChildJoins(elemType, childAlias),
            childAlias, otm.fkColumn(), rootAlias,
            outputColumnName
        ).strip();
    }

    /**
     * Builds:
     *   jsonb_build_object('id', cl.id, 'lang', cl.lang, 'certification', case when cert.id is null then null else jsonb_build_object(...) end)
     */
    private String buildJsonObjectForDto(Class<?> dtoType, String alias) {
        List<String> kvPairs = new ArrayList<>();

        for (Field f : dtoType.getDeclaredFields()) {
            SqlColumn col = f.getAnnotation(SqlColumn.class);
            if (col != null) {
                String key = jsonKey(f, col);
                kvPairs.add("'" + key + "', " + alias + "." + col.name());
                continue;
            }

            JsonOneToOne oto = f.getAnnotation(JsonOneToOne.class);
            if (oto != null) {
                String key = f.getName();
                kvPairs.add("'" + key + "', " + buildOneToOneJsonObject(oto));
            }
        }

        if (kvPairs.isEmpty()) {
            throw new IllegalArgumentException("No mappable fields found on " + dtoType.getName());
        }

        return "jsonb_build_object(" + String.join(", ", kvPairs) + ")";
    }

    private String buildChildJoins(Class<?> childDtoType, String childAlias) {
        // Collect LEFT JOINs for any @JsonOneToOne on child DTO
        List<String> joins = new ArrayList<>();
        for (Field f : childDtoType.getDeclaredFields()) {
            JsonOneToOne oto = f.getAnnotation(JsonOneToOne.class);
            if (oto != null) {
                String nestedAlias = oto.alias();
                joins.add("left join " + oto.table() + " " + nestedAlias +
                    " on " + oto.joinLeftColumn() + " = " + nestedAlias + "." + oto.joinRightColumn());
            }
        }
        return joins.isEmpty() ? "" : String.join("\n  ", joins);
    }

    private String buildOneToOneJsonObject(JsonOneToOne oto) {
        Class<?> targetType = oto.targetType();
        SqlTable nestedTableAnn = require(targetType.getAnnotation(SqlTable.class),
            "1-1 target DTO must have @SqlTable: " + targetType.getName());

        String nestedAlias = oto.alias();

        // build nested jsonb_build_object from target DTO columns
        List<String> nestedPairs = new ArrayList<>();
        for (Field nf : targetType.getDeclaredFields()) {
            SqlColumn col = nf.getAnnotation(SqlColumn.class);
            if (col != null) {
                nestedPairs.add("'" + jsonKey(nf, col) + "', " + nestedAlias + "." + col.name());
            }
        }

        String nestedObj = "jsonb_build_object(" + String.join(", ", nestedPairs) + ")";

        // Optional 1-1: return null when missing
        return "case when " + nestedAlias + ".id is null then null else " + nestedObj + " end";
    }

    private static String outColumnAlias(Field f, SqlColumn col) {
        // SQL output column alias (snake/camel doesn’t matter; you’ll map from ResultSet)
        // Use field name unless overridden in jsonKey (handy when you share mapping).
        return col.jsonKey().isBlank() ? f.getName() : col.jsonKey();
    }

    private static String jsonKey(Field f, SqlColumn col) {
        return col.jsonKey().isBlank() ? f.getName() : col.jsonKey();
    }

    private static <T> T require(T value, String message) {
        if (value == null) throw new IllegalArgumentException(message);
        return value;
    }
}
