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

import java.util.List;
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
    public List<EmbeddingModel> getReadyModels() {
        return embeddingModelRepository.findAllByStatus(EmbeddingModelStatus.READY);
    }

    @Override
    public List<EmbeddingModel> getReadyOrBuildingModels() {
        return embeddingModelRepository.findAllByStatusIn(
            List.of(EmbeddingModelStatus.READY, EmbeddingModelStatus.BUILDING));
    }

    @Override
    public String getTableNameForModel(EmbeddingModel model) {
        return "experience_embedding" + "_" + model.getModelKey().toLowerCase();
    }

    @Override
    public EmbeddingModel save(EmbeddingModel model) {
        return embeddingModelRepository.save(model);
    }
}
