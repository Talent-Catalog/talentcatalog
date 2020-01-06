import {Injectable} from '@angular/core';
import {environment} from "../../environments/environment";
import {HttpClient} from "@angular/common/http";
import {EducationMajor} from "../model/education-major";
import {Observable, throwError} from "rxjs";
import {catchError, map} from "rxjs/operators";
import {LanguageService} from "./language.service";

@Injectable({
  providedIn: 'root'
})
export class EducationMajorService {

  private apiUrl: string = environment.apiUrl + '/education-major';

  constructor(private http: HttpClient,
              private languageService: LanguageService) { }

  listMajors(): Observable<EducationMajor[]> {
    const locale = this.languageService.getSelectedLanguage() || 'en';
    return this.http.get<EducationMajor[]>(`${this.apiUrl}`).pipe(
      map((items: EducationMajor[], index: number) => {
        return items.sort((a, b) => a.name.localeCompare(b.name, locale));
      }),
      catchError(e => throwError(e))
    );
  }

}
