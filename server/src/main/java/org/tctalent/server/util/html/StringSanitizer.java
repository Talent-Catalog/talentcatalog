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


/**
 * Utility class for sanitizing strings by handling special characters.
 * <p>
 * Currently, this class provides methods to remove or replace Line Separator (LSEP) characters.
 * In the future, it may be extended to handle other special characters as well.
 * </p>
 */
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

}
