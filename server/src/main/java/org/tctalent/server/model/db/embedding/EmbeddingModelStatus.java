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

package org.tctalent.server.model.db.embedding;

/**
 * TC status of an EmbeddingModel.
 * <p>
 *     At any given time there will be one ACTIVE model. There could also be multiple
 *     RETIRED models.
 *     New models can be introduced. They are typically prepared in the background,
 *     associated with an alternate embedding table. The other statuses tracking the building
 *     and potential deployment of a new model.
 * </p>
 *
 * @author John Cameron
 */
public enum EmbeddingModelStatus {
    /**
     * This is the current model used for matching.
     */
    ACTIVE,

    /**
     * Model is currently building to an alternate database table.
     */
    BUILDING,

    /**
     * Building of the model failed.
     */
    FAILED,

    /**
     * The model has successfully built on the alternate database table. It is ready to deploy.
     */
    READY,

    /**
     * The model used to be our active production model but has been replaced.
     * Eventually tables associated with RETIRED embedding models will be dropped to save
     * space (including the in memory table indexes).
     */
    RETIRED,

    /**
     * Previously active, still being kept up to date so rollback is immediate.
     */
    STANDBY
}
