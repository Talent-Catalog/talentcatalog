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

import {generateYearArray} from "./year-helper";

describe('generateYearArray', () => {

  it('should generate an array of years from 1950 to the current year by default', () => {
    const currentYear = new Date().getFullYear();
    const result = generateYearArray();
    const expected = [];
    for (let year = 1950; year <= currentYear; year++) {
      expected.push(year);
    }
    expect(result).toEqual(expected);
  });

  it('should generate an array of years from the specified start year to the current year', () => {
    const startYear = 2000;
    const currentYear = new Date().getFullYear();
    const result = generateYearArray(startYear);
    const expected = [];
    for (let year = startYear; year <= currentYear; year++) {
      expected.push(year);
    }
    expect(result).toEqual(expected);
  });

  it('should generate an array of years from the specified start year to the specified end year', () => {
    const startYear = 2000;
    const endYear = 2010;
    const result = generateYearArray(startYear, false, endYear);
    const expected = [];
    for (let year = startYear; year <= endYear; year++) {
      expected.push(year);
    }
    expect(result).toEqual(expected);
  });

  it('should generate an array of years from the specified start year to the end year with an offset', () => {
    const startYear = 2000;
    const endYear = 2010;
    const endYearOffset = 5;
    const result = generateYearArray(startYear, false, endYear, endYearOffset);
    const expected = [];
    for (let year = startYear; year <= endYear + endYearOffset; year++) {
      expected.push(year);
    }
    expect(result).toEqual(expected);
  });

  it('should generate an array of years in reverse order when reverse flag is true', () => {
    const startYear = 2000;
    const endYear = 2010;
    const result = generateYearArray(startYear, true, endYear);
    const expected = [];
    for (let year = endYear; year >= startYear; year--) {
      expected.push(year);
    }
    expect(result).toEqual(expected);
  });

  it('should generate an array of years from default start year to the current year with offset', () => {
    const currentYear = new Date().getFullYear();
    const endYearOffset = 5;
    const result = generateYearArray(undefined, false, undefined, endYearOffset);
    const expected = [];
    for (let year = 1950; year <= currentYear + endYearOffset; year++) {
      expected.push(year);
    }
    expect(result).toEqual(expected);
  });

});
