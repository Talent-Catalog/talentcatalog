import { Injectable } from '@angular/core';
import {environment} from "../../environments/environment";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {Education} from "../model/education";

@Injectable({
  providedIn: 'root'
})
export class EducationService {

  private apiUrl: string = environment.apiUrl + '/education';

  constructor(private http: HttpClient) { }

  createEducation(request): Observable<Education> {
    return this.http.post<Education>(`${this.apiUrl}`, request);
  }

}
