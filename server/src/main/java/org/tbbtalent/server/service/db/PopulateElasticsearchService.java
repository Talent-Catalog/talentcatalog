/*
 * Copyright (c) 2021 Talent Beyond Boundaries.
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

package org.tbbtalent.server.service.db;

/**
 * Service for bulk updates of Elasticsearch
 *
 * @author John Cameron
 */
public interface PopulateElasticsearchService {

    /**
     * This will loop through all candidates on the database and add or update
     * their counterparts in Elasticsearch.
     * <p/>
     * It is expected to be run as an asynchronous task.
     * @param deleteExisting If true, the process starts by deleting all 
     *                       existing candidate records from Elasticsearch.
     */
    void populateElasticCandidates(boolean deleteExisting);
}
