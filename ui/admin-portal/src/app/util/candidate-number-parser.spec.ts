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

import {CandidateNumberParser} from './candidate-number-parser';

describe('CandidateNumberParser', () => {
  describe('parseCandidateNumbers', () => {
    it('should return an empty array for null or undefined input', () => {
      expect(CandidateNumberParser.parseCandidateNumbers(null)).toEqual([]);
      expect(CandidateNumberParser.parseCandidateNumbers(undefined)).toEqual([]);
    });

    it('should normalize, trim, and remove empty array values', () => {
      const result = CandidateNumberParser.parseCandidateNumbers([
        ' 123 ',
        456,
        '',
        '   '
      ]);

      expect(result).toEqual(['123', '456']);
    });

    it('should return an empty array for a blank string', () => {
      expect(CandidateNumberParser.parseCandidateNumbers('   ')).toEqual([]);
    });

    it('should split a string on commas and whitespace', () => {
      const result = CandidateNumberParser.parseCandidateNumbers(
        ' 123, 456  789,\n101112 '
      );

      expect(result).toEqual(['123', '456', '789', '101112']);
    });
  });
});
