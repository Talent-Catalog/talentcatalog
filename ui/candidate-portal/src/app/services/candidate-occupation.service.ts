import {Injectable} from '@angular/core';
import {environment} from "../../environments/environment";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {CandidateOccupation} from "../model/candidate-occupation";

@Injectable({
  providedIn: 'root'
})
export class CandidateOccupationService {

  private apiUrl: string = environment.apiUrl + '/candidate-occupation';

  constructor(private http: HttpClient) { }

  createCandidateOccupation(request): Observable<CandidateOccupation> {
    return this.http.post<CandidateOccupation>(`${this.apiUrl}`, request);
  }

  deleteCandidateOccupation(id: number): Observable<any> {
    return this.http.delete<any>(`${this.apiUrl}/${id}`);
  }

  listMyOccupations(): Observable<CandidateOccupation[]> {
    return this.http.get<CandidateOccupation[]>(`${this.apiUrl}/list`);
  }

  // TODO
  updateCandidateOccupations(request): Observable<CandidateOccupation[]> {
    alert('TODO');
    return Observable.create(null);
  }
}
