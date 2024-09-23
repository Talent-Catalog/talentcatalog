import {TestBed} from '@angular/core/testing';

import {CandidateSourceCacheService} from './candidate-source-cache.service';
import {LocalStorageService} from "angular-2-local-storage";
import {CandidateSource} from "../model/base";
import {SavedSearch, SavedSearchType} from "../model/saved-search";

describe('CandidateSourceCacheService', () => {
  let service: CandidateSourceCacheService;
  let localStorageService: jasmine.SpyObj<LocalStorageService>;

  beforeEach(() => {
    const localStorageServiceSpy = jasmine.createSpyObj('LocalStorageService', ['set', 'get', 'remove']);

    TestBed.configureTestingModule({
      providers: [
        CandidateSourceCacheService,
        { provide: LocalStorageService, useValue: localStorageServiceSpy }
      ]
    });
    service = TestBed.inject(CandidateSourceCacheService);
    localStorageService = TestBed.inject(LocalStorageService) as jasmine.SpyObj<LocalStorageService>;
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

      expect(localStorageService.set).toHaveBeenCalledWith(cacheKey, source);
      expect(service['cacheKeys'].has(cacheKey)).toBeTrue();
    });
  });

  describe('getFromCache', () => {
    it('should retrieve cached source with the correct key', () => {
      const source: CandidateSource = {id: 1} as CandidateSource;
      const cacheKey = service.cacheKey(source);
      localStorageService.get.and.returnValue(source);

      const result = service.getFromCache(cacheKey);

      expect(result).toEqual(source);
      expect(localStorageService.get).toHaveBeenCalledWith(cacheKey);
    });

    it('should return null if no cached results are found', () => {
      const source: CandidateSource = { id: 1 } as CandidateSource;
      const cacheKey = service.cacheKey(source);
      localStorageService.get.and.returnValue(null);

      const result = service.getFromCache(cacheKey);

      expect(result).toBeNull();
      expect(localStorageService.get).toHaveBeenCalledWith(cacheKey);
    });
  });

  describe('removeFromCache', () => {
    it('should remove cached results with the correct key', () => {
      const source: CandidateSource = { id: 1 } as CandidateSource;
      const cacheKey = service.cacheKey(source);
      const deleteSpy = spyOn(service['cacheKeys'], 'delete').and.callThrough();
      localStorageService.remove.and.returnValue(true);

      service.removeFromCache(cacheKey);

      expect(localStorageService.remove).toHaveBeenCalledWith(cacheKey);
      expect(deleteSpy).toHaveBeenCalledWith(cacheKey);
    });
  });

  describe('clearAll', () => {
    it('should remove all cached keys from localStorage and clear cacheKeys set', () => {
      const cacheKeys = ['key1', 'key2', 'key3'];
      service['cacheKeys'] = new Set(cacheKeys);
      localStorageService.remove.and.returnValue(true);
      const clearSpy = spyOn(service['cacheKeys'], 'clear').and.callThrough();

      service.clearAll();

      cacheKeys.forEach(cacheKey => {
        expect(localStorageService.remove).toHaveBeenCalledWith(cacheKey);
      });
      expect(clearSpy).toHaveBeenCalled();
      expect(service['cacheKeys'].size).toBe(0);
    });
  });

});
