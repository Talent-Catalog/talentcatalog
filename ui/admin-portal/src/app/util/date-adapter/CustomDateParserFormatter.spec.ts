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

import {CustomDateParserFormatter} from "./ngb-date-adapter";
import {NgbDateStruct} from "@ng-bootstrap/ng-bootstrap";

describe('CustomDateParserFormatter', () => {
  let formatter: CustomDateParserFormatter;

  beforeEach(() => {
    formatter = new CustomDateParserFormatter();
  });

  it('should create an instance', () => {
    expect(formatter).toBeTruthy();
  });

  it('should parse string to NgbDateStruct', () => {
    const dateString = '2023-07-09';
    const expectedDate: NgbDateStruct = { year: 2023, month: 7, day: 9 };
    const parsedDate = formatter.parse(dateString);
    expect(parsedDate).toEqual(expectedDate);
  });

  it('should return null when parsing empty string', () => {
    const dateString = '';
    const parsedDate = formatter.parse(dateString);
    expect(parsedDate).toBeNull();
  });

  it('should format NgbDateStruct to string', () => {
    const dateStruct: NgbDateStruct = { year: 2023, month: 7, day: 9 };
    const expectedDateString = '2023-07-09';
    const formattedDateString = formatter.format(dateStruct);
    expect(formattedDateString).toEqual(expectedDateString);
  });

  it('should return null when formatting null NgbDateStruct', () => {
    const dateStruct = null;
    const formattedDateString = formatter.format(dateStruct);
    expect(formattedDateString).toBeNull();
  });
});
