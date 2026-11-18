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

import {TestBed} from '@angular/core/testing';
import {expect, vi} from 'vitest';
import {CandidateSource} from '../model/base';
import {SavedSearch} from '../model/saved-search';
import {CandidateSourceCacheService} from './candidate-source-cache.service';
import {LocalStorageService} from './local-storage.service';

describe('CandidateSourceCacheService', () => {
  let service: CandidateSourceCacheService;
  let storage: {
    get: ReturnType<typeof vi.fn>;
    set: ReturnType<typeof vi.fn>;
    remove: ReturnType<typeof vi.fn>;
  };

  beforeEach(() => {
    storage = {
      get: vi.fn().mockReturnValue(null),
      set: vi.fn(),
      remove: vi.fn()
    };
    TestBed.configureTestingModule({
      providers: [
        CandidateSourceCacheService,
        {provide: LocalStorageService, useValue: storage}
      ]
    });
    service = TestBed.inject(CandidateSourceCacheService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('cacheKey', () => {
    it('should generate the correct cache key for a saved search', () => {
      const source = {id: 123, defaultSearch: false} as SavedSearch;
      expect(service.cacheKey(source)).toBe('SearchInfo123');
    });

    it('should generate the correct cache key for a list', () => {
      const source = {id: 456} as CandidateSource;
      expect(service.cacheKey(source)).toBe('ListInfo456');
    });
  });

  describe('cache', () => {
    it('should cache source with the correct key and update cacheKeys', () => {
      const source = {id: 1} as CandidateSource;
      const cacheKey = service.cacheKey(source);
      service.cache(cacheKey, source);

      expect(storage.set).toHaveBeenCalledWith(cacheKey, source);
      expect(service['cacheKeys'].has(cacheKey)).toBe(true);
    });
  });

  describe('getFromCache', () => {
    it('should retrieve cached source with the correct key', () => {
      const source = {id: 1} as CandidateSource;
      const cacheKey = service.cacheKey(source);
      storage.get.mockReturnValue(source);

      expect(service.getFromCache(cacheKey)).toEqual(source);
      expect(storage.get).toHaveBeenCalledWith(cacheKey);
    });

    it('should return null if no cached results are found', () => {
      const cacheKey = 'nonExistentKey';
      storage.get.mockReturnValue(null);

      expect(service.getFromCache(cacheKey)).toBeNull();
      expect(storage.get).toHaveBeenCalledWith(cacheKey);
    });
  });

  describe('removeFromCache', () => {
    it('should remove cached results with the correct key', () => {
      const cacheKey = 'someKey';
      const deleteSpy = vi.spyOn(service['cacheKeys'], 'delete');

      service.removeFromCache(cacheKey);
      expect(storage.remove).toHaveBeenCalledWith(cacheKey);
      expect(deleteSpy).toHaveBeenCalledWith(cacheKey);
    });
  });

  describe('clearAll', () => {
    it('should remove all cached keys and clear the cacheKeys set', () => {
      const cacheKeys = ['key1', 'key2', 'key3'];
      service['cacheKeys'] = new Set(cacheKeys);
      const clearSpy = vi.spyOn(service['cacheKeys'], 'clear');

      service.clearAll();
      for (const cacheKey of cacheKeys) {
        expect(storage.remove).toHaveBeenCalledWith(cacheKey);
      }
      expect(clearSpy).toHaveBeenCalledTimes(1);
      expect(service['cacheKeys'].size).toBe(0);
    });
  });
});
