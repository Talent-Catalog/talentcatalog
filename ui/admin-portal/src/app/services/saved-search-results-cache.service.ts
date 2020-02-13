import { Injectable } from '@angular/core';
import {LocalStorageService} from "angular-2-local-storage";
import {SearchResults} from "../model/search-results";
import {Candidate} from "../model/candidate";

export interface CachedSearchResults {
  searchID: number;
  pageNumber: number;
  pageSize: number;
  sortFields: string[],
  sortDirection: string,
  results: SearchResults<Candidate>;
  timestamp: number;
}

@Injectable({
  providedIn: 'root'
})
export class SavedSearchResultsCacheService {

  constructor(
    private localStorageService: LocalStorageService,

  ) { }

  private static cacheKey(savedSearchID: number): string {
    return "Search" + savedSearchID;
  }

  cache(cachedSearchResults: CachedSearchResults) {
    const cacheKey = SavedSearchResultsCacheService.cacheKey(cachedSearchResults.searchID);
    // alternative using global localStorage localStorage.setItem(cacheKey, JSON.stringify(cachedSearchResults));
    this.localStorageService.set(cacheKey, cachedSearchResults);
  }

  getFromCache(savedSearchID: number): CachedSearchResults {
    const cacheKey = SavedSearchResultsCacheService.cacheKey(savedSearchID);
    return this.localStorageService.get<CachedSearchResults>(cacheKey);
    // alternative using global localStorage  return JSON.parse(localStorage.getItem(cacheKey))
  }
}
