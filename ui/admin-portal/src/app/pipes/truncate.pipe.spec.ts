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

import {TruncatePipe} from './truncate.pipe';

describe('TruncatePipe', () => {
  let pipe: TruncatePipe;

  beforeEach(() => {
    pipe = new TruncatePipe();
  });

  it('should create an instance', () => {
    expect(pipe).toBeTruthy();
  });

  it('should truncate text to specified length with default suffix', () => {
    const inputText = 'Lorem ipsum dolor sit amet';
    const length = 10;
    const expectedOutput = 'Lorem ipsu...';

    const result = pipe.transform(inputText, length);

    expect(result).toEqual(expectedOutput);
  });

  it('should not truncate text shorter than specified length', () => {
    const inputText = 'Short text';
    const length = 20;

    const result = pipe.transform(inputText, length);

    expect(result).toEqual(inputText);
  });

  it('should handle empty input', () => {
    const inputText = '';
    const length = 10;

    const result = pipe.transform(inputText, length);

    expect(result).toEqual(inputText);
  });

  it('should handle null input', () => {
    const inputText = null;
    const length = 10;

    const result = pipe.transform(inputText, length);

    expect(result).toEqual(inputText);
  });

  it('should handle undefined input', () => {
    const inputText = undefined;
    const length = 10;

    const result = pipe.transform(inputText, length);

    expect(result).toEqual(inputText);
  });

  it('should handle custom suffix', () => {
    const inputText = 'Long text that needs truncation';
    const length = 15;
    const customSuffix = '... (read more)';
    const expectedOutput = 'Long text that... (read more)';

    const result = pipe.transform(inputText, length, customSuffix);

    expect(result).toEqual(expectedOutput);
  });
});
