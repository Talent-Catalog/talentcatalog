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


import {
  enumKeys,
  enumKeysToEnumOptions,
  EnumOption,
  enumOptions,
  enumStringValues, getOrdinal,
  isEnumOption, isEnumOptionArray
} from "./enum";

enum CandidateStatus {
  draft = "draft (inactive)",
  active = "active",
  pending = "pending",
}

describe('Enum Utilities', () => {
  const candidateStatusEnum = CandidateStatus;

  describe('enumOptions', () => {
    it('should convert an enum to an array of EnumOptions', () => {
      const result = enumOptions(candidateStatusEnum);
      expect(result).toEqual([
        { key: 'draft', stringValue: 'draft (inactive)' },
        { key: 'active', stringValue: 'active' },
        { key: 'pending', stringValue: 'pending' },
      ]);
    });
  });

  describe('enumKeysToEnumOptions', () => {
    it('should convert an array of keys to an array of EnumOptions', () => {
      const keys = ['draft', 'active', 'pending'];
      const result = enumKeysToEnumOptions(keys, candidateStatusEnum);
      expect(result).toEqual([
        { key: 'draft', stringValue: 'draft (inactive)' },
        { key: 'active', stringValue: 'active' },
        { key: 'pending', stringValue: 'pending' },
      ]);
    });

    it('should handle an empty array', () => {
      const result = enumKeysToEnumOptions([], candidateStatusEnum);
      expect(result).toEqual([]);
    });
  });

  describe('enumKeys', () => {
    it('should return all keys in the enum', () => {
      const result = enumKeys(candidateStatusEnum);
      expect(result).toEqual(['draft', 'active', 'pending']);
    });
  });

  describe('enumStringValues', () => {
    it('should return all string values in the enum', () => {
      const result = enumStringValues(candidateStatusEnum);
      expect(result).toEqual(['draft (inactive)', 'active', 'pending']);
    });
  });

  describe('isEnumOption', () => {
    it('should return true for valid EnumOption', () => {
      const validOption: EnumOption = { key: 'draft', stringValue: 'draft (inactive)' };
      expect(isEnumOption(validOption)).toBeTrue();
    });

    it('should return false for invalid EnumOption', () => {
      const invalidOption = { key: 'draft' };
      expect(isEnumOption(invalidOption)).toBeFalse();
    });
  });

  describe('isEnumOptionArray', () => {
    it('should return true for valid EnumOption array', () => {
      const validOptionArray: EnumOption[] = [
        { key: 'draft', stringValue: 'draft (inactive)' },
        { key: 'active', stringValue: 'active' },
      ];
      expect(isEnumOptionArray(validOptionArray)).toBeTrue();
    });

    it('should return false for invalid EnumOption array', () => {
      const invalidOptionArray = [{ key: 'draft' }];
      expect(isEnumOptionArray(invalidOptionArray)).toBeFalse();
    });

    it('should return false for non-array input', () => {
      expect(isEnumOptionArray('not an array')).toBeFalse();
    });

    it('should return false for an array of numbers', () => {
      expect(isEnumOptionArray([1, 2, 3])).toBeFalse();
    });
  });

  describe('getOrdinal', () => {
    it('should return the index of a key in the enum', () => {
      const result = getOrdinal(candidateStatusEnum, 'active');
      expect(result).toBe(1);
    });

    it('should return -1 for a non-existent key', () => {
      const result = getOrdinal(candidateStatusEnum, 'nonExistent');
      expect(result).toBe(-1);
    });
  });
});
