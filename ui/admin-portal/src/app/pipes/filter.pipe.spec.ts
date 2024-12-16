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

import {FilterPipe} from './filter.pipe';

describe('FilterPipe', () => {
  let pipe: FilterPipe;

  beforeEach(() => {
    pipe = new FilterPipe();
  });

  it('create an instance', () => {
    expect(pipe).toBeTruthy();
  });

  it('should return original value when searchText is falsy', () => {
    const value = [
      { user: { firstName: 'John', lastName: 'Doe' }, candidateNumber: '123' },
      { user: { firstName: 'Jane', lastName: 'Smith' }, candidateNumber: '456' }
    ];
    const searchText = '';

    const result = pipe.transform(value, searchText);

    expect(result).toEqual(value);
  });

  it('should filter by full name', () => {
    const value = [
      { user: { firstName: 'John', lastName: 'Doe' }, candidateNumber: '123' },
      { user: { firstName: 'Jane', lastName: 'Smith' }, candidateNumber: '456' }
    ];
    const searchText = 'Jane';

    const result = pipe.transform(value, searchText);

    expect(result.length).toEqual(1);
    expect(result[0].user.firstName).toEqual('Jane');
  });

  it('should filter by candidate number', () => {
    const value = [
      { user: { firstName: 'John', lastName: 'Doe' }, candidateNumber: '123' },
      { user: { firstName: 'Jane', lastName: 'Smith' }, candidateNumber: '456' }
    ];
    const searchText = '456';

    const result = pipe.transform(value, searchText);

    expect(result.length).toEqual(1);
    expect(result[0].candidateNumber).toEqual('456');
  });

  it('should handle undefined searchText', () => {
    const value = [
      { user: { firstName: 'John', lastName: 'Doe' }, candidateNumber: '123' },
      { user: { firstName: 'Jane', lastName: 'Smith' }, candidateNumber: '456' }
    ];
    const searchText = undefined;

    const result = pipe.transform(value, searchText);

    expect(result).toEqual(value);
  });

  it('should handle NaN searchText', () => {
    const value = [
      { user: { firstName: 'John', lastName: 'Doe' }, candidateNumber: '123' },
      { user: { firstName: 'Jane', lastName: 'Smith' }, candidateNumber: '456' }
    ];
    const searchText = 'xyz';

    const result = pipe.transform(value, searchText);

    expect(result.length).toEqual(0);
  });
});
