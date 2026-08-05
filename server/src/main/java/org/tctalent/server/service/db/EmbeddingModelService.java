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

package org.tctalent.server.service.db;

import java.util.List;
import org.springframework.lang.Nullable;
import org.tctalent.server.model.db.embedding.EmbeddingModel;

/**
 * Service for managing embedding models in the database.
 *
 * @author John Cameron
 */
public interface EmbeddingModelService {

    /**
     * Finds an embedding model by its model key.
     * @param modelKey The model key of the embedding model to find.
     * @return EmbeddingModel The embedding model with the specified model key, or null if not found.
     */
    @Nullable
    EmbeddingModel findModelByKey(String modelKey);

    /**
     * Returns the currently building model if one exists.
     * @return EmbeddingModel The currently building model, or null if there is no model building.
     */
    @Nullable
    EmbeddingModel getBuildingModel();

    /**
     * Returns the default embedding model as specified in the configuration properties.
     * @return EmbeddingModel The default embedding model.
     */
    EmbeddingModel getDefaultModel();

    /**
     * Returns all embedding models that are currently READY - ie available for matching.
     * @return List of READY embedding models. Empty if there are none.
     */
    List<EmbeddingModel> getReadyModels();

    /**
     * Returns the table name for the given embedding model.
     * @param model The embedding model for which to get the table name.
     * @return String The table name associated with the given embedding model.
     */
    String getTableNameForModel(EmbeddingModel model);

    /**
     * Saves the given embedding model to the database.
     * @param model The embedding model to save.
     * @return EmbeddingModel The saved embedding model.
     */
    EmbeddingModel save(EmbeddingModel model);
}
