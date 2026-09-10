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


import {isHtml, truncate} from './string';

describe('string utilities', () => {
  describe('truncate', () => {
    it('truncates a string longer than the maximum length', () => {
      expect(truncate('Talent Catalog', 6)).toBe('Talent...');
    });

    it('returns a string unchanged when it is not longer than the maximum', () => {
      expect(truncate('Talent', 6)).toBe('Talent');
    });

    it('returns an empty string unchanged', () => {
      expect(truncate('', 6)).toBe('');
    });
  });

  describe('isHtml', () => {
    it('returns true when the text contains an HTML tag', () => {
      expect(isHtml('<strong>Talent</strong>')).toBeTrue();
    });

    it('returns false for plain text', () => {
      expect(isHtml('Talent Catalog')).toBeFalse();
    });
  });
});
