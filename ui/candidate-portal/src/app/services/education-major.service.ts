import {Injectable} from '@angular/core';
import {environment} from "../../environments/environment";
import {HttpClient} from "@angular/common/http";
import {LanguageLevel} from "../model/language-level";
import {Observable} from "rxjs";
import {EducationLevel} from "../model/education-level";
import {EducationMajor} from "../model/education-major";

@Injectable({
  providedIn: 'root'
})
export class EducationLevelService {

  private apiUrl: string = environment.apiUrl + '/education-major';

  constructor(private http: HttpClient) { }

  listEducationLevels(): Observable<EducationMajor[]> {
    return this.http.get<EducationMajor[]>(`${this.apiUrl}`);
  }

}
