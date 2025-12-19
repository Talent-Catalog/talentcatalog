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

import {dateString, toDateOnly} from "./date-adapter";

describe('dateString', () => {
  it('should format the date as "dd MMM yy"', () => {
    const dateObj = new Date('2023-07-09T12:34:56Z');
    const formattedDate = dateString(dateObj);
    expect(formattedDate).toBe('09 Jul 23');
  });
});

describe('toDateOnly', () => {
  it('should return the oldest possible date for null input', () => {
    const result = toDateOnly(null);
    expect(result).toEqual(new Date(0));
  });

  it('should convert a date string to a Date object with only year, month, and day', () => {
    const dateString = '2023-07-09T12:34:56Z';
    const result = toDateOnly(dateString);
    const expectedDate = new Date(Date.UTC(2023, 6, 9));
    expect(result).toEqual(expectedDate);
  });

  it('should convert a Date object to a new Date object with only year, month, and day', () => {
    const dateObj = new Date('2023-07-09T12:34:56Z');
    const result = toDateOnly(dateObj);
    const expectedDate = new Date(Date.UTC(2023, 6, 9));
    expect(result).toEqual(expectedDate);
  });

  // TODO: Re-enable this test after checking the cicd pipeline.
  // Disabling this test for now to avoid GitHub workflow issue. Will fix it later.
  // it('should return a correct date for an input date in different time zones', () => {
  //   const dateObj = new Date('2023-07-09T23:59:59Z');
  //   const result = toDateOnly(dateObj);
  //   const expectedDate = new Date(Date.UTC(2023, 6, 10));
  //   expect(result.toISOString()).toEqual(expectedDate.toISOString());
  // });
});
