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

import {TruncatePipe} from './truncate.pipe';

describe('TruncatePipe', () => {
  let pipe: TruncatePipe;

  beforeEach(() => {
    pipe = new TruncatePipe();
  });

  it('should create an instance', () => {
    expect(pipe).toBeTruthy();
  });

  it('should use the default length and suffix', () => {
    const text = '12345678901234567890EXTRA';

    expect(pipe.transform(text))
    .toBe('12345678901234567890...');
  });

  it('should truncate text using a custom length', () => {
    expect(pipe.transform('Talent Catalog', 6))
    .toBe('Talent...');
  });

  it('should use a custom suffix', () => {
    expect(pipe.transform('Talent Catalog', 6, '…'))
    .toBe('Talent…');
  });

  it('should trim whitespace from the truncated portion', () => {
    expect(pipe.transform('Talent Catalog', 7, '...'))
    .toBe('Talent...');
  });

  it('should not truncate text shorter than the specified length', () => {
    expect(pipe.transform('Short', 10))
    .toBe('Short');
  });

  it('should not truncate text equal to the specified length', () => {
    expect(pipe.transform('12345', 5))
    .toBe('12345');
  });

  it('should return an empty string unchanged', () => {
    expect(pipe.transform('')).toBe('');
  });

  it('should return null unchanged', () => {
    expect(pipe.transform(null as any)).toBeNull();
  });

  it('should return undefined unchanged', () => {
    expect(pipe.transform(undefined as any)).toBeUndefined();
  });

  it('should support an empty custom suffix', () => {
    expect(pipe.transform('Talent Catalog', 6, ''))
    .toBe('Talent');
  });
});
