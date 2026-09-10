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
 *     At any given time there can be multiple READY models which can be used for matching.
 *     All READY models are kept up to date as candidate data is updated.
 * <p>
 *     Before a model is READY it must be built. The BUILDING status indicates that the model is
 *     being built in the background. The model is built on an alternate database table, and
 *     when the build is complete, the model is marked READY and can be used for matching.
 * <p>
 *     If the build fails, the model is marked FAILED and will not be used for matching.
 * <p>
 *     To save resources (database space, memory, and CPU), models can be RETIRED.
 *     Retiring a model involves dropping the database table and in memory indexes associated with
 *     the model. A model can be in a RETIRING state before it is fully retired.
 *     A REIRING model is no longer presented to users as a candidate for matching.
 * <p>
 *     New models can be introduced. They are typically prepared in the background,
 *     associated with a new embedding table. Those models have status BUILDING until the
 *     build is complete. Once the build is complete, the model is marked READY and can be used
 *     for matching.
 * <p>
 * See <a href="https://github.com/Talent-Catalog/talentcatalog/blob/a6ff32d5c049d90a0e09965f321500d5173902cf/server/src/main/java/org/tctalent/server/model/db/embedding/README.md">
 *     this README</a> for more details on how models are built
 * and managed.
 * @author John Cameron
 */
public enum EmbeddingModelStatus {

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
     * The model used to be available for matching, but it is no longer used.
     * The tables associated with RETIRED embedding models can be dropped to save
     * space (including the in memory table indexes).
     */
    RETIRED,

    /**
     * The model is in the process of being retired. It is no longer presented to users as a
     * candidate for matching.
     * Once it is no longer being used, its status will be RETIRED.
     */
    RETIRING
}
