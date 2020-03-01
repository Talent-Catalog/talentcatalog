import { Injectable } from '@angular/core';
import {LocalStorageService} from "angular-2-local-storage";
import {SearchResults} from "../model/search-results";
import {Candidate} from "../model/candidate";

export interface CachedSearchResults {
  searchID: number;
  pageNumber: number;
  pageSize: number;
  sortFields: string[];
  sortDirection: string;
  shortlistStatus?: string[];
  results?: SearchResults<Candidate>;
  timestamp: number;
}

@Injectable({
  providedIn: 'root'
})
export class SavedSearchResultsCacheService {

  constructor(
    private localStorageService: LocalStorageService,

  ) { }

  private static cacheKey(savedSearchID: number, shortlistStatus: string[]): string {
    return "Search" + savedSearchID + '/' + shortlistStatus;
  }

  cache(cachedSearchResults: CachedSearchResults) {
    const cacheKey = SavedSearchResultsCacheService.cacheKey(
      cachedSearchResults.searchID, cachedSearchResults.shortlistStatus);
    this.localStorageService.set(cacheKey, cachedSearchResults);
  }

  getFromCache(savedSearchID: number, shortlistStatus: string[]): CachedSearchResults {
    const cacheKey = SavedSearchResultsCacheService.cacheKey(savedSearchID, shortlistStatus);
    return this.localStorageService.get<CachedSearchResults>(cacheKey);
  }
}
