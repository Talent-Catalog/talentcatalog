package org.tctalent.server.model.db;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ModelConverterTest {

  private ModelConverter converter;

  @BeforeEach
  void setUp() {
    converter = new ModelConverter();
  }

  @Test
  void convertToDatabaseColumn_nullOrEmpty_returnsNull() {
    assertNull(converter.convertToDatabaseColumn(null));
    assertNull(converter.convertToDatabaseColumn(Collections.emptyList()));
  }

  @Test
  void convertToDatabaseColumn_nonEmptyList_returnsCommaSeparatedString() {
    List<Long> input = Arrays.asList(1L, 2L, 3L);
    String result = converter.convertToDatabaseColumn(input);
    assertEquals("1,2,3", result);
  }

  @Test
  void convertToEntityAttribute_null_returnsNull() {
    assertNull(converter.convertToEntityAttribute(null));
  }

  @Test
  void convertToEntityAttribute_validString_returnsListOfLongs() {
    String input = "1,2,3";
    List<Long> expected = Arrays.asList(1L, 2L, 3L);
    List<Long> result = converter.convertToEntityAttribute(input);
    assertEquals(expected, result);
  }

  @Test
  void convertToEntityAttribute_stringWithSpaces_throwsNumberFormatException() {
    String input = " 1 , 2 , 3 ";
    assertThrows(NumberFormatException.class, () -> converter.convertToEntityAttribute(input));
  }
}
