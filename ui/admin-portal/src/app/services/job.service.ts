import {Injectable} from '@angular/core';
import {environment} from "../../environments/environment";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {Job, SearchJobRequest, UpdateJobRequest} from "../model/job";
import {SearchResults} from "../model/search-results";

@Injectable({
  providedIn: 'root'
})
export class JobService {

  private apiUrl: string = environment.apiUrl + '/job';

  constructor(private http: HttpClient) { }


  create(request: UpdateJobRequest): Observable<Job> {
    return this.http.post<Job>(`${this.apiUrl}`, request);
  }

  createSuggestedSearch(id: number): Observable<Job> {
    return this.http.post<Job>(`${this.apiUrl}/${id}/create-search`, null);
  }

  get(id: number): Observable<Job> {
    return this.http.get<Job>(`${this.apiUrl}/${id}`);
  }

  removeSuggestedSearch(id: number, savedSearchId: number): Observable<Job> {
    return this.http.put<Job>(`${this.apiUrl}/${id}/remove-search`, savedSearchId);
  }

  searchPaged(request: SearchJobRequest): Observable<SearchResults<Job>> {
    return this.http.post<SearchResults<Job>>(`${this.apiUrl}/search-paged`, request);
  }

  update(id: number, request: UpdateJobRequest): Observable<Job> {
    return this.http.put<Job>(`${this.apiUrl}/${id}`, request);
  }
}
