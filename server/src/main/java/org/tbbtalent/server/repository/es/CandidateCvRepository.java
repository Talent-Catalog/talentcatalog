/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.repository.es;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.tbbtalent.server.model.db.Candidate;

/**
 * TODO JC Doc
 *
 * @author John Cameron
 */
public interface CandidateCvRepository  
        extends ElasticsearchRepository<Candidate, String> {
}
