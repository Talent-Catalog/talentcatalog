/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.repository.db;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Give the table's name, this repository is used to update a job experience embedding table.
 */
@Repository
@RequiredArgsConstructor
public class JobExperienceEmbeddingRepository {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Inserts an embedding or replaces the existing embedding for the same
     * candidate job experience and embedding model.
     *
     * @return 1 when a row was inserted or updated
     */
    public int upsert(
        String tableName,
        long candidateJobExperienceId,
        long embeddingModelId,
        List<Double> embedding
    ) {
        validateTableName(tableName);

        String sql = """
        insert into %s (
            candidate_job_experience_id,
            embedding_model_id,
            embedding,
            updated_at
        )
        values (?, ?, ?::vector, now())
        on conflict (candidate_job_experience_id, embedding_model_id)
        do update set
            embedding = excluded.embedding,
            updated_at = now()
        """.formatted(tableName);

        return jdbcTemplate.update(
            sql,
            candidateJobExperienceId,
            embeddingModelId,
            toVectorLiteral(embedding)
        );
    }

    /**
     * Converts a Java list of doubles into PostgreSQL vector literal format.
     * <p>
     * Example:
     *   [0.12, -0.34, 0.56]
     */
    private String toVectorLiteral(List<Double> embedding) {
        return embedding.stream()
            .map(d -> d.toString())
            .collect(Collectors.joining(",", "[", "]"));
    }

    /**
     * Prevents arbitrary SQL from being supplied as the table name.
     * <p>
     * This syntax check should be used in addition to validating during
     * application startup that the configured table actually exists.
     */
    private void validateTableName(String tableName) {
        if (tableName == null
            || !tableName.matches("[a-z][a-z0-9_]*")) {
            throw new IllegalArgumentException(
                "Invalid embedding table name: " + tableName
            );
        }
    }
}
