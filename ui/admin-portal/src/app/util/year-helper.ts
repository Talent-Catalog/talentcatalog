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

export function generateYearArray(startYear?: number,
                                  reverse?: boolean,
                                  endYear?: number,
                                  endYearOffset?: number): number[] {
  // Set sensible defaults
  const start = startYear || 1950;
  const end = (endYear || new Date().getFullYear()) + (endYearOffset || 0);

  // Generate array in preferred order
  const years = [];
  for (let year = start; year <= end; year++) {
    years.push(year);
  }

  if (reverse) {
    years.reverse();
  }

  return years;
}
