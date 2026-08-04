/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 *  the terms of the GNU General Public License as published by the Free
 *  Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.service.db.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.tctalent.server.configuration.properties.VectorEmbeddingModelProperties;
import org.tctalent.server.model.db.embedding.EmbeddingModel;
import org.tctalent.server.model.db.embedding.EmbeddingModelStatus;
import org.tctalent.server.repository.db.EmbeddingModelRepository;
import org.tctalent.server.service.db.EmbeddingModelService;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmbeddingModelServiceImpl implements EmbeddingModelService {
    private final EmbeddingModelRepository embeddingModelRepository;
    private final VectorEmbeddingModelProperties vectorEmbeddingModelProperties;

//todo    private static final String DATABASE_SCHEMA = "public";

    @Override
    @Nullable
    public EmbeddingModel findModelByKey(String modelKey) {
        return embeddingModelRepository.findByModelKey(modelKey);
    }

    @Override
    @Nullable
    public EmbeddingModel getBuildingModel() {
        return embeddingModelRepository.findByStatus(EmbeddingModelStatus.BUILDING);
    }

    @Override
    @Nullable
    public EmbeddingModel getDefaultModel() {
        return findModelByKey(vectorEmbeddingModelProperties.getDefaultEmbeddingModelKey());
    }

    @Override
    public String getTableNameForModel(EmbeddingModel model) {
        String name = "job_experience_embedding" + "_" + model.getModelKey().toLowerCase();

        //TODO JC Check that table exists in DB.
        //TODO commented out until we sort out problem with Databaseconfiration supporting multipl
        //TODO data sources which seems to cause problem with jdbcTemplate not being available.
//        Integer tableCount = jdbc.queryForObject(
//            """
//            select count(*)
//            from information_schema.tables
//            where table_schema = ?
//              and table_name = ?
//              and table_type = 'BASE TABLE'
//            """,
//            Integer.class,
//            DATABASE_SCHEMA,
//            name
//        );
//
//        if (tableCount == null || tableCount == 0) {
//            log.warn(
//                "embedding.alternate-embedding-table '%s' is not a table in schema '%s'",
//                name,
//                DATABASE_SCHEMA
//            );
//        }
        return name;
    }

    @Override
    public EmbeddingModel save(EmbeddingModel model) {
        return embeddingModelRepository.save(model);
    }
}
