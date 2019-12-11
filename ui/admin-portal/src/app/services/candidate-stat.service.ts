import {Injectable} from '@angular/core';
import {Observable} from 'rxjs/index';
import {environment} from '../../environments/environment';
import {HttpClient} from '@angular/common/http';
import {DataRow} from "../model/data-row";

@Injectable({providedIn: 'root'})
export class CandidateStatService {

  private apiUrl = environment.apiUrl + '/candidate/stat';

  constructor(private http:HttpClient) {}

  getNationalityData(): Observable<DataRow[]> {
    return this.http.get<DataRow[]>(`${this.apiUrl}/nationality`);
  }



}
