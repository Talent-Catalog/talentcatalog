import {Injectable} from '@angular/core';
import {Observable} from 'rxjs/index';
import {environment} from '../../environments/environment';
import {HttpClient} from '@angular/common/http';
import {SearchResults} from '../model/search-results';
import {CandidateAttachment, CandidateAttachmentRequest} from '../model/candidate-attachment';

@Injectable({providedIn: 'root'})
export class CandidateAttachmentService {

  private apiUrl = environment.apiUrl + '/candidate-attachment';

  constructor(private http: HttpClient) {}

  search(request): Observable<CandidateAttachment[]> {
    return this.http.post<CandidateAttachment[]>(`${this.apiUrl}/search`, request);
  }

  searchPaged(request): Observable<SearchResults<CandidateAttachment>> {
    return this.http.post<SearchResults<CandidateAttachment>>(`${this.apiUrl}/search-paged`, request);
  }

  createAttachment(details: CandidateAttachmentRequest): Observable<CandidateAttachment>  {
    return this.http.post<CandidateAttachment>(`${this.apiUrl}`, details);
  }

  deleteAttachment(id: number) {
    return this.http.delete<CandidateAttachment>(`${this.apiUrl}/${id}`);
  }

  uploadAttachment(id: number, cv: boolean, formData: FormData): Observable<CandidateAttachment> {
    return this.http.post<CandidateAttachment>(
      `${this.apiUrl}/${id}/upload?cv=${cv}`, formData);
  }

  updateAttachment(value: any): Observable<CandidateAttachment> {
    return this.http.put<CandidateAttachment>(`${this.apiUrl}`, value);
  }
}
