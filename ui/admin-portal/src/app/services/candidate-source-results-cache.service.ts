import {Injectable} from '@angular/core';
import {LocalStorageService} from "angular-2-local-storage";
import {SearchResults} from "../model/search-results";
import {Candidate} from "../model/candidate";
import {CandidateSource} from "../model/base";
import {getCandidateSourceType} from "../model/saved-search";

export interface CachedSourceResults {
  id: number;
  pageNumber: number;
  pageSize: number;
  sortFields: string[];
  sortDirection: string;
  results?: SearchResults<Candidate>;
  timestamp: number;
}

/**
 * Service for caching the results returned by candidate sources.
 * <p/>
 * Once set of results is cached for each candidate source.
 * The cached results are associated with a standard filtering
 * (by page number/size, sorting and sort order)
 */
@Injectable({
  providedIn: 'root'
})
export class CandidateSourceResultsCacheService {

  constructor(
    private localStorageService: LocalStorageService,

  ) { }

  private static cacheKey(source: CandidateSource): string {
    return getCandidateSourceType(source) + source.id;
  }

  /**
   * Cache the given results associated with the given source.
   * @param source Candidate source associated with results.
   * @param cachedSearchResults Results to be stored in cache.
   */
  cache(source: CandidateSource, cachedSearchResults: CachedSourceResults) {
    const cacheKey = CandidateSourceResultsCacheService.cacheKey(source);
    this.localStorageService.set(cacheKey, cachedSearchResults);
  }

  /**
   * Returns the cached results associated with the given source.
   * <p/>
   * Note that it is up to the caller to ensure that the given page number,
   * sort and any other requested filtering is what is required.
   * <p/>
   * In particular, note that SavedSearch's have review filters which affect
   * the returned results (in addition to the standard paging and sorting).
   * This common caching does not take into account review filtering so
   * it is up to the caller to ensure that all SavedSearch results caching
   * is associated with a standard, unchanging, review filter.
   * @param source Candidate source whose cache is requested
   * @return Cached results or null if none found.
   */
  getFromCache(source: CandidateSource): CachedSourceResults {
    const cacheKey = CandidateSourceResultsCacheService.cacheKey(source);
    return this.localStorageService.get<CachedSourceResults>(cacheKey);
  }

  /**
   * Remove the cached info associated with the given source.
   * <p/>
   * This ensures that the next request for results from this source will
   * not come from the cache.
   * @param source Candidate source whose cache is to be cleared.
   */
  removeFromCache(source: CandidateSource): boolean {
    const cacheKey = CandidateSourceResultsCacheService.cacheKey(source);
    return this.localStorageService.remove(cacheKey);
  }
}
