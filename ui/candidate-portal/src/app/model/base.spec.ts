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

import {sanitizeEmailInput} from './base';

describe('sanitizeEmailInput', () => {
  it('returns a clean email address unchanged', () => {
    expect(sanitizeEmailInput('example@example.com')).toBe('example@example.com');
  });

  it('trims leading and trailing whitespace', () => {
    expect(sanitizeEmailInput('  example@example.com  ')).toBe('example@example.com');
  });

  it('leaves an ordinary space embedded in the middle of the address untouched', () => {
    expect(sanitizeEmailInput('exam ple@example.com')).toBe('exam ple@example.com');
  });

  it('returns an empty string unchanged', () => {
    expect(sanitizeEmailInput('')).toBe('');
  });

  it('returns null unchanged', () => {
    expect(sanitizeEmailInput(null)).toBeNull();
  });

  it('returns undefined unchanged', () => {
    expect(sanitizeEmailInput(undefined)).toBeUndefined();
  });
});
