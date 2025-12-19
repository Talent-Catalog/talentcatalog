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

import {SearchResults} from './search-results';

interface DummyResult {
  id: number;
  name: string;
}

describe('SearchResults', () => {
  let searchResults: SearchResults<DummyResult>;

  beforeEach(() => {
    searchResults = new SearchResults<DummyResult>();
  });

  it('should create an instance of SearchResults', () => {
    expect(searchResults).toBeTruthy();
  });

  it('should allow setting and getting the number property', () => {
    searchResults.number = 1;
    expect(searchResults.number).toBe(1);
  });

  it('should allow setting and getting the size property', () => {
    searchResults.size = 10;
    expect(searchResults.size).toBe(10);
  });

  it('should allow setting and getting the totalElements property', () => {
    searchResults.totalElements = 100;
    expect(searchResults.totalElements).toBe(100);
  });

  it('should allow setting and getting the totalPages property', () => {
    searchResults.totalPages = 10;
    expect(searchResults.totalPages).toBe(10);
  });

  it('should allow setting and getting the first property', () => {
    searchResults.first = true;
    expect(searchResults.first).toBeTrue();
  });

  it('should allow setting and getting the last property', () => {
    searchResults.last = false;
    expect(searchResults.last).toBeFalse();
  });

  it('should allow setting and getting the content property', () => {
    const content: DummyResult[] = [{ id: 1, name: 'Result 1' }, { id: 2, name: 'Result 2' }];
    searchResults.content = content;
    expect(searchResults.content).toBe(content);
  });

  it('should handle empty content', () => {
    searchResults.content = [];
    expect(searchResults.content.length).toBe(0);
  });

  it('should handle null content', () => {
    searchResults.content = null;
    expect(searchResults.content).toBeNull();
  });
});
