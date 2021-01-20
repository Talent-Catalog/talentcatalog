import {Injectable} from '@angular/core';
import {Observable, Subject} from 'rxjs/index';
import {environment} from '../../environments/environment';
import {HttpClient} from '@angular/common/http';
import {SearchResults} from '../model/search-results';
import {CandidateNote} from '../model/candidate-note';
import {tap} from 'rxjs/operators';

export interface CreateCandidateNoteRequest {
  candidateId: number;
  title: string;
  comment?: string;
}

@Injectable({providedIn: 'root'})
export class CandidateNoteService {

  private apiUrl = environment.apiUrl + '/candidate-note';

  private newNoteSource = new Subject();
  newNote$ = this.newNoteSource.asObservable();

  private updatedNoteSource = new Subject();
  updatedNote$ = this.updatedNoteSource.asObservable();

  constructor(private http: HttpClient) {}

  list(id: number): Observable<CandidateNote[]> {
    return this.http.get<CandidateNote[]>(`${this.apiUrl}/${id}/list`);
  }

  search(request): Observable<SearchResults<CandidateNote>> {
    return this.http.post<SearchResults<CandidateNote>>(`${this.apiUrl}/search`, request);
  }

  create(request: CreateCandidateNoteRequest): Observable<CandidateNote>  {
     return this.http.post<CandidateNote>(`${this.apiUrl}`, request).pipe(
       tap(() => {
         this.newNoteSource.next()
       }))
  }

  update(id: number, details): Observable<CandidateNote>  {
    return this.http.put<CandidateNote>(`${this.apiUrl}/${id}`, details).pipe(
      tap(() => {
        this.updatedNoteSource.next()
      }));
  }

}
