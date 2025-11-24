package org.tctalent.server.model.db;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SearchJoinTest {

  private SearchJoin searchJoin;

  @BeforeEach
  void setUp() {
    searchJoin = new SearchJoin();
  }

  @Test
  void testDefaultConstructor() {
    assertNull(searchJoin.getSavedSearch());
    assertNull(searchJoin.getChildSavedSearch());
    assertNull(searchJoin.getSearchType());
  }

  @Test
  void testSavedSearchGetterSetter() {
    SavedSearch savedSearch = new SavedSearch();
    searchJoin.setSavedSearch(savedSearch);
    assertSame(savedSearch, searchJoin.getSavedSearch());
  }

  @Test
  void testChildSavedSearchGetterSetter() {
    SavedSearch childSearch = new SavedSearch();
    searchJoin.setChildSavedSearch(childSearch);
    assertSame(childSearch, searchJoin.getChildSavedSearch());
  }

  @Test
  void testSearchTypeGetterSetter() {
    SearchType type = SearchType.and;
    searchJoin.setSearchType(type);
    assertEquals(type, searchJoin.getSearchType());
  }
}
