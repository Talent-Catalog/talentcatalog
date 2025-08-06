package org.tctalent.server.api.admin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.tctalent.server.exception.*;

import static org.junit.jupiter.api.Assertions.*;

class IJoinedTableApiTest {

  private IJoinedTableApi<Object, Object, Object> api;

  @BeforeEach
  void setup() {
    // Anonymous class implementing interface with no overrides
    api = new IJoinedTableApi<>() {};
  }

  @Test
  void testCreate_throwsNotImplementedException() {
    NotImplementedException ex = assertThrows(
        NotImplementedException.class,
        () -> api.create(1L, new Object())
    );
    assertTrue(ex.getMessage().contains("create"));
  }

  @Test
  void testDelete_throwsNotImplementedException() {
    NotImplementedException ex = assertThrows(
        NotImplementedException.class,
        () -> api.delete(1L)
    );
    assertTrue(ex.getMessage().contains("delete"));
  }

  @Test
  void testGet_throwsNotImplementedException() {
    NotImplementedException ex = assertThrows(
        NotImplementedException.class,
        () -> api.get(1L)
    );
    assertTrue(ex.getMessage().contains("get"));
  }

  @Test
  void testList_throwsNotImplementedException() {
    NotImplementedException ex = assertThrows(
        NotImplementedException.class,
        () -> api.list(1L)
    );
    assertTrue(ex.getMessage().contains("list"));
  }

  @Test
  void testSearch_throwsNotImplementedException() {
    NotImplementedException ex = assertThrows(
        NotImplementedException.class,
        () -> api.search(1L, new Object())
    );
    assertTrue(ex.getMessage().contains("search"));
  }

  @Test
  void testSearchPaged_throwsNotImplementedException() {
    NotImplementedException ex = assertThrows(
        NotImplementedException.class,
        () -> api.searchPaged(1L, new Object())
    );
    assertTrue(ex.getMessage().contains("searchPaged"));
  }

  @Test
  void testUpdate_throwsNotImplementedException() {
    NotImplementedException ex = assertThrows(
        NotImplementedException.class,
        () -> api.update(1L, new Object())
    );
    assertTrue(ex.getMessage().contains("update"));
  }
}
