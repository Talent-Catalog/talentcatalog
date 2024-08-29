import {Injectable} from '@angular/core';
import {LocalStorageService} from "angular-2-local-storage";
import {CandidateSource} from "../model/base";
import {getCandidateSourceType} from "../model/saved-search";
import {CachedSourceResults} from "./candidate-source-results-cache.service";

@Injectable({
  providedIn: 'any' // Will provide a new instance for each component
})
export class CandidateSourceCacheService {
  private cacheKeys: Set<string> = new Set();

  constructor(
    private localStorageService: LocalStorageService,
  ) { }

  /**
   * Generates a unique cache key based on the input.
   * @param source - The candidate source to generate a cache key for.
   * @returns - A unique cache key.
   */
  cacheKey(source: CandidateSource): string {
    return getCandidateSourceType(source) + 'Info' + source.id;
  }

  /**
   * Adds a candidate source to the cache with the specified cache key.
   * @param cacheKey - The key to use for the cached object.
   * @param source - The candidate source to cache.
   */
  cache(cacheKey: string, source: CandidateSource): void {
    this.localStorageService.set(cacheKey, source);
    this.cacheKeys.add(cacheKey);
  }

  /**
   * Retrieves a candidate source from the cache using the specified cache key.
   * @param cacheKey - The key of the cached object.
   * @returns - The cached source, or null if the key doesn't exist.
   */
  getFromCache(cacheKey: string): CandidateSource | null {
    return this.localStorageService.get<CandidateSource>(cacheKey);
  }

  /**
   * Removes an object from the cache using the specified cache key.
   * @param cacheKey - The key of the object to remove.
   */
  removeFromCache(cacheKey: string): void {
    this.localStorageService.remove(cacheKey);
    this.cacheKeys.delete(cacheKey);
  }

  /**
   * Clears all cached objects for the current instance of the cache service.
   */
  clearAll(): void {
    this.cacheKeys.forEach(cacheKey => {
      this.localStorageService.remove(cacheKey);
    });
    this.cacheKeys.clear();
  }

}
