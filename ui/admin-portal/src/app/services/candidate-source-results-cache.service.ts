import {Injectable} from '@angular/core';
import {LocalStorageService} from "angular-2-local-storage";
import {SearchResults} from "../model/search-results";
import {Candidate} from "../model/candidate";

export interface CachedSearchResults {
  searchID: number;
  pageNumber: number;
  pageSize: number;
  sortFields: string[];
  sortDirection: string;
  reviewStatusFilter?: string[];
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

  private static cacheKey(sourceType: string, id: number, reviewStatusFilter: string[]): string {
    return sourceType + id + '/' + reviewStatusFilter;
  }

  cache(sourceType: string, cachedSearchResults: CachedSearchResults) {
    const cacheKey = CandidateSourceResultsCacheService.cacheKey(sourceType,
      cachedSearchResults.searchID, cachedSearchResults.reviewStatusFilter);
    this.localStorageService.set(cacheKey, cachedSearchResults);
  }

  getFromCache(sourceType: string, id: number, reviewStatusFilter: string[])
    : CachedSearchResults {
    const cacheKey = CandidateSourceResultsCacheService
      .cacheKey(sourceType, id, reviewStatusFilter);
    return this.localStorageService.get<CachedSearchResults>(cacheKey);
  }
}
