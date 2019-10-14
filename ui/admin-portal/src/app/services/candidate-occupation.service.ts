import {Injectable} from '@angular/core';
import {Observable} from 'rxjs/index';
import {environment} from '../../environments/environment';
import {HttpClient} from '@angular/common/http';
import {Occupation} from "../model/occupation";

@Injectable({providedIn: 'root'})
export class CandidateOccupationService {

  private apiUrl = environment.apiUrl + '/candidate-occupation';

  constructor(private http:HttpClient) {}

  /* Note: This endpoint returns a list of Occupation objects, not CandidateOccupations */
  listVerifiedOccupations(): Observable<Occupation[]> {
    return this.http.get<Occupation[]>(`${this.apiUrl}/verified`);
  }

}
