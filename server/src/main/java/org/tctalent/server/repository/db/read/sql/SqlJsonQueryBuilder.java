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

/**
 * Generates SQL for fetching candidate data from the database.
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
 */
@Component
public class SqlJsonQueryBuilder {

    private static final int JSON_FIELDS_PER_CHUNK = 40;

    /**
     * Compute the SQL required to fetch data with the 
     * @param rootDtoClass Root DTO class we want to receive data
     * @param idsParamName Name of parameter in the SQL which will receive the ids of the data to
     *                     be fetched by the SQL.
     * @return SQL query string which will retrieve the data
     */
    public String buildByIdsQuery(Class<?> rootDtoClass, String idsParamName) {

        //SQL is generated automatically from DTO objects. Annotations on the DTO classes are used
        //to drive the SQL generation.
        SqlTable rootTable = require(
            rootDtoClass.getAnnotation(SqlTable.class),
            "Root DTO must have @SqlTable"
        );

        //This the database table associated with the root DTO. 
        //For example, the Candidate table for the CandidateReadDTO. You will see this table
        //specified on the @SQLTable annotation on the CandidateReadDTO.
        String rootTableAlias = rootTable.alias();

        //Matching data is retrieved in the form of a single String field containing JSON.
        //This creates the Postgres SQL which populates the returned JSON. It uses Postgres
        //jsonb support, specifically jsonb_agg and jsonb_build_object.
        String rootJson = buildJsonObject(rootDtoClass, rootTableAlias,rootTableAlias + ".id");

        //The SQL is just a select where id matches one of the ids passed in.
        //There is a row for each id. Each row just has two fields: the id and the JSON encoded data. 
        //The json "data" field encodes a single object encoding an instance of the rootDtoClass.
        return """
            select
              %s.id,
              %s as data
            from %s %s
            where %s.id in (:%s)
            """.formatted(
            rootTableAlias,
            rootJson,
            rootTable.name(), rootTableAlias,
            rootTableAlias, idsParamName
        ).strip();
    }
    private String buildJsonObject(
        Class<?> dtoType,
        String tableAlias,
        String idExpression
    ) {
        boolean mapDefaults = mapUnannotated(dtoType);
        List<String> kvPairs = new ArrayList<>();

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

                kvPairs.add("""
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

                kvPairs.add("""
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

            kvPairs.add("'" + jsonKey + "', " + tableAlias + "." + dbColumn);
        }

        if (kvPairs.isEmpty()) {
            throw new IllegalStateException(
                "DTO " + dtoType.getName() + " has no mapped fields"
            );
        }

        return buildChunkedJsonObject(kvPairs);
    }

    /* ==========================================================
       JSON chunking logic (NEW)
       ========================================================== */

    private String buildChunkedJsonObject(List<String> kvPairs) {

        List<String> chunks = new ArrayList<>();

        for (int i = 0; i < kvPairs.size(); i += JSON_FIELDS_PER_CHUNK) {
            List<String> slice = kvPairs.subList(
                i,
                Math.min(i + JSON_FIELDS_PER_CHUNK, kvPairs.size())
            );

            chunks.add(
                "jsonb_build_object(" + String.join(", ", slice) + ")"
            );
        }

        // Merge all chunks into a single JSON object
        return String.join(" || ", chunks);
    }

    /* ==========================================================
       Type resolution (unchanged)
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
