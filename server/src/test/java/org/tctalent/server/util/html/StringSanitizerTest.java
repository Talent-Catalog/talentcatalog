/*
 * Copyright (c) 2025 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.util.html;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

public class StringSanitizerTest {

  @Test
  public void testControlCharacterRemoval() {
    // Sample string with control characters
    String testString =
        "Hello" + (char)0x00 + " World " + (char)0x01 + "with control" + (char)0x0C + "characters";

    // Before sanitizing
    System.out.println("Original length: " + testString.length());
    System.out.println("Original (hex): " + stringToHex(testString));

    // Run sanitizer
    String sanitized = StringSanitizer.replaceControlCharacters(testString);

    // After sanitizing
    assert sanitized != null;
    System.out.println("Sanitized length: " + sanitized.length());
    System.out.println("Sanitized (hex): " + stringToHex(sanitized));

    // Verify control characters removed
    assertEquals("Hello World with controlcharacters", sanitized);
    assertEquals(testString.length() - 3, sanitized.length());

    // Ensure null handled properly
    assertNull(StringSanitizer.replaceControlCharacters(null));
  }

  // Makes control characters visible to human reader
  private String stringToHex(String input) {
    if (input == null) {
      return "null";
    }

    StringBuilder result = new StringBuilder();
    for (int i = 0; i < input.length(); i++) {
      char c = input.charAt(i);
      // '0x20' = hexadecimal representation of 32 - anything below 32 is a control character.
      if (c < 0x20 && c != '\n' && c != '\r' && c != '\t') {
        result.append(String.format("[\\x%02X]", (int)c));
      } else {
        result.append(c);
      }
    }
    return result.toString();
  }

}
