import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {environment} from "../../environments/environment";
import {Observable} from "rxjs";
import {CandidateAttachment} from "../model/candidate-attachment";
import {SearchResults} from "../model/search-results";

@Injectable({
  providedIn: 'root'
})
export class CandidateAttachmentService {

  private apiUrl: string = environment.apiUrl + '/candidate-attachment';

  constructor(private http: HttpClient) { }

  searchCandidateAttachments(request): Observable<SearchResults<CandidateAttachment>> {
    return this.http.post<SearchResults<CandidateAttachment>>(`${this.apiUrl}/search`, request);
  }
}
