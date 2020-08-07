/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
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
