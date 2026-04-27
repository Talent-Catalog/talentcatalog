package org.tctalent.server.model.db;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DelimitedIdConverterTest {

  private DelimitedIdConverter converter;

  @BeforeEach
  void setUp() {
    converter = new DelimitedIdConverter();
  }

  @Test
  void convertToDatabaseColumn_nullOrEmpty_returnsNullOrEmpty() {
    assertNull(converter.convertToDatabaseColumn(null));
    assertEquals("", converter.convertToDatabaseColumn(Collections.emptyList())); // empty list returns empty string
  }


  @Test
  void convertToDatabaseColumn_validList_returnsCommaSeparatedString() {
    List<Long> input = Arrays.asList(1L, 2L, 3L, 12345L);
    String result = converter.convertToDatabaseColumn(input);
    assertEquals("1,2,3,12345", result);
  }

  @Test
  void convertToEntityAttribute_nullOrEmpty_returnsNull() {
    assertNull(converter.convertToEntityAttribute(null));
    assertNull(converter.convertToEntityAttribute(""));
    assertNull(converter.convertToEntityAttribute("   "));
  }

  @Test
  void convertToEntityAttribute_validString_returnsListOfLongs() {
    String input = "1,2,3,12345";
    List<Long> result = converter.convertToEntityAttribute(input);
    assertEquals(Arrays.asList(1L, 2L, 3L, 12345L), result);
  }

  @Test
  void convertToEntityAttribute_stringWithoutSpaces_returnsListOfLongs() {
    String input = "1,2,3";
    List<Long> result = converter.convertToEntityAttribute(input);
    assertEquals(Arrays.asList(1L, 2L, 3L), result);
  }

}
