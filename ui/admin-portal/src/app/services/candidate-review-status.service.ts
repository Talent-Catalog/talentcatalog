import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {environment} from '../../environments/environment';
import {HttpClient} from '@angular/common/http';
import {CandidateReviewStatusItem} from "../model/candidate-review-status-item";

@Injectable({providedIn: 'root'})
export class CandidateReviewStatusService {

  private apiUrl = environment.apiUrl + '/candidate-reviewstatus';

  constructor(private http: HttpClient) {}

  get(id): Observable<CandidateReviewStatusItem>  {
    return this.http.get<CandidateReviewStatusItem>(`${this.apiUrl}/${id}`);
  }

  create(details): Observable<CandidateReviewStatusItem>  {
    return this.http.post<CandidateReviewStatusItem>(`${this.apiUrl}`, details);
  }

  update(id: number, details): Observable<CandidateReviewStatusItem>  {
    return this.http.put<CandidateReviewStatusItem>(`${this.apiUrl}/${id}`, details);
  }

}
