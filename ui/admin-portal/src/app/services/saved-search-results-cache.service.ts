import { Injectable } from '@angular/core';
import {LocalStorageService} from "angular-2-local-storage";
import {SearchResults} from "../model/search-results";
import {Candidate} from "../model/candidate";

export interface CachedSearchResults {
  searchID: number;
  pageNumber: number;
  pageSize: number;
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

  //todo search for and get rid of use of localStorage global.

  cache(cachedSearchResults: CachedSearchResults) {
    const cacheKey = SavedSearchResultsCacheService.cacheKey(cachedSearchResults.searchID);
    // localStorage.setItem(cacheKey, JSON.stringify(cachedSearchResults));
    this.localStorageService.set(cacheKey, cachedSearchResults);
  }

  getFromCache(savedSearchID: number): CachedSearchResults {
    const cacheKey = SavedSearchResultsCacheService.cacheKey(savedSearchID);
    return this.localStorageService.get<CachedSearchResults>(cacheKey);
    // return JSON.parse(localStorage.getItem(cacheKey))
  }
}
