import {Injectable} from '@angular/core';
import {Observable} from 'rxjs/index';
import {environment} from '../../environments/environment';
import {HttpClient} from '@angular/common/http';
import {CandidateEducation} from "../model/candidate-education";

@Injectable({providedIn: 'root'})
export class CandidateEducationService {

  private apiUrl = environment.apiUrl + '/candidate-education';

  constructor(private http:HttpClient) {}

  list(id: number): Observable<CandidateEducation[]> {
    return this.http.get<CandidateEducation[]>(`${this.apiUrl}/${id}/list`);
  }

  create(id: number, details): Observable<CandidateEducation>  {
    return this.http.post<CandidateEducation>(`${this.apiUrl}/${id}`, details);
  }

  update(id: number, details): Observable<CandidateEducation>  {
    return this.http.put<CandidateEducation>(`${this.apiUrl}/${id}`, details);
  }

}
