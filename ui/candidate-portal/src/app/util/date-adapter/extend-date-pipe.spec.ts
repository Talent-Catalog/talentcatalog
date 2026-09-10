/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */
import {ExtendDatePipe} from './extend-date-pipe';

describe('ExtendDatePipe', () => {
  let pipe: ExtendDatePipe;

  beforeEach(() => {
    pipe = new ExtendDatePipe('en-US');
  });

  it('should create the pipe', () => {
    expect(pipe).toBeTruthy();
  });

  it('should use customDefault when no format is provided', () => {
    const result = pipe.transform(
      '2026-07-27T15:30:45Z',
      undefined,
      'UTC'
    );

    expect(result).toBe('2026-07-27');
  });

  it('should use the customDateTime format', () => {
    const result = pipe.transform(
      '2026-07-27T15:30:45Z',
      'customDateTime',
      'UTC'
    );

    expect(result).toBe('2026-07-27, 3:30:45 PM');
  });

  it('should use the customMonthYear format', () => {
    const result = pipe.transform(
      '2026-07-27T15:30:45Z',
      'customMonthYear',
      'UTC'
    );

    expect(result).toBe('Jul 26');
  });

  it('should accept a standard Angular date format', () => {
    const result = pipe.transform(
      '2026-07-27T15:30:45Z',
      'dd/MM/yyyy',
      'UTC'
    );

    expect(result).toBe('27/07/2026');
  });

  it('should transform a Date object', () => {
    const date = new Date('2026-07-27T15:30:45Z');

    expect(pipe.transform(date, 'customDefault', 'UTC'))
    .toBe('2026-07-27');
  });

  it('should transform a numeric timestamp', () => {
    const timestamp = Date.UTC(2026, 6, 27, 15, 30, 45);

    expect(pipe.transform(timestamp, 'customDefault', 'UTC'))
    .toBe('2026-07-27');
  });

  it('should pass the provided locale to DatePipe', () => {
    const result = pipe.transform(
      '2026-07-27T15:30:45Z',
      'MMMM',
      'UTC',
      'en-US'
    );

    expect(result).toBe('July');
  });
});
