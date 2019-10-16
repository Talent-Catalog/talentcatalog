import {Injectable} from '@angular/core';
import {Observable} from 'rxjs/index';
import {environment} from '../../environments/environment';
import {HttpClient} from '@angular/common/http';
import {CandidateJobExperience} from "../model/candidate-job-experience";
import { SearchResults } from '../model/search-results';

@Injectable({providedIn: 'root'})
export class CandidateJobExperienceService {

  private apiUrl = environment.apiUrl + '/candidate-job-experience';

  constructor(private http:HttpClient) {}

  list(id: number): Observable<CandidateJobExperience[]> {
    return this.http.get<CandidateJobExperience[]>(`${this.apiUrl}/${id}/list`);
  }

  create(id: number, details): Observable<CandidateJobExperience>  {
    return this.http.post<CandidateJobExperience>(`${this.apiUrl}/${id}`, details);
  }

  update(id: number, details): Observable<CandidateJobExperience>  {
    return this.http.put<CandidateJobExperience>(`${this.apiUrl}/${id}`, details);
  }

  search(request): Observable<SearchResults<CandidateJobExperience>> {
    return this.http.post<SearchResults<CandidateJobExperience>>(`${this.apiUrl}/search`, request);
  }

}
