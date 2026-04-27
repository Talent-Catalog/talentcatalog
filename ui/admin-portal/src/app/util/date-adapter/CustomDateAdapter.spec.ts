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

import {CustomDateAdapter} from "./ngb-date-adapter";

describe('CustomDateAdapter', () => {
  let adapter: CustomDateAdapter;

  beforeEach(() => {
    adapter = new CustomDateAdapter();
  });

  it('should create an instance', () => {
    expect(adapter).toBeTruthy();
  });

  it('should convert string to NgbDateStruct', () => {
    const dateString = '2023-07-09';
    const expectedDate = { year: 2023, month: 7, day: 9 };
    const convertedDate = adapter.fromModel(dateString);
    expect(convertedDate).toEqual(expectedDate);
  });

  it('should return null when converting empty string', () => {
    const dateString = '';
    const convertedDate = adapter.fromModel(dateString);
    expect(convertedDate).toBeNull();
  });

  it('should convert NgbDateStruct to string', () => {
    const dateStruct = { year: 2023, month: 7, day: 9 };
    const expectedDateString = '2023-07-09';
    const convertedDateString = adapter.toModel(dateStruct);
    expect(convertedDateString).toEqual(expectedDateString);
  });

  it('should return null when converting null NgbDateStruct', () => {
    const dateStruct = null;
    const convertedDateString = adapter.toModel(dateStruct);
    expect(convertedDateString).toBeNull();
  });
});
