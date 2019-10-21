import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/index';
import { environment } from '../../environments/environment';
import { HttpClient } from '@angular/common/http';
import { SearchResults } from '../model/search-results';
import {CandidateNote} from "../model/candidate-note";
import {CandidateAttachment} from "../model/candidate-attachment";

@Injectable({providedIn: 'root'})
export class CandidateAttachmentService {

  private apiUrl = environment.apiUrl + '/candidate-attachment';

  constructor(private http:HttpClient) {}

  search(request): Observable<SearchResults<CandidateAttachment>> {
    return this.http.post<SearchResults<CandidateAttachment>>(`${this.apiUrl}/search`, request);
  }

  // create(details): Observable<CandidateAttachment>  {
  //   return this.http.post<CandidateAttachment>(`${this.apiUrl}`, details);
  // }
  //
  // update(id: number, details): Observable<CandidateAttachment>  {
  //   return this.http.put<CandidateAttachment>(`${this.apiUrl}/${id}`, details);
  // }

}
