/*
 * Copyright (c) 2024 Talent Catalog.
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


import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.tctalent.server.logging.LogBuilder;

/**
 * Utility class for sanitizing strings by handling special characters.
 * <p>
 * Currently, this class provides methods to remove or replace Line Separator (LSEP) characters.
 * In the future, it may be extended to handle other special characters as well.
 * </p>
 */
@Slf4j
public class StringSanitizer {

  private static final String LSEP = "\u2028";

  /**
   * Removes any Line Separator (LSEP) characters from the given string.
   *
   * @param input the input string
   * @return a string with any LSEP characters removed, or null if input was null
   */
  public static String removeLsep(String input) {
    if (input == null) {
      return null;
    }
    // Unicode code point for LSEP is \u2028
    return input.replace(LSEP, "");
  }

  /**
   * Replaces any Line Separator (LSEP) characters in the given string with a {@code <br>} tag.
   *
   * @param input the input string
   * @return a string with any LSEP characters replaced with {@code <br>} tags, or null if input was null
   */
  public static String replaceLsepWithBr(String input) {
    if (input == null) {
      return null;
    }
    // Unicode code point for LSEP is \u2028
    return input.replace(LSEP, "<br>");
  }

  /**
   * Removes control characters from the input string.
   * <p>
   * This method removes ASCII control characters (characters with code points from 0x00-0x1F),
   * except for common whitespace characters: newline (0x0A), carriage return (0x0D) and tab (0x09),
   * which are preserved.
   * <p>
   * If control characters are found and removed, a debug log message is generated with the input
   * string (truncated to 500 characters if it's longer).
   *
   * @param input the string to sanitize, can be null
   * @return the sanitized string with control characters removed, or null if the input was null
   */
  @Nullable
  static String removeControlCharacters(@Nullable String input) {
    if (input == null) {
      return null;
    }

    // Regex range - excludes newline (\n = 0x0A), carriage return (\r = 0x0D) and tab (\t = 0x09)
    String pattern = "[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F]";
    String result = input.replaceAll(pattern, "");

    if (!result.equals(input)) {
      // Input could be really long: truncate anything >= 500 characters
      String truncated = input.length() < 500 ? input : input.substring(0, 500) + "...";

      LogBuilder.builder(log)
          .action("StringSanitizer#replaceControlCharacters")
          .message("Sanitizer removed control characters from input: " + truncated)
          .logDebug();
    }

    return result;
  }

}
