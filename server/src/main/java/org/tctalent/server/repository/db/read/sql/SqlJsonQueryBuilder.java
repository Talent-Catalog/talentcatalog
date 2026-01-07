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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.tctalent.server.repository.db.read.annotation.JsonOneToMany;
import org.tctalent.server.repository.db.read.annotation.JsonOneToOne;
import org.tctalent.server.repository.db.read.annotation.SqlColumn;
import org.tctalent.server.repository.db.read.annotation.SqlDefaults;
import org.tctalent.server.repository.db.read.annotation.SqlIgnore;
import org.tctalent.server.repository.db.read.annotation.SqlTable;

/**
 * Generates SQL for fetching candidate data from the database encoded as a single String of JSON.
 * <p>
 *     The result set consists of one row per candidate. Each row contains:
 *     <ul>
 *         <li>id (long) - candidate id</li>
 *         <li>json (string) - candidate data encoded as a JSON object</li>*         
 *     </ul>
 * </p>
 * <p>
 *     The SQL is automatically generated from the candidate related dtos.
 *     The root candidate DTO is 
 *     {@link org.tctalent.server.repository.db.read.dto.CandidateReadDto CandidateReadDto}.
 *     It relates to the {@link org.tctalent.server.model.db.Candidate Candidate} JPA entity.
 * </p>
 * <p>
 *     CandidateReadDTO is constructed from other DTOs corresponding to the other equivalent 
 *     candidate related entities.
 * </p>
 * <p>
 *     Below is an example of what the generated SQL can look like. This is generated from a
 *     simplified CandidateReadDto in that a lot of standard fields have been removed.
 *     But it shows how nesting works - in that it encodes a Candidate, including its nested User,
 *     which in turn has a nested Partner.*     
 * </p>
 * <p>
 *     It also shows how it constructs the encoding of the 1-many job experiences associated with 
 *     the candidate.
 * </p>
 * <pre>{@code
 * select
 *   c.id,
 *   jsonb_build_object('candidateNumber', c.candidate_number, 'user', (
 *     select jsonb_build_object('id', u.id, 'firstName', u.first_name, 'lastName', u.last_name, 'partnerReadDto', (
 *     select jsonb_build_object('id', p.id, 'abbreviation', p.abbreviation, 'name', p.name)
 *     from partner p
 *     where partner_id = p.id
 * ))
 *     from users u
 *     where user_id = u.id
 * ), 'createdBy', (
 *     select jsonb_build_object('id', u.id, 'firstName', u.first_name, 'lastName', u.last_name, 'partnerReadDto', (
 *     select jsonb_build_object('id', p.id, 'abbreviation', p.abbreviation, 'name', p.name)
 *     from partner p
 *     where partner_id = p.id
 * ))
 *     from users u
 *     where created_by = u.id
 * ), 'updatedBy', (
 *     select jsonb_build_object('id', u.id, 'firstName', u.first_name, 'lastName', u.last_name, 'partnerReadDto', (
 *     select jsonb_build_object('id', p.id, 'abbreviation', p.abbreviation, 'name', p.name)
 *     from partner p
 *     where partner_id = p.id
 * ))
 *     from users u
 *     where updated_by = u.id
 * ), 'candidateJobExperiences', coalesce((
 *     select jsonb_agg(jsonb_build_object('companyName', cje.company_name, 'role', cje.role))
 *     from candidate_job_experience cje
 *     where cje.candidate_id = c.id
 * ), '[]' :: jsonb)) as data
 * from candidate c
 * where c.id in (:ids)
 *  }
 *  </pre>
 * <p>
 * Builds PostgreSQL SQL that returns:
 * </p>
 *
 * <ul>
 *   <li><p>{@code id} (root table primary key)</p></li>
 *   <li><p>{@code json} (a {@code jsonb} object containing DTO fields)</p></li>
 * </ul>
 *
 * <p>
 * The builder supports:
 * </p>
 *
 * <ul>
 *   <li><p>Scalar columns (via {@code @SqlColumn} or optional default mapping)</p></li>
 *   <li><p>Nested one-to-one objects (via {@code @JsonOneToOne})</p></li>
 *   <li><p>Nested one-to-many arrays (via {@code @JsonOneToMany})</p></li>
 *   <li><p>Ignoring fields (via {@code @SqlIgnore})</p></li>
 * </ul>
 *
 * <p>
 * Notes:
 * </p>
 *
 * <ul>
 *   <li><p>Table/alias are sourced from {@code @SqlTable} on each DTO class.</p></li>
 *   <li><p>Type for one-to-one/many is deduced from the annotated field type; an exception is thrown if unsuitable.</p></li>
 *   <li><p>To avoid Postgres function argument limits, JSON objects are built in chunks and concatenated.</p></li>
 * </ul>
 *
 * @author John Cameron

 */
@Component
public class SqlJsonQueryBuilder {

    /**
     * <p>
     * Maximum number of key/value pairs per {@code jsonb_build_object(...)} call.
     * </p>
     *
     * <p>
     * Keep this comfortably under typical engine limits. Each pair contributes two arguments.
     * </p>
     */
    private static final int JSONB_BUILD_OBJECT_MAX_PAIRS = 40;

    /**
     * <p>
     * Builds a query returning {@code id} and {@code json}.
     * </p>
     *
     * <p>
     * The returned {@code json} column is aliased as {@code json}.
     * </p>
     *
     * @param rootDtoClass Root DTO class annotated with {@code @SqlTable}
     * @param idsParamName Named parameter used in {@code IN (:<param>)} predicate
     * @return SQL string
     */
    public String buildByIdsQuery(@NonNull Class<?> rootDtoClass, @NonNull String idsParamName) {

        //SQL is generated automatically from DTO objects. Annotations on the DTO classes are used
        //to drive the SQL generation.
        SqlTable rootTable = requireSqlTable(rootDtoClass);

        //This is the database table associated with the root DTO. 
        //For example, the Candidate table for the CandidateReadDTO. You will see this table
        //specified on the @SQLTable annotation on the CandidateReadDTO.
        String rootTableAlias = rootTable.alias();

        //Matching data is retrieved in the form of a single String field containing JSON.
        //This creates the Postgres SQL which populates the returned JSON. It uses Postgres
        //jsonb support, specifically jsonb_agg and jsonb_build_object.
        BuildContext ctx = new BuildContext();
        String jsonExpr = buildJsonExpression(rootDtoClass, rootTableAlias, ctx);

        //The SQL is just a select where id matches one of the ids passed in.
        //There is a row for each id. Each row just has two fields: the id and the JSON encoded data. 
        //The json string field encodes a single object encoding an instance of the rootDtoClass.
        return """
            select
              %s.id,
              %s as json
            from %s %s
            where %s.id in (:%s)
            """.formatted(
            rootTableAlias,
            jsonExpr,
            rootTable.name(), rootTableAlias,
            rootTableAlias, idsParamName
        ).strip();
    }


    /**
     * <p>
     * Builds a SQL expression that produces a {@code jsonb} object representing
     * the given DTO type, using the provided table alias as the current SQL scope.
     * </p>
     *
     * <p>
     * This method is the core recursive entry point of the SQL JSON builder.
     * It inspects the fields of the DTO class and, for each field, generates
     * the appropriate SQL expression based on annotations and conventions.
     * </p>
     *
     * <p>
     * Supported field mappings:
     * </p>
     *
     * <ul>
     *   <li><p>
     *     Scalar fields mapped to columns via {@link SqlColumn}, or via optional
     *     default column mapping when enabled on the DTO.
     *   </p></li>
     *   <li><p>
     *     Nested one-to-one relationships via {@link JsonOneToOne}, implemented
     *     as correlated subqueries that return a single JSON object or {@code null}.
     *   </p></li>
     *   <li><p>
     *     Nested one-to-many relationships via {@link JsonOneToMany}, implemented
     *     as correlated subqueries that return JSON arrays (empty rather than
     *     {@code null}).
     *   </p></li>
     * </ul>
     *
     * <p>
     * For nested relationships, this method is called recursively with a new
     * table alias representing the immediate child scope. Alias uniqueness is
     * managed by {@link BuildContext}.
     * </p>
     *
     * <p>
     * JSON key names are always derived from DTO field names. This method
     * produces only the SQL <em>value</em> expressions for each field; key
     * assignment is handled centrally when assembling the JSON object.
     * </p>
     *
     * <p>
     * To avoid PostgreSQL function argument limits, the generated key/value
     * pairs are assembled using {@link #buildJsonbObjectInChunks(List)}.
     * </p>
     *
     * <p>
     * Fields may be excluded from processing if they are:
     * </p>
     *
     * <ul>
     *   <li><p>Annotated with {@link SqlIgnore}.</p></li>
     *   <li><p>Static, transient, or synthetic.</p></li>
     * </ul>
     *
     * <p>
     * If default column mapping is enabled for the DTO and a field appears to
     * be a complex type but lacks relationship annotations, this method fails
     * fast with an exception to avoid silently generating incorrect SQL.
     * </p>
     *
     * @param dtoType DTO class to convert into a JSON object
     * @param tableAlias SQL alias representing the current table scope
     * @param ctx build context used to ensure alias uniqueness across recursion
     * @return SQL expression producing a {@code jsonb} object
     */
    private String buildJsonExpression(Class<?> dtoType, String tableAlias, BuildContext ctx) {
        List<Pair> pairs = new ArrayList<>();

        boolean defaultColumnsEnabled = isDefaultColumnsEnabled(dtoType);

        for (Field field : dtoType.getDeclaredFields()) {
            if (shouldSkipField(field)) {
                continue;
            }
            if (field.isAnnotationPresent(SqlIgnore.class)) {
                continue;
            }

            JsonOneToMany oneToMany = field.getAnnotation(JsonOneToMany.class);
            if (oneToMany != null) {
                Class<?> elementType = deduceCollectionElementType(field);
                SqlTable childTable = requireSqlTable(elementType);

                String childAlias = ctx.uniqueAlias(childTable.alias());
                String valueExpr = buildOneToManyExpression(
                    elementType,
                    childTable,
                    childAlias,
                    oneToMany.joinColumn(),
                    tableAlias
                );

                pairs.add(new Pair(field.getName(), valueExpr));
                continue;
            }

            JsonOneToOne oneToOne = field.getAnnotation(JsonOneToOne.class);
            if (oneToOne != null) {
                Class<?> targetType = deduceOneToOneTargetType(field);
                SqlTable childTable = requireSqlTable(targetType);

                String childAlias = ctx.uniqueAlias(childTable.alias());
                String valueExpr = buildOneToOneExpression(
                    targetType,
                    childTable,
                    childAlias,
                    oneToOne.joinColumn(),
                    tableAlias
                );

                pairs.add(new Pair(field.getName(), valueExpr));
                continue;
            }

            SqlColumn sqlColumn = field.getAnnotation(SqlColumn.class);
            if (sqlColumn != null) {
                String columnName = sqlColumn.name().isBlank()
                    ? camelToSnakeCase(field.getName())
                    : sqlColumn.name();

                pairs.add(new Pair(field.getName(), tableAlias + "." + columnName));
                continue;
            }

            if (defaultColumnsEnabled) {
                if (looksLikeComplexType(field.getType())) {
                    throw new IllegalStateException(
                        "Field '" + dtoType.getSimpleName() + "." + field.getName()
                            + "' looks like a complex type but is not annotated with @JsonOneToOne/@JsonOneToMany or @SqlIgnore"
                    );
                }

                String columnName = camelToSnakeCase(field.getName());
                pairs.add(new Pair(field.getName(), tableAlias + "." + columnName));
            }
        }

        return buildJsonbObjectInChunks(pairs);
    }

    /**
     * <p>
     * Builds a SQL expression that produces a nested JSON object for a one-to-one
     * relationship.
     * </p>
     *
     * <p>
     * The returned SQL expression:
     * </p>
     *
     * <ul>
     *   <li><p>Runs a correlated subquery against the target table.</p></li>
     *   <li><p>Builds a JSON object representing the target DTO.</p></li>
     *   <li><p>Returns {@code null} if no matching target row exists.</p></li>
     * </ul>
     *
     * <p>
     * Correlation is performed using a foreign-key column on the parent table
     * that references {@code targetAlias.id}. This reflects the common
     * one-to-one pattern where the parent owns the relationship.
     * </p>
     *
     * <p>
     * As with one-to-many relationships, this method does not determine the JSON
     * field name. The caller is responsible for associating the returned
     * expression with the correct JSON key.
     * </p>
     *
     * @param targetType DTO class representing the one-to-one target
     * @param targetTable {@link SqlTable} describing the target table
     * @param targetAlias SQL alias to use for the target table
     * @param joinColumnOnParent foreign-key column on the parent table referencing
     *                           {@code targetAlias.id}
     * @param parentAlias SQL alias of the parent table
     * @return SQL expression producing a {@code jsonb} object or {@code null}
     */
    private String buildOneToOneExpression(
        Class<?> targetType,
        SqlTable targetTable,
        String targetAlias,
        String joinColumnOnParent,
        String parentAlias
    ) {
        BuildContext nestedCtx = new BuildContext();
        String nestedJson = buildJsonExpression(targetType, targetAlias, nestedCtx);

        return ("""
            (
              select %s
              from %s %s
              where %s.%s = %s.id
            )
            """).formatted(
            nestedJson,
            targetTable.name(),
            targetAlias,
            parentAlias,
            joinColumnOnParent,
            targetAlias
        ).trim();
    }

    /**
     * <p>
     * Builds a SQL expression that produces a JSON array for a one-to-many
     * relationship.
     * </p>
     *
     * <p>
     * The returned SQL expression:
     * </p>
     *
     * <ul>
     *   <li><p>Runs a correlated subquery against the child table.</p></li>
     *   <li><p>Aggregates child rows using {@code jsonb_agg(...)}.</p></li>
     *   <li><p>Returns an empty JSON array ({@code []}) rather than {@code null}
     *       when no child rows exist.</p></li>
     * </ul>
     *
     * <p>
     * Correlation is performed against the <em>immediate parent alias</em>
     * provided to this method. This is critical for supporting nested
     * one-to-many relationships; child rows must join to their direct parent,
     * not to the root table.
     * </p>
     *
     * <p>
     * This method is intentionally unaware of the JSON field name. The caller
     * is responsible for associating the returned expression with a JSON key
     * corresponding to the DTO field name.
     * </p>
     *
     * @param elementType DTO class representing the element type of the collection
     * @param childTable {@link SqlTable} describing the child table
     * @param childAlias SQL alias to use for the child table
     * @param joinColumnOnChild foreign-key column on the child table referencing
     *                          {@code parentAlias.id}
     * @param parentAlias SQL alias of the immediate parent table
     * @return SQL expression producing a {@code jsonb} array
     */
    private String buildOneToManyExpression(
        Class<?> elementType,
        SqlTable childTable,
        String childAlias,
        String joinColumnOnChild,
        String parentAlias
    ) {
        BuildContext nestedCtx = new BuildContext();
        String elementJson = buildJsonExpression(elementType, childAlias, nestedCtx);

        return ("""
            (
              select coalesce(jsonb_agg(%s), '[]'::jsonb)
              from %s %s
              where %s.%s = %s.id
            )
            """).formatted(
            elementJson,
            childTable.name(),
            childAlias,
            childAlias,
            joinColumnOnChild,
            parentAlias
        ).trim();
    }


    /**
     * <p>
     * Builds a {@code jsonb} object expression from the given key/value pairs,
     * splitting the object into multiple {@code jsonb_build_object(...)} calls
     * if necessary and concatenating them.
     * </p>
     *
     * <p>
     * This method exists to avoid PostgreSQL's function argument limits.
     * {@code jsonb_build_object} takes one argument per JSON key and one per
     * value, so a JSON object with many fields can easily exceed the maximum
     * allowed number of arguments in a single function call.
     * </p>
     *
     * <p>
     * To work around this, the pairs are partitioned into chunks of at most
     * {@link #JSONB_BUILD_OBJECT_MAX_PAIRS} key/value pairs. Each chunk is built
     * as its own {@code jsonb_build_object(...)} expression, and the resulting
     * objects are merged using the {@code ||} operator.
     * </p>
     *
     * <p>
     * PostgreSQL's {@code jsonb || jsonb} operator merges objects such that:
     * </p>
     *
     * <ul>
     *   <li><p>All keys from both objects are present in the result.</p></li>
     *   <li><p>If the same key appears in both objects, the value from the right-hand
     *       operand wins.</p></li>
     * </ul>
     *
     * <p>
     * In this builder, keys are guaranteed to be unique across chunks, so
     * overwriting cannot occur.
     * </p>
     *
     * <p>
     * If no pairs are provided, this method returns an empty JSON object
     * literal ({@code '{}'::jsonb}).
     * </p>
     *
     * @param pairs ordered list of JSON field names and their SQL value expressions
     * @return SQL expression producing a {@code jsonb} object
     */
    private String buildJsonbObjectInChunks(List<Pair> pairs) {
        if (pairs.isEmpty()) {
            return "'{}'::jsonb";
        }

        List<String> chunkExprs = new ArrayList<>();
        for (int i = 0; i < pairs.size(); i += JSONB_BUILD_OBJECT_MAX_PAIRS) {
            int end = Math.min(i + JSONB_BUILD_OBJECT_MAX_PAIRS, pairs.size());
            List<Pair> chunk = pairs.subList(i, end);

            StringBuilder sb = new StringBuilder();
            sb.append("jsonb_build_object(");
            for (int j = 0; j < chunk.size(); j++) {
                Pair p = chunk.get(j);
                if (j > 0) sb.append(", ");
                sb.append("'").append(p.jsonKey).append("', ");
                sb.append(p.valueExpr);
            }
            sb.append(")");
            chunkExprs.add(sb.toString());
        }

        // Concatenate chunked objects. jsonb "||" merges objects (later keys overwrite earlier, but keys are unique here).
        if (chunkExprs.size() == 1) {
            return chunkExprs.get(0);
        }

        StringBuilder merged = new StringBuilder();
        merged.append(chunkExprs.get(0));
        for (int i = 1; i < chunkExprs.size(); i++) {
            merged.append(" || ").append(chunkExprs.get(i));
        }
        return merged.toString();
    }

    /* ==========================================================
       Type resolution
       ========================================================== */

    private boolean looksLikeComplexType(Class<?> type) {
        if (type.isPrimitive()) return false;
        if (Number.class.isAssignableFrom(type)) return false;
        if (CharSequence.class.isAssignableFrom(type)) return false;
        if (Boolean.class.equals(type)) return false;
        if (Enum.class.isAssignableFrom(type)) return false;

        // Date/time are treated as scalar columns in SQL.
        if (java.time.temporal.Temporal.class.isAssignableFrom(type)) return false;

        // Collections are handled via @JsonOneToMany; unannotated collections are suspicious.
        if (Collection.class.isAssignableFrom(type)) return true;

        // DTO types and other objects should be annotated explicitly.
        return true;
    }

    private Class<?> deduceOneToOneTargetType(Field field) {
        Class<?> t = field.getType();

        if (t.isInterface()) {
            throw new IllegalStateException("Field '" + field.getDeclaringClass().getSimpleName() + "." + field.getName()
                + "' type is an interface; cannot deduce @JsonOneToOne target type safely.");
        }
        if (t.isPrimitive()) {
            throw new IllegalStateException("Field '" + field.getDeclaringClass().getSimpleName() + "." + field.getName()
                + "' is primitive; @JsonOneToOne requires an object type.");
        }
        if (Collection.class.isAssignableFrom(t)) {
            throw new IllegalStateException("Field '" + field.getDeclaringClass().getSimpleName() + "." + field.getName()
                + "' is a collection; use @JsonOneToMany.");
        }

        return t;
    }

    private Class<?> deduceCollectionElementType(Field field) {
        if (!Collection.class.isAssignableFrom(field.getType())) {
            throw new IllegalStateException("Field '" + field.getDeclaringClass().getSimpleName() + "." + field.getName()
                + "' must be a Collection type for @JsonOneToMany.");
        }

        Type gt = field.getGenericType();
        if (!(gt instanceof ParameterizedType pt)) {
            throw new IllegalStateException("Field '" + field.getDeclaringClass().getSimpleName() + "." + field.getName()
                + "' must be parameterized (e.g. List<Foo>) for @JsonOneToMany.");
        }

        Type arg0 = pt.getActualTypeArguments()[0];
        if (!(arg0 instanceof Class<?> elementType)) {
            throw new IllegalStateException("Cannot deduce element type for field '"
                + field.getDeclaringClass().getSimpleName() + "." + field.getName() + "'.");
        }

        if (elementType.isInterface()) {
            throw new IllegalStateException("Element type is an interface for field '"
                + field.getDeclaringClass().getSimpleName() + "." + field.getName() + "'.");
        }

        return elementType;
    }

    private SqlTable requireSqlTable(Class<?> dtoType) {
        SqlTable st = dtoType.getAnnotation(SqlTable.class);
        if (st == null) {
            throw new IllegalStateException("Missing @SqlTable on " + dtoType.getName());
        }
        if (st.name() == null || st.name().isBlank()) {
            throw new IllegalStateException("@SqlTable.name is blank on " + dtoType.getName());
        }
        if (st.alias() == null || st.alias().isBlank()) {
            throw new IllegalStateException("@SqlTable.alias is blank on " + dtoType.getName());
        }
        return st;
    }

    private boolean shouldSkipField(Field field) {
        int m = field.getModifiers();
        return field.isSynthetic()
            || Modifier.isStatic(m)
            || Modifier.isTransient(m);
    }

    private boolean isDefaultColumnsEnabled(Class<?> dtoType) {
        SqlDefaults d = dtoType.getAnnotation(SqlDefaults.class);
        return d != null && d.mapUnannotatedColumns();
    }
    
    /**
     * <p>
     * Maintains SQL alias uniqueness during recursive SQL generation.
     * </p>
     *
     * <p>
     * {@code SqlJsonQueryBuilder} generates nested correlated subqueries when
     * building JSON for one-to-one and one-to-many relationships. Each level
     * of nesting introduces new table aliases.
     * </p>
     *
     * <p>
     * {@code BuildContext} ensures that aliases derived from {@link SqlTable#alias()}
     * remain unique within the generated SQL statement. If the same base alias
     * is requested more than once (for example, when the same DTO type appears
     * multiple times in different branches), a numeric suffix is appended:
     * </p>
     *
     * <ul>
     *   <li><p>{@code co}</p></li>
     *   <li><p>{@code co2}</p></li>
     *   <li><p>{@code co3}</p></li>
     * </ul>
     *
     * <p>
     * This avoids alias collisions while keeping aliases readable and
     * deterministic.
     * </p>
     *
     * <p>
     * {@code BuildContext} is intentionally short-lived and scoped to a single
     * SQL build operation. It does not cache state across invocations.
     * </p>
     */
    private static final class BuildContext {
        private final Map<String, Integer> aliasCounts = new HashMap<>();

        private String uniqueAlias(String baseAlias) {
            Integer count = aliasCounts.get(baseAlias);
            if (count == null) {
                aliasCounts.put(baseAlias, 1);
                return baseAlias;
            }
            int next = count + 1;
            aliasCounts.put(baseAlias, next);
            return baseAlias + next;
        }
    }

    private static final class Pair {
        private final String jsonKey;
        private final String valueExpr;

        private Pair(String jsonKey, String valueExpr) {
            this.jsonKey = jsonKey;
            this.valueExpr = valueExpr;
        }
    }
}
