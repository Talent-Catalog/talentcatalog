/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.repository.es;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.tbbtalent.server.model.es.Article;
import org.tbbtalent.server.model.es.Author;

/**
 * TODO JC Doc
 *
 * @author John Cameron
 */
@SpringBootTest
public class ArticleRepositoryTest {

    @Autowired
    private ArticleRepository articleRepository;
    
    @Test
    void testCreate() {
        Article article = new Article("Spring Data Elasticsearch");
        article.setAuthors(Arrays.asList(new Author("John Smith"), new Author("John Doe")));
        articleRepository.save(article);
    }
}
