/*
 * Copyright (c) 2024 Talent Beyond Boundaries.
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

package org.tctalent.server.util;

public class StringHelper {

  /**
   *   Function to check if a string contains only digits
   */
  public static boolean
  onlyDigits(String str, int n)
  {
    // Traverse the string from start to end
    for (int i = 0; i < n; i++) {

      // Check if character is not a digit between 0-9, return false if so
      if (str.charAt(i) < '0'
          || str.charAt(i) > '9') {
        return false;
      }
    }
    // If we reach here, that means all characters were digits.
    return true;
  }
}
