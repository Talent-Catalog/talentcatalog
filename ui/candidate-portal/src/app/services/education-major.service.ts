import {Injectable} from '@angular/core';
import {environment} from "../../environments/environment";
import {HttpClient} from "@angular/common/http";
import {EducationMajor} from "../model/education-major";
import {Observable} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class EducationMajorService {

  private apiUrl: string = environment.apiUrl + '/education-major';

  constructor(private http: HttpClient) { }

  listMajors(): Observable<EducationMajor[]> {
    return this.http.get<EducationMajor[]>(`${this.apiUrl}`);
  }

}
