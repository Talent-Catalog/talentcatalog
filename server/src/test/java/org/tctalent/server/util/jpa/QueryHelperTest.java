/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 *  the terms of the GNU General Public License as published by the Free
 *  Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.util.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import jakarta.persistence.Tuple;
import jakarta.persistence.TupleElement;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class QueryHelperTest {

  @Mock
  private Tuple tuple;

  @Mock
  private TupleElement<String> firstNameElement;

  @Mock
  private TupleElement<Integer> ageElement;

  @Test
  void parseTupleResultConvertsTupleRowsToObjectsUsingSnakeCaseAliases() {
    QueryHelper queryHelper = new QueryHelper();

    when(firstNameElement.getAlias()).thenReturn("first_name");
    when(ageElement.getAlias()).thenReturn("age");
    when(tuple.getElements()).thenReturn(List.of(firstNameElement, ageElement));
    when(tuple.get(firstNameElement)).thenReturn("Ehsan");
    when(tuple.get(ageElement)).thenReturn(30);

    List<TestDto> result = queryHelper.parseTupleResult(List.of(tuple), TestDto.class);

    assertEquals(1, result.size());
    assertEquals("Ehsan", result.get(0).getFirstName());
    assertEquals(30, result.get(0).getAge());
  }

  @Test
  void parseTupleResultReturnsEmptyListWhenQueryResultListIsEmpty() {
    QueryHelper queryHelper = new QueryHelper();

    List<TestDto> result = queryHelper.parseTupleResult(List.of(), TestDto.class);

    assertEquals(List.of(), result);
  }

  @Test
  void parseTupleResultThrowsWhenResultObjectIsNotTuple() {
    QueryHelper queryHelper = new QueryHelper();

    RuntimeException exception = assertThrows(
        RuntimeException.class,
        () -> queryHelper.parseTupleResult(List.of("not a tuple"), TestDto.class)
    );

    assertEquals("Query should return instance of Tuple", exception.getMessage());
  }

  @Setter
  @Getter
  private static class TestDto {
    private String firstName;
    private Integer age;

  }
}