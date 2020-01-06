import {Injectable} from '@angular/core';
import {Observable} from 'rxjs/index';
import {environment} from '../../environments/environment';
import {HttpClient} from '@angular/common/http';
import {Occupation} from "../model/occupation";
import {CandidateOccupation} from "../model/candidate-occupation";

@Injectable({providedIn: 'root'})
export class CandidateOccupationService {

  private apiUrl = environment.apiUrl + '/candidate-occupation';

  constructor(private http:HttpClient) {}

  /* Note: This endpoint returns a list of Occupation objects, not CandidateOccupations */
  listVerifiedOccupations(): Observable<Occupation[]> {
    return this.http.get<Occupation[]>(`${this.apiUrl}/verified`);
  }

  /* Note: This endpoint returns a list of Occupation objects, not CandidateOccupations */
  listOccupations(): Observable<Occupation[]> {
    return this.http.get<Occupation[]>(`${this.apiUrl}/occupation`);
  }

  get(id: number): Observable<CandidateOccupation[]> {
    return this.http.get<CandidateOccupation[]>(`${this.apiUrl}/${id}/list`);
  }

  update(id: number, details): Observable<CandidateOccupation>  {
    return this.http.put<CandidateOccupation>(`${this.apiUrl}/${id}`, details);
  }
}
