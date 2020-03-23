import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {environment} from '../../environments/environment';
import {HttpClient} from '@angular/common/http';
import {StatReport} from "../model/stat-report";

@Injectable({providedIn: 'root'})
export class CandidateStatService {

  private apiUrl = environment.apiUrl + '/candidate/stat';

  constructor(private http:HttpClient) {}

  getAllStats(): Observable<StatReport[]> {
    return this.http.get<StatReport[]>(`${this.apiUrl}/all`);
  }

}
