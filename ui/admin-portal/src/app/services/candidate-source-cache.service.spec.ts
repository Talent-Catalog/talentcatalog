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

import {TestBed} from '@angular/core/testing';
import {CandidateSourceCacheService} from './candidate-source-cache.service';
import {CandidateSource} from "../model/base";
import {SavedSearch, SavedSearchType} from "../model/saved-search";

describe('CandidateSourceCacheService', () => {
  let service: CandidateSourceCacheService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [CandidateSourceCacheService]
    });
    service = TestBed.inject(CandidateSourceCacheService);

    spyOn(localStorage, 'setItem');
    spyOn(localStorage, 'getItem').and.returnValue(null);
    spyOn(localStorage, 'removeItem');
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('cacheKey', () => {
    it('should generate the correct cache key for a saved search', () => {
      const source: SavedSearch = { id: 123, savedSearchType: SavedSearchType.other } as SavedSearch;

      const expectedKey = 'SearchInfo123';
      const result = service.cacheKey(source);

      expect(result).toBe(expectedKey);
    });

    it('should generate the correct cache key for a list', () => {
      const source: CandidateSource = { id: 456 } as CandidateSource;

      const expectedKey = 'ListInfo456';
      const result = service.cacheKey(source);

      expect(result).toBe(expectedKey);
    });
  });

  describe('cache', () => {
    it('should cache source with the correct key and update cacheKeys', () => {
      const source: CandidateSource = { id: 1 } as CandidateSource;
      const cacheKey = service.cacheKey(source);
      service.cache(cacheKey, source);

      expect(localStorage.setItem).toHaveBeenCalledWith('tc-admin-' + cacheKey, JSON.stringify(source));
      expect(service['cacheKeys'].has(cacheKey)).toBeTrue();
    });
  });

  describe('getFromCache', () => {
    it('should retrieve cached source with the correct key', () => {
      const source: CandidateSource = { id: 1 } as CandidateSource;
      const cacheKey = service.cacheKey(source);
      (localStorage.getItem as jasmine.Spy).and.returnValue(JSON.stringify(source));

      const result = service.getFromCache(cacheKey);

      expect(result).toEqual(source);
      expect(localStorage.getItem).toHaveBeenCalledWith('tc-admin-' + cacheKey);
    });

    it('should return null if no cached results are found', () => {
      const cacheKey = 'nonExistentKey';
      (localStorage.getItem as jasmine.Spy).and.returnValue(null);

      const result = service.getFromCache(cacheKey);

      expect(result).toBeNull();
      expect(localStorage.getItem).toHaveBeenCalledWith('tc-admin-' + cacheKey);
    });
  });

  describe('removeFromCache', () => {
    it('should remove cached results with the correct key', () => {
      const cacheKey = 'someKey';
      const deleteSpy = spyOn(service['cacheKeys'], 'delete').and.callThrough();

      service.removeFromCache(cacheKey);

      expect(localStorage.removeItem).toHaveBeenCalledWith('tc-admin-' + cacheKey);
      expect(deleteSpy).toHaveBeenCalledWith(cacheKey);
    });
  });

  describe('clearAll', () => {
    it('should remove all cached keys from localStorage and clear cacheKeys set', () => {
      const cacheKeys = ['key1', 'key2', 'key3'];
      service['cacheKeys'] = new Set(cacheKeys);
      const clearSpy = spyOn(service['cacheKeys'], 'clear').and.callThrough();

      service.clearAll();

      cacheKeys.forEach(cacheKey => {
        expect(localStorage.removeItem).toHaveBeenCalledWith('tc-admin-' + cacheKey);
      });
      expect(clearSpy).toHaveBeenCalled();
      expect(service['cacheKeys'].size).toBe(0);
    });
  });

});
