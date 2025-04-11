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
import {CandidateSourceResultsCacheService, CachedSourceResults} from './candidate-source-results-cache.service';
import {CandidateSource} from '../model/base';
import {LocalStorageService} from "./local-storage.service";

describe('CandidateSourceResultsCacheService', () => {
  let service: CandidateSourceResultsCacheService;
  let localStorageService: jasmine.SpyObj<LocalStorageService>;

  beforeEach(() => {
    const localStorageServiceSpy = jasmine.createSpyObj('LocalStorageService', ['set', 'get', 'remove']);

    TestBed.configureTestingModule({
      providers: [
        CandidateSourceResultsCacheService,
        { provide: LocalStorageService, useValue: localStorageServiceSpy }
      ]
    });

    service = TestBed.inject(CandidateSourceResultsCacheService);
    localStorageService = TestBed.inject(LocalStorageService) as jasmine.SpyObj<LocalStorageService>;
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('cache', () => {
    it('should cache results with the correct key', () => {
      const source: CandidateSource = { id: 1 } as CandidateSource;
      const cachedResults: CachedSourceResults = {
        id: 1,
        pageNumber: 1,
        pageSize: 10,
        sortFields: ['name'],
        sortDirection: 'asc',
        timestamp: Date.now()
      };

      const originalCacheKey = CandidateSourceResultsCacheService['cacheKey'];
      (CandidateSourceResultsCacheService as any)['cacheKey'] = () => 'type1';
      service.cache(source, cachedResults);

      expect(localStorageService.set).toHaveBeenCalledWith('type1', cachedResults);

      CandidateSourceResultsCacheService['cacheKey'] = originalCacheKey;
    });
  });

  describe('getFromCache', () => {
    it('should retrieve cached results with the correct key', () => {
      const source: CandidateSource = { id: 1 } as CandidateSource;
      const cachedResults: CachedSourceResults = {
        id: 1,
        pageNumber: 1,
        pageSize: 10,
        sortFields: ['name'],
        sortDirection: 'asc',
        timestamp: Date.now()
      };

      const originalCacheKey = CandidateSourceResultsCacheService['cacheKey'];
      (CandidateSourceResultsCacheService as any)['cacheKey'] = () => 'type1';
      localStorageService.get.and.returnValue(cachedResults);

      const result = service.getFromCache(source);

      expect(result).toEqual(cachedResults);
      expect(localStorageService.get).toHaveBeenCalledWith('type1');

      // Restore the original static method
      CandidateSourceResultsCacheService['cacheKey'] = originalCacheKey;
    });

    it('should return null if no cached results are found', () => {
      const source: CandidateSource = { id: 1 } as CandidateSource;

      const originalCacheKey = CandidateSourceResultsCacheService['cacheKey'];
      (CandidateSourceResultsCacheService as any)['cacheKey'] = () => 'type1';
      localStorageService.get.and.returnValue(null);

      const result = service.getFromCache(source);

      expect(result).toBeNull();
      expect(localStorageService.get).toHaveBeenCalledWith('type1');

      // Restore the original static method
      CandidateSourceResultsCacheService['cacheKey'] = originalCacheKey;
    });
  });

  describe('removeFromCache', () => {
    it('should remove cached results with the correct key', () => {
      const source: CandidateSource = { id: 1 } as CandidateSource;

      const originalCacheKey = CandidateSourceResultsCacheService['cacheKey'];
      (CandidateSourceResultsCacheService as any)['cacheKey'] = () => 'type1';
      spyOn(localStorage, 'removeItem');

      service.removeFromCache(source);

      expect(localStorageService.remove).toHaveBeenCalledWith('type1');

      // Restore the original static method
      CandidateSourceResultsCacheService['cacheKey'] = originalCacheKey;
    });
  });
});
