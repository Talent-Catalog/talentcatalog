/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 *  the terms of the GNU General Public License as published by the Free
 *  Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

import {generateYearArray} from './year-helper';

describe('generateYearArray', () => {
  it('uses all defaults and returns years in reverse order', () => {
    const currentYear = new Date().getFullYear();

    const result = generateYearArray();

    expect(result[0]).toBe(currentYear);
    expect(result[result.length - 1]).toBe(1950);
    expect(result.length).toBe(currentYear - 1950 + 1);
  });

  it('returns an explicitly bounded range in ascending order', () => {
    expect(generateYearArray(2020, false, 2023))
    .toEqual([2020, 2021, 2022, 2023]);
  });

  it('applies a positive end-year offset', () => {
    expect(generateYearArray(2020, true, 2022, 2))
    .toEqual([2024, 2023, 2022, 2021, 2020]);
  });

  it('returns an empty array when the start year is after the end year', () => {
    expect(generateYearArray(2025, false, 2024, 0)).toEqual([]);
  });
});
