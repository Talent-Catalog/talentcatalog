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

export class CandidateNumberParser {

  /**
   * Normalises candidate numbers input into a clean string array.
   */
  public static parseCandidateNumbers(rawData): string[] {

    // 1) Null / undefined → no candidate numbers
    if (rawData == null) {
      return [];
    }

    // 2) Be lenient if backend / form ever sends an array already
    if (Array.isArray(rawData)) {
      return rawData
      .map(value => String(value).trim())
      .filter(value => value.length > 0);
    }

    // 3) Fallback: treat as a single string and split it
    const text = String(rawData).trim();
    if (!text) {
      return [];
    }

    return text
    // Split on commas and/or whitespace: "123, 456  789" → ["123", "456", "789"]
    .split(/[\s,]+/)
    .map(token => token.trim())
    .filter(token => token.length > 0);
  }
}
