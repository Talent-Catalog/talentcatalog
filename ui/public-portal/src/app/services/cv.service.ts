import { Injectable } from '@angular/core';
import {environment} from '../../environments/environment';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Candidate} from '../model/candidate';

@Injectable({
  providedIn: 'root'
})
export class CvService {
  apiUrl: string = environment.apiUrl + '/cv';

  constructor(private http: HttpClient) { }

  decodeCvRequest(token: string): Observable<Candidate> {
    return this.http.get<Candidate>(`${this.apiUrl}/` + token);
  }

}
