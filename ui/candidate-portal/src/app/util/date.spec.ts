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

import {toDateOnly} from './date';

describe('toDateOnly', () => {
  it('returns the Unix epoch for a null date', () => {
    expect(toDateOnly(null)).toEqual(new Date(0));
  });

  it('removes the time and returns the local date components in UTC', () => {
    const original = new Date(2026, 6, 27, 18, 45, 30);

    expect(toDateOnly(original)).toEqual(new Date(Date.UTC(2026, 6, 27)));
  });
});
