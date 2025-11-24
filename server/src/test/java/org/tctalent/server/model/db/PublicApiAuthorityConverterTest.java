package org.tctalent.server.model.db;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;
import java.util.HashSet;
import org.junit.jupiter.api.Test;

public class PublicApiAuthorityConverterTest {

  private final PublicApiAuthorityConverter converter = new PublicApiAuthorityConverter();

  @Test
  void convertToDatabaseColumn_nullOrEmpty_returnsEmptyString() {
    assertEquals("", converter.convertToDatabaseColumn(null));
    assertEquals("", converter.convertToDatabaseColumn(new HashSet<>()));
  }

  @Test
  void convertToDatabaseColumn_nonEmpty_returnsCommaSeparatedString() {
    Set<PublicApiAuthority> input = Set.of(
        PublicApiAuthority.READ_CANDIDATE_DATA,
        PublicApiAuthority.ADMIN
    );
    String result = converter.convertToDatabaseColumn(input);
    // Order in a Set is not guaranteed, so check contains both substrings and comma separated
    assertTrue(result.contains("READ_CANDIDATE_DATA"));
    assertTrue(result.contains("ADMIN"));
    assertTrue(result.contains(","));
  }

  @Test
  void convertToEntityAttribute_nullOrEmpty_returnsEmptySet() {
    assertTrue(converter.convertToEntityAttribute(null).isEmpty());
    assertTrue(converter.convertToEntityAttribute("").isEmpty());
    assertTrue(converter.convertToEntityAttribute("   ").isEmpty());
  }

  @Test
  void convertToEntityAttribute_validString_returnsSet() {
    String dbData = "READ_CANDIDATE_DATA, ADMIN";
    Set<PublicApiAuthority> expected = Set.of(
        PublicApiAuthority.READ_CANDIDATE_DATA,
        PublicApiAuthority.ADMIN
    );

    Set<PublicApiAuthority> result = converter.convertToEntityAttribute(dbData);
    assertEquals(expected, result);
  }

  @Test
  void convertToEntityAttribute_invalidValue_throwsIllegalArgumentException() {
    String dbData = "READ_CANDIDATE_DATA, INVALID_AUTHORITY";

    IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
      converter.convertToEntityAttribute(dbData);
    });

    assertTrue(thrown.getMessage().contains("No enum constant"));
  }
}
