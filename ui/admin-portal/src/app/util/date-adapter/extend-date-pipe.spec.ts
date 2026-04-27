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

import {ExtendDatePipe} from "./extend-date-pipe";
import {TestBed} from "@angular/core/testing";
import {LOCALE_ID} from "@angular/core";
import {DatePipe} from "@angular/common";

describe('ExtendDatePipe', () => {
  let pipe: ExtendDatePipe;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        ExtendDatePipe,
        { provide: LOCALE_ID, useValue: 'en-US' },
      ]
    });
    pipe = TestBed.inject(ExtendDatePipe);
  });


  it('should create an instance', () => {
    expect(pipe).toBeTruthy();
  });

  it('should transform a Date object using customDefault format', () => {
    const date = new Date('2023-07-09T12:34:56Z');
    const transformedDate = pipe.transform(date);
    expect(transformedDate).toBe('2023-07-09');
  });

  it('should transform a Date object using customMonthYear format', () => {
    const date = new Date('2023-07-09T12:34:56Z');
    const transformedDate = pipe.transform(date, 'customMonthYear');
    expect(transformedDate).toBe('Jul 23');
  });

  it('should transform a string date using customDefault format', () => {
    const dateString = '2023-07-09T12:34:56Z';
    const transformedDate = pipe.transform(dateString);
    expect(transformedDate).toBe('2023-07-09');
  });

  it('should transform a Date object with a different timezone', () => {
    const date = new Date('2023-07-09T12:34:56Z');
    const transformedDate = pipe.transform(date, 'customDefault', 'UTC+5');
    expect(transformedDate).toBe('2023-07-09');
  });

  it('should return null for null input', () => {
    const transformedDate = pipe.transform(null);
    expect(transformedDate).toBe(null);
  });

  it('should return null for undefined input', () => {
    const transformedDate = pipe.transform(undefined);
    expect(transformedDate).toBe(null);
  });

});
