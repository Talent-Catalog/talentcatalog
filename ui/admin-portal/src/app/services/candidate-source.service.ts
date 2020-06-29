import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {environment} from '../../environments/environment';
import {HttpClient} from '@angular/common/http';
import {SearchResults} from "../model/search-results";
import {CandidateSource, SearchCandidateSourcesRequest} from "../model/base";
import {isSavedSearch, SearchSavedSearchRequest} from "../model/saved-search";
import {map} from "rxjs/operators";
import {SavedSearchService} from "./saved-search.service";
import {TargetListSelection} from "../components/list/select/select-list.component";

@Injectable({providedIn: 'root'})
export class CandidateSourceService {

  private savedListApiUrl = environment.apiUrl + '/saved-list';
  private savedSearchApiUrl = environment.apiUrl + '/saved-search';

  constructor(private http: HttpClient) {}

  addSharedUser(source: CandidateSource, request: { userId: number }):
    Observable<CandidateSource> {

    const apiUrl = isSavedSearch(source) ?
      this.savedSearchApiUrl : this.savedListApiUrl;

    return this.http.put<CandidateSource>(`${apiUrl}/shared-add/${source.id}`, request)
      .pipe(
        map(result => this.processPostResult(result))
      );
  }

  copy(source: CandidateSource, selection: TargetListSelection): Observable<CandidateSource> {
    const apiUrl = isSavedSearch(source) ?
      this.savedSearchApiUrl : this.savedListApiUrl;

    return this.http.put<CandidateSource>(`${apiUrl}/copy/${source.id}`, selection);
  }

  delete(source: CandidateSource): Observable<boolean>  {
    const apiUrl = isSavedSearch(source) ?
      this.savedSearchApiUrl : this.savedListApiUrl;

    return this.http.delete<boolean>(`${apiUrl}/${source.id}`);
  }

  removeSharedUser(source: CandidateSource, request: { userId: number }):
    Observable<CandidateSource> {

    const apiUrl = isSavedSearch(source) ?
      this.savedSearchApiUrl : this.savedListApiUrl;

    return this.http.put<CandidateSource>(`${apiUrl}/shared-remove/${source.id}`, request)
      .pipe(
        map(result => this.processPostResult(result))
      );
  }

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

  processPostResult(result: CandidateSource): CandidateSource {
    if (isSavedSearch(result)) {
      result = SavedSearchService.convertSavedSearchEnums(result);
    }
    return result;
  };

  processPostResults(results: SearchResults<CandidateSource>):
    SearchResults<CandidateSource> {
    for (const result of results.content) {
      this.processPostResult(result);
    }
    return results;
  };
}
