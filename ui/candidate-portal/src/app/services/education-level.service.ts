import {Injectable} from '@angular/core';
import {environment} from "../../environments/environment";
import {HttpClient} from "@angular/common/http";
import {LanguageLevel} from "../model/language-level";
import {Observable} from "rxjs";
import {EducationLevel} from "../model/education-level";

@Injectable({
  providedIn: 'root'
})
export class EducationLevelService {

  private apiUrl: string = environment.apiUrl + '/education-level';

  constructor(private http: HttpClient) { }

  listEducationLevels(): Observable<EducationLevel[]> {
    return this.http.get<EducationLevel[]>(`${this.apiUrl}`);
  }

}
