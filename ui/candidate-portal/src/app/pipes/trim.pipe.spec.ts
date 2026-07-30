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

import {TrimPipe} from './trim.pipe';

describe('TrimPipe', () => {

  let pipe: TrimPipe;

  beforeEach(() => {
    pipe = new TrimPipe();
  });

  it('should create an instance', () => {
    expect(pipe).toBeTruthy();
  });

  it('should remove whitespace from the beginning and end', () => {
    expect(pipe.transform('  Talent Catalog  '))
    .toBe('Talent Catalog');
  });

  it('should preserve whitespace inside the value', () => {
    expect(pipe.transform('  Talent   Catalog  '))
    .toBe('Talent   Catalog');
  });

  it('should return the same value when no surrounding whitespace exists', () => {
    expect(pipe.transform('Talent Catalog'))
    .toBe('Talent Catalog');
  });

  it('should return an empty string when the value contains only whitespace', () => {
    expect(pipe.transform('   ')).toBe('');
  });

  it('should ignore additional pipe arguments', () => {
    expect(pipe.transform('  Ehsan  ', 'unused', 123))
    .toBe('Ehsan');
  });
});
