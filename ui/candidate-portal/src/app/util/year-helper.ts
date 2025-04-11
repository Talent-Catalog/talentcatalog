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

export function generateYearArray(startYear: number = 1950,
                                  reverse: boolean = true,
                                  endYear?: number,
                                  endYearOffset: number = 0): number[] {
  // Set sensible defaults
  if (!endYear) {
    endYear = new Date().getFullYear();
  }
  endYear += endYearOffset;

  // Generate array in preferred order
  const years = [];
  for (let year = startYear; year <= endYear; year++) {
    years.push(year);
  }

  if (reverse) {
    years.reverse();
  }

  return years;
}
