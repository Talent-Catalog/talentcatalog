import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/index';
import { environment } from '../../environments/environment';
import { HttpClient } from '@angular/common/http';
import { SearchResults } from '../model/search-results';
import {CandidateNote} from "../model/candidate-note";

@Injectable({providedIn: 'root'})
export class CandidateNoteService {

  private apiUrl = environment.apiUrl + '/candidate-note';

  constructor(private http:HttpClient) {}

  list(id: number): Observable<CandidateNote[]> {
    return this.http.get<CandidateNote[]>(`${this.apiUrl}/${id}/list`);
  }

  search(request): Observable<SearchResults<CandidateNote>> {
    return this.http.post<SearchResults<CandidateNote>>(`${this.apiUrl}/search`, request);
  }

  create(details): Observable<CandidateNote>  {
    return this.http.post<CandidateNote>(`${this.apiUrl}`, details);
  }

  update(id: number, details): Observable<CandidateNote>  {
    return this.http.put<CandidateNote>(`${this.apiUrl}/${id}`, details);
  }

}
