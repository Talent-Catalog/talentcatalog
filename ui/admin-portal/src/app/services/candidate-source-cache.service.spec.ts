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

import type {Mock} from "vitest";
import {TestBed} from '@angular/core/testing';
import {CandidateSourceCacheService} from './candidate-source-cache.service';
import {CandidateSource} from "../model/base";
import {SavedSearch} from "../model/saved-search";
import {expect, vi} from 'vitest';

describe('CandidateSourceCacheService', () => {
    let service: CandidateSourceCacheService;

    beforeEach(() => {
        TestBed.configureTestingModule({
            providers: [CandidateSourceCacheService]
        });
        service = TestBed.inject(CandidateSourceCacheService);

        vi.spyOn(localStorage, 'setItem').mockReturnValue(undefined);
        vi.spyOn(localStorage, 'getItem').mockReturnValue(null);
        vi.spyOn(localStorage, 'removeItem').mockReturnValue(undefined);
    });

    it('should be created', () => {
        expect(service).toBeTruthy();
    });

    describe('cacheKey', () => {
        it('should generate the correct cache key for a saved search', () => {
            //Having defaultSearch with any value makes it look like a saved search
            const source: SavedSearch = { id: 123, defaultSearch: false } as SavedSearch;

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
            expect(service['cacheKeys'].has(cacheKey)).toBe(true);
        });
    });

    describe('getFromCache', () => {
        it('should retrieve cached source with the correct key', () => {
            const source: CandidateSource = { id: 1 } as CandidateSource;
            const cacheKey = service.cacheKey(source);
            (localStorage.getItem as Mock).mockReturnValue(JSON.stringify(source));

            const result = service.getFromCache(cacheKey);

            expect(result).toEqual(source);
            expect(localStorage.getItem).toHaveBeenCalledWith('tc-admin-' + cacheKey);
        });

        it('should return null if no cached results are found', () => {
            const cacheKey = 'nonExistentKey';
            (localStorage.getItem as Mock).mockReturnValue(null);

            const result = service.getFromCache(cacheKey);

            expect(result).toBeNull();
            expect(localStorage.getItem).toHaveBeenCalledWith('tc-admin-' + cacheKey);
        });
    });

    describe('removeFromCache', () => {
        it('should remove cached results with the correct key', () => {
            const cacheKey = 'someKey';
            const deleteSpy = vi.spyOn(service['cacheKeys'], 'delete');

            service.removeFromCache(cacheKey);

            expect(localStorage.removeItem).toHaveBeenCalledWith('tc-admin-' + cacheKey);
            expect(deleteSpy).toHaveBeenCalledWith(cacheKey);
        });
    });

    describe('clearAll', () => {
        it('should remove all cached keys from localStorage and clear cacheKeys set', () => {
            const cacheKeys = ['key1', 'key2', 'key3'];
            service['cacheKeys'] = new Set(cacheKeys);
            const clearSpy = vi.spyOn(service['cacheKeys'], 'clear');

            service.clearAll();

            cacheKeys.forEach(cacheKey => {
                expect(localStorage.removeItem).toHaveBeenCalledWith('tc-admin-' + cacheKey);
            });
            expect(clearSpy).toHaveBeenCalled();
            expect(service['cacheKeys'].size).toBe(0);
        });
    });

});
