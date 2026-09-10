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

import {CustomDateAdapter} from './ngb-date-adapter';
import {NgbDateStruct} from '@ng-bootstrap/ng-bootstrap';

describe('CustomDateAdapter', () => {
  let adapter: CustomDateAdapter;

  beforeEach(() => {
    adapter = new CustomDateAdapter();
  });

  it('should create an instance', () => {
    expect(adapter).toBeTruthy();
  });

  describe('fromModel', () => {
    it('should convert a date string to NgbDateStruct', () => {
      expect(adapter.fromModel('2026-07-27')).toEqual({
        year: 2026,
        month: 7,
        day: 27
      });
    });

    it('should convert date values to numbers', () => {
      const result = adapter.fromModel('2026-01-09');

      expect(result.year).toBe(2026);
      expect(result.month).toBe(1);
      expect(result.day).toBe(9);
    });

    it('should return null for an empty value', () => {
      expect(adapter.fromModel('')).toBeNull();
    });

    it('should return null for null', () => {
      expect(adapter.fromModel(null as any)).toBeNull();
    });
  });

  describe('toModel', () => {
    it('should convert NgbDateStruct to a date string', () => {
      const date: NgbDateStruct = {
        year: 2026,
        month: 7,
        day: 27
      };

      expect(adapter.toModel(date)).toBe('2026-07-27');
    });

    it('should add leading zeros to single-digit months and days', () => {
      const date: NgbDateStruct = {
        year: 2026,
        month: 1,
        day: 9
      };

      expect(adapter.toModel(date)).toBe('2026-01-09');
    });

    it('should return null for a null date', () => {
      expect(adapter.toModel(null as any)).toBeNull();
    });
  });
});
