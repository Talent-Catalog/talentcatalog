import {Injectable} from '@angular/core';
import {LocalStorageService} from "angular-2-local-storage";
import {SearchResults} from "../model/search-results";
import {Candidate} from "../model/candidate";
import {CandidateSource} from "../model/base";
import {getCandidateSourceType} from "../model/saved-search";

export interface CachedSearchResults {
  id: number;
  pageNumber: number;
  pageSize: number;
  sortFields: string[];
  sortDirection: string;
  results?: SearchResults<Candidate>;
  timestamp: number;
}

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

  cache(source: CandidateSource, cachedSearchResults: CachedSearchResults) {
    const cacheKey = CandidateSourceResultsCacheService.cacheKey(source);
    this.localStorageService.set(cacheKey, cachedSearchResults);
  }

  getFromCache(source: CandidateSource): CachedSearchResults {
    const cacheKey = CandidateSourceResultsCacheService.cacheKey(source);
    return this.localStorageService.get<CachedSearchResults>(cacheKey);
  }

  removeFromCache(source: CandidateSource): boolean {
    const cacheKey = CandidateSourceResultsCacheService.cacheKey(source);
    return this.localStorageService.remove(cacheKey);
  }
}
