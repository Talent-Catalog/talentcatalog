import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {environment} from '../../environments/environment';
import {HttpClient} from '@angular/common/http';
import {SearchResults} from "../model/search-results";
import {CandidateSource, SearchCandidateSourcesRequest} from "../model/base";
import {isSavedSearch, SearchSavedSearchRequest} from "../model/saved-search";
import {map} from "rxjs/operators";
import {SavedSearchService} from "./saved-search.service";

@Injectable({providedIn: 'root'})
export class CandidateSourceService {

  private savedListApiUrl = environment.apiUrl + '/saved-list';
  private savedSearchApiUrl = environment.apiUrl + '/saved-search';

  constructor(private http: HttpClient) {}

  searchPaged(request: SearchCandidateSourcesRequest):
    Observable<SearchResults<CandidateSource>> {

    const apiUrl = request instanceof SearchSavedSearchRequest ?
      this.savedSearchApiUrl : this.savedListApiUrl;

    return this.http.post<SearchResults<CandidateSource>>(
      `${apiUrl}/search-paged`, request)
      .pipe(
        map(results => this.processPostResults(results))
      );
  }

  processPostResults(results: SearchResults<CandidateSource>):
    SearchResults<CandidateSource> {
    for (let savedSearch of results.content) {
      if (isSavedSearch(savedSearch)) {
        savedSearch = SavedSearchService.convertSavedSearchEnums(savedSearch);
      }
    }
    return results;
  };

}
