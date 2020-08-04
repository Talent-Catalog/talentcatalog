/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.repository.es;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.tbbtalent.server.model.es.CandidateEs;
import org.tbbtalent.server.service.db.impl.CandidateServiceImpl;

public interface CandidateEsRepository 
        extends ElasticsearchRepository<CandidateEs, String> {

    /**
     * Supports Elasticsearch Simple Query String Query as described here:
     * https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-simple-query-string-query.html
     * <p/>
     * Uses Query annotation as described here:
     * https://docs.spring.io/spring-data/elasticsearch/docs/current-SNAPSHOT/reference/html/#elasticsearch.query-methods.at-query
     * @param searchQuery Query string
     * @param pageable Paging and sorting
     * @return Requested page of results matching the query
     * @deprecated Easier to use {@link NativeSearchQueryBuilder} - 
     * see {@link CandidateServiceImpl}, as described here 
     * https://www.baeldung.com/spring-data-elasticsearch-tutorial 
     */
    @Query("{\"simple_query_string\": {\"query\": \"?0\"}}")
    Page<CandidateEs> simpleQueryString(String searchQuery, Pageable pageable);
}