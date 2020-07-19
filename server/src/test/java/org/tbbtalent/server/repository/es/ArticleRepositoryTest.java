/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.repository.es;

/**
 * TODO JC Doc
 *
 * @author John Cameron
 */
//@SpringBootTest
public class ArticleRepositoryTest {

//    @Autowired
//    private ElasticsearchRestTemplate elasticsearchTemplate;
//
//    @Autowired
//    private ArticleRepository articleRepository;
//
//    private final Author johnSmith = new Author("John Smith");
//    private final Author johnDoe = new Author("John Doe");
//    
//    @BeforeEach
//    void before() {
//        Article article = new Article("Spring Data Elasticsearch");
//        article.setAuthors(asList(johnSmith, johnDoe));
//        article.setTags("elasticsearch", "spring data");
//        articleRepository.save(article);
//
//        article = new Article("Search engines");
//        article.setAuthors(asList(johnDoe));
//        article.setTags("search engines", "tutorial");
//        articleRepository.save(article);
//
//        article = new Article("Second Article About Elasticsearch");
//        article.setAuthors(asList(johnSmith));
//        article.setTags("elasticsearch", "spring data");
//        articleRepository.save(article);
//
//        article = new Article("Elasticsearch Tutorial");
//        article.setAuthors(asList(johnDoe));
//        article.setTags("elasticsearch");
//        articleRepository.save(article);
//    }
//
//    @AfterEach
//    public void after() {
//        articleRepository.deleteAll();
//    }
//
//    @Test
//    public void givenPersistedArticles_whenSearchByAuthorsName_thenRightFound() {
//        final Page<Article> articleByAuthorName = articleRepository
//                .findByAuthorsName(johnSmith.getName(), 
//                        PageRequest.of(0, 10));
//        assertEquals(2L, articleByAuthorName.getTotalElements());
//    }
//
//
//    @Test
//    public void givenPersistedArticles_whenUseRegexQuery_thenRightArticlesFound() {
//        final Query searchQuery = new NativeSearchQueryBuilder()
//                .withFilter(regexpQuery("title", ".*data.*"))
//                .build();
//
//        final SearchHits<Article> articles = elasticsearchTemplate
//                .search(searchQuery, Article.class, IndexCoordinates.of("blog"));
//
//        assertEquals(1, articles.getTotalHits());
//    }
//
//    @Test
//    public void givenSavedDoc_whenTitleUpdated_thenCouldFindByUpdatedTitle() {
//        final Query searchQuery = new NativeSearchQueryBuilder()
//                .withQuery(fuzzyQuery("title", "serch"))
//                .build();
//        final SearchHits<Article> articles = elasticsearchTemplate
//                .search(searchQuery, Article.class, IndexCoordinates.of("blog"));
//
//        assertEquals(1, articles.getTotalHits());
//
//        final Article article = articles.getSearchHit(0)
//                .getContent();
//        final String newTitle = "Getting started with Search Engines";
//        article.setTitle(newTitle);
//        articleRepository.save(article);
//
//        assertEquals(newTitle, articleRepository.findById(article.getId())
//                .get()
//                .getTitle());
//    }
    
}
