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
import {dateString, toDateOnly} from "./date-adapter";

describe('Date utilities', () => {

  describe('dateString', () => {
    it('should format a date using the expected display format', () => {
      const date = new Date(2026, 6, 27, 12, 30);

      expect(dateString(date)).toBe('27 Jul 26');
    });
  });

  describe('toDateOnly', () => {
    it('should return the Unix epoch when the supplied date is null', () => {
      const result = toDateOnly(null);

      expect(result).toEqual(new Date(0));
      expect(result.getTime()).toBe(0);
    });

    it('should remove the time and return the date at UTC midnight', () => {
      const date = new Date(2026, 6, 27, 15, 30, 45);

      const result = toDateOnly(date);

      expect(result).toEqual(new Date(Date.UTC(2026, 6, 27)));
    });

    it('should accept a date string', () => {
      const result = toDateOnly('2026-07-27T18:45:30');

      expect(result).toEqual(new Date(Date.UTC(2026, 6, 27)));
    });

    it('should handle other falsy values as an empty date', () => {
      expect(toDateOnly(undefined)).toEqual(new Date(0));
      expect(toDateOnly('')).toEqual(new Date(0));
    });
  });
});
