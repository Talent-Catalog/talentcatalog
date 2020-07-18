/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.repository.es;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.tbbtalent.server.model.es.Article;

/**
 * TODO JC Doc
 *
 * @author John Cameron
 */
public interface ArticleRepository 
        extends ElasticsearchRepository<Article, String> 
{

    Iterable<Article> findByAuthorsName(String name);

    Page<Article> findByAuthorsName(String name, Pageable pageable);

    //todo For some reason this won't run - stops tart up.
//    @Query("{\"bool\": {\"must\": [{\"match\": {\"authors.name\": \"?0\"}}]}}")
//    Page<Article> findByAuthorsNameUsingCustomQuery(String name, Pageable pageable);
}