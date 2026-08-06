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
import {
  enumKeys,
  enumKeysToEnumOptions,
  EnumOption,
  enumOptions,
  enumStringValues,
  getOrdinal,
  isEnumOption,
  isEnumOptionArray
} from './enum';

enum CandidateStatus {
  draft = 'draft (inactive)',
  active = 'active',
  pending = 'pending'
}

describe('Enum utilities', () => {
  describe('enumOptions', () => {
    it('should convert an enum into options', () => {
      expect(enumOptions(CandidateStatus)).toEqual([
        {
          key: 'draft',
          stringValue: 'draft (inactive)'
        },
        {
          key: 'active',
          stringValue: 'active'
        },
        {
          key: 'pending',
          stringValue: 'pending'
        }
      ]);
    });

    it('should return an empty array for an empty object', () => {
      expect(enumOptions({})).toEqual([]);
    });
  });

  describe('enumKeysToEnumOptions', () => {
    it('should convert selected keys into options', () => {
      expect(
        enumKeysToEnumOptions(
          ['active', 'draft'],
          CandidateStatus
        )
      ).toEqual([
        {
          key: 'active',
          stringValue: 'active'
        },
        {
          key: 'draft',
          stringValue: 'draft (inactive)'
        }
      ]);
    });

    it('should preserve the supplied key order', () => {
      const result = enumKeysToEnumOptions(
        ['pending', 'active', 'draft'],
        CandidateStatus
      );

      expect(result.map(option => option.key)).toEqual([
        'pending',
        'active',
        'draft'
      ]);
    });

    it('should return an empty array for empty keys', () => {
      expect(
        enumKeysToEnumOptions([], CandidateStatus)
      ).toEqual([]);
    });

    it('should return undefined when keys are undefined', () => {
      expect(
        enumKeysToEnumOptions(
          undefined as any,
          CandidateStatus
        )
      ).toBeUndefined();
    });

    it('should use undefined for an unknown enum key', () => {
      expect(
        enumKeysToEnumOptions(
          ['unknown'],
          CandidateStatus
        )
      ).toEqual([
        {
          key: 'unknown',
          stringValue: undefined
        }
      ]);
    });
  });

  describe('enumKeys', () => {
    it('should return all enum keys', () => {
      expect(enumKeys(CandidateStatus)).toEqual([
        'draft',
        'active',
        'pending'
      ]);
    });

    it('should return an empty array for an empty object', () => {
      expect(enumKeys({})).toEqual([]);
    });
  });

  describe('enumStringValues', () => {
    it('should return all enum string values', () => {
      expect(enumStringValues(CandidateStatus)).toEqual([
        'draft (inactive)',
        'active',
        'pending'
      ]);
    });

    it('should return an empty array for an empty object', () => {
      expect(enumStringValues({})).toEqual([]);
    });
  });

  describe('isEnumOption', () => {
    it('should return true for a valid EnumOption', () => {
      const option: EnumOption = {
        key: 'draft',
        stringValue: 'draft (inactive)'
      };

      expect(isEnumOption(option)).toBeTrue();
    });

    it('should return true even when property values are empty', () => {
      expect(
        isEnumOption({
          key: '',
          stringValue: ''
        })
      ).toBeTrue();
    });

    it('should return false when key is missing', () => {
      expect(
        isEnumOption({
          stringValue: 'active'
        })
      ).toBeFalse();
    });

    it('should return false when stringValue is missing', () => {
      expect(
        isEnumOption({
          key: 'active'
        })
      ).toBeFalse();
    });

    it('should return false for null', () => {
      expect(isEnumOption(null)).toBeFalse();
    });

    it('should return false for undefined', () => {
      expect(isEnumOption(undefined)).toBeFalse();
    });

    it('should return false for primitive values', () => {
      expect(isEnumOption('active')).toBeFalse();
      expect(isEnumOption(1)).toBeFalse();
      expect(isEnumOption(true)).toBeFalse();
    });
  });

  describe('isEnumOptionArray', () => {
    it('should return true for an EnumOption array', () => {
      const options: EnumOption[] = [
        {
          key: 'draft',
          stringValue: 'draft (inactive)'
        },
        {
          key: 'active',
          stringValue: 'active'
        }
      ];

      expect(isEnumOptionArray(options)).toBeTrue();
    });

    it('should return false for an empty array', () => {
      expect(isEnumOptionArray([])).toBeFalse();
    });

    it('should return false for a non-array object', () => {
      expect(
        isEnumOptionArray({
          key: 'active',
          stringValue: 'active'
        })
      ).toBeFalse();
    });

    it('should return false for null and undefined', () => {
      expect(isEnumOptionArray(null)).toBeFalse();
      expect(
        isEnumOptionArray(undefined)
      ).toBeFalse();
    });

    it('should return false when the first option is invalid', () => {
      expect(
        isEnumOptionArray([
          {
            key: 'active'
          }
        ])
      ).toBeFalse();
    });

    it('should return false for an array of numbers', () => {
      expect(isEnumOptionArray([1, 2, 3])).toBeFalse();
    });

    it('should return false when the first item is a numeric string', () => {
      expect(
        isEnumOptionArray(['123'] as any)
      ).toBeFalse();
    });

    it('should return false when the first item is a non-numeric string', () => {
      expect(
        isEnumOptionArray(['active'] as any)
      ).toBeFalse();
    });
  });

  describe('getOrdinal', () => {
    it('should return the ordinal of an existing key', () => {
      expect(
        getOrdinal(CandidateStatus, 'active')
      ).toBe(1);
    });

    it('should return zero for the first key', () => {
      expect(
        getOrdinal(CandidateStatus, 'draft')
      ).toBe(0);
    });

    it('should return minus one for an unknown key', () => {
      expect(
        getOrdinal(CandidateStatus, 'unknown')
      ).toBe(-1);
    });
  });
});
