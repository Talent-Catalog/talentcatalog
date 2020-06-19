import {Injectable} from '@angular/core';
import {Observable} from 'rxjs/index';
import {environment} from '../../environments/environment';
import {HttpClient} from '@angular/common/http';
import {SearchResults} from "../model/search-results";
import {Candidate} from "../model/candidate";
import {CandidateSource, PagedSearchRequest} from "../model/base";
import {isSavedSearch} from "../model/saved-search";

@Injectable({providedIn: 'root'})
export class CandidateSourceService {

  private savedListApiUrl = environment.apiUrl + '/saved-list-candidate';
  private savedSearchApiUrl = environment.apiUrl + '/saved-search-candidate';

  constructor(private http: HttpClient) {}

  searchPaged(source: CandidateSource, request: PagedSearchRequest):
    Observable<SearchResults<Candidate>> {

    const apiUrl = isSavedSearch(source) ?
      this.savedSearchApiUrl : this.savedListApiUrl;

    return this.http.post<SearchResults<Candidate>>(
      `${apiUrl}/${source.id}/search-paged`, request);
  }

}
