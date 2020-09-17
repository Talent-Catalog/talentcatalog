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

  listCandidateAttachments(): Observable<CandidateAttachment[]> {
    return this.http.get<CandidateAttachment[]>(`${this.apiUrl}`);
  }

  searchCandidateAttachments(request): Observable<SearchResults<CandidateAttachment>> {
    return this.http.post<SearchResults<CandidateAttachment>>(`${this.apiUrl}/search`, request);
  }

  createAttachment(request): Observable<CandidateAttachment> {
    return this.http.post<CandidateAttachment>(`${this.apiUrl}`, request);
  }

  deleteAttachment(id: number) {
    return this.http.delete<CandidateAttachment>(`${this.apiUrl}/${id}`);
  }

  downloadAttachment(id: number) {
    return this.http.get(`${this.apiUrl}/${id}/download`,
      { responseType: 'blob' });
  }

  uploadAttachment(cv: boolean, formData: FormData): Observable<CandidateAttachment> {
    return this.http.post<CandidateAttachment>(
      `${this.apiUrl}/upload?cv=${cv}`, formData);
  }

}
