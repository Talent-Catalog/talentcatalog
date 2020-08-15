import {Injectable} from '@angular/core';
import {environment} from "../../environments/environment";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {CandidateCertification} from "../model/candidate-certification";
import {CandidateEducation} from '../model/candidate-education';

@Injectable({
  providedIn: 'root'
})
export class CandidateCertificationService {

  private apiUrl: string = environment.apiUrl + '/candidate-certification';

  constructor(private http: HttpClient) {
  }

  createCandidateCertification(request): Observable<CandidateCertification> {
    return this.http.post<CandidateCertification>(`${this.apiUrl}`, request);
  }

  update(id: number, details): Observable<CandidateCertification>  {
    return this.http.put<CandidateCertification>(`${this.apiUrl}/${id}`, details);
  }

  deleteCandidateCertification(id: number): Observable<any> {
    return this.http.delete<any>(`${this.apiUrl}/${id}`);
  }
}
