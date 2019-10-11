import {Injectable} from '@angular/core';
import {environment} from "../../environments/environment";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {CandidateCertification} from "../model/candidate-certification";

@Injectable({
  providedIn: 'root'
})
export class CandidateCertificationService {

  private apiUrl: string = environment.apiUrl + '/candidate-certification';

  constructor(private http: HttpClient) { }

  createCandidateCertification(request): Observable<CandidateCertification> {
    return this.http.post<CandidateCertification>(`${this.apiUrl}`, request);
  }

  deleteCandidateCertification(id: number): Observable<any> {
    return this.http.delete<any>(`${this.apiUrl}/${id}`);
  }
}
