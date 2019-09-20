import {Injectable} from '@angular/core';
import {environment} from "../../environments/environment";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {Certification} from "../model/certification";

@Injectable({
  providedIn: 'root'
})
export class CertificationService {

  private apiUrl: string = environment.apiUrl + '/certification';

  constructor(private http: HttpClient) { }

  createCertification(request): Observable<Certification> {
    return this.http.post<Certification>(`${this.apiUrl}`, request);
  }

  deleteCertification(id: number): Observable<any> {
    return this.http.delete<any>(`${this.apiUrl}/${id}`);
  }
}
