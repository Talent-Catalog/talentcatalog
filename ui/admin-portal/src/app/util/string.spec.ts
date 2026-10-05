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

import {hasTextContent, isHtml, truncate} from "./string";

describe('Utility Functions', () => {
  describe('truncate', () => {
    it('should truncate the string to the specified length and add "..." at the end', () => {
      const str = 'This is a long string that needs to be truncated';
      const num = 20;
      const result = truncate(str, num);
      expect(result).toBe('This is a long strin...');
    });

    it('should return the original string if its length is less than or equal to the specified length', () => {
      const str = 'Short string';
      const num = 20;
      const result = truncate(str, num);
      expect(result).toBe(str);
    });

    it('should return the original string if the string is empty', () => {
      const str = '';
      const num = 20;
      const result = truncate(str, num);
      expect(result).toBe(str);
    });

    it('should handle undefined or null strings', () => {
      const str = null;
      const num = 20;
      const result = truncate(str, num);
      expect(result).toBe(str);

      const str2 = undefined;
      const result2 = truncate(str2, num);
      expect(result2).toBe(str2);
    });
  });

  describe('isHtml', () => {
    it('should return true for a string containing HTML tags', () => {
      const text = '<p>This is a paragraph.</p>';
      const result = isHtml(text);
      expect(result).toBe(true);
    });

    it('should return false for a string without HTML tags', () => {
      const text = 'This is a plain text.';
      const result = isHtml(text);
      expect(result).toBe(false);
    });

    it('should return false for an empty string', () => {
      const text = '';
      const result = isHtml(text);
      expect(result).toBe(false);
    });

    it('should return false for null or undefined inputs', () => {
      const text = null;
      const result = isHtml(text);
      expect(result).toBe(false);

      const text2 = undefined;
      const result2 = isHtml(text2);
      expect(result2).toBe(false);
    });

    it('should return true for a string containing self-closing HTML tags', () => {
      const text = '<img src="image.jpg" />';
      const result = isHtml(text);
      expect(result).toBe(true);
    });
  });

  describe('hasTextContent', () => {
    it('should be false for null, undefined and empty text', () => {
      expect(hasTextContent(null)).toBeFalse();
      expect(hasTextContent(undefined)).toBeFalse();
      expect(hasTextContent('')).toBeFalse();
    });

    it('should be false for whitespace only', () => {
      expect(hasTextContent('   \n\t ')).toBeFalse();
    });

    it('should be false for HTML with no visible text', () => {
      expect(hasTextContent('<p></p>')).toBeFalse();
      expect(hasTextContent('<p><br></p>')).toBeFalse();
      expect(hasTextContent('<p>&nbsp;</p>')).toBeFalse();
      expect(hasTextContent('<div> <p>  </p> </div>')).toBeFalse();
    });

    it('should be true for plain text', () => {
      expect(hasTextContent('Java developer')).toBeTrue();
    });

    it('should be true for HTML containing text', () => {
      expect(hasTextContent('<p>Java <b>developer</b></p>')).toBeTrue();
    });
  });
});
