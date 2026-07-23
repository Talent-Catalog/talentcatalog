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

import {ExtendDatePipe} from './date-pipe';

describe('ExtendDatePipe', () => {
  let pipe: ExtendDatePipe;
  const date = new Date(2026, 0, 15);

  beforeEach(() => {
    pipe = new ExtendDatePipe('en-US');
  });

  it('should use a configured custom format', () => {
    expect(pipe.transform(date, 'customMonthYear')).toBe('Jan 26');
  });

  it('should fall back to the supplied standard format', () => {
    expect(pipe.transform(date, 'yyyy')).toBe('2026');
  });
});
