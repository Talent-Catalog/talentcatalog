import {Injectable} from '@angular/core';
import {environment} from "../../environments/environment";
import {HttpClient} from "@angular/common/http";
import {Nationality} from "../model/nationality";
import {Observable, throwError} from "rxjs";
import {catchError, map} from "rxjs/operators";
import {LanguageService} from "./language.service";

@Injectable({
  providedIn: 'root'
})
export class NationalityService {

  private apiUrl: string = environment.apiUrl + '/nationality';

  constructor(private http: HttpClient,
              private languageService: LanguageService) { }

  listNationalities(): Observable<Nationality[]> {
    const locale = this.languageService.getSelectedLanguage() || 'en';
    return this.http.get<Nationality[]>(`${this.apiUrl}`).pipe(
      map((items: Nationality[], index: number) => {
        return items.sort((a, b) => a.name.localeCompare(b.name, locale));
      }),
      catchError(e => throwError(e))
    );
  }

}
