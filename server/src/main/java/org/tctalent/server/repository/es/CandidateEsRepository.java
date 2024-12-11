/*
 * Copyright (c) 2024 Talent Catalog.
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

package org.tctalent.server.repository.es;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.tctalent.server.model.es.CandidateEs;

/**
 * This provides access to Candidate documents in the Elasticsearch server.
 * <p>
 *   It is primarily useful for updating existing records through its
 *   save method and accessing (or deleting) documents using their ids.
 * </p>
 * <p>
 *     It is less useful for doing complex queries. See deprecation
 *     comments on the simpleQueryString method.
 *     It is not flexible enough.
 * </p>
 */
public interface CandidateEsRepository
        extends ElasticsearchRepository<CandidateEs, String> {

    /**
     * Supports Elasticsearch Simple Query String Query as described here:
     * <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-simple-query-string-query.html">
     *     In the Elasticsearch doc</a>
     * <p/>
     * Uses Query annotation as described here:
     * <a href="https://docs.spring.io/spring-data/elasticsearch/reference/elasticsearch/repositories/elasticsearch-repository-queries.html#elasticsearch.query-methods.at-query">
     *     In the Spring Data Elasticsearch Repository doc</a>
     * @param searchQuery Query string
     * @param pageable Paging and sorting
     * @return Requested page of results matching the query
     */
    @Query("{\"simple_query_string\": {\"query\": \"?0\"}}")
    Page<CandidateEs> simpleQueryString(String searchQuery, Pageable pageable);

    /**
     * Deletes candidate with given candidate number
     * @param candidateNumber Candidate number
     */
    void deleteByCandidateNumber(String candidateNumber);
}
