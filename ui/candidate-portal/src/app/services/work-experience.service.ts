import {Injectable} from '@angular/core';
import {environment} from "../../environments/environment";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {WorkExperience} from "../model/work-experience";

@Injectable({
  providedIn: 'root'
})
export class WorkExperienceService {

  private apiUrl: string = environment.apiUrl + '/work-experience';

  constructor(private http: HttpClient) { }

  createWorkExperience(request): Observable<WorkExperience> {
    return this.http.post<WorkExperience>(`${this.apiUrl}`, request);
  }

  deleteWorkExperience(id: number): Observable<any> {
    return this.http.delete<any>(`${this.apiUrl}/${id}`);
  }
}
