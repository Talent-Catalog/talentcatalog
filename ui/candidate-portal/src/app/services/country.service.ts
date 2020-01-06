import {Injectable} from '@angular/core';
import {environment} from "../../environments/environment";
import {HttpClient} from "@angular/common/http";
import {Country} from "../model/country";
import {Observable, throwError} from "rxjs";
import {catchError, map} from "rxjs/operators";
import {LanguageService} from "./language.service";

@Injectable({
  providedIn: 'root'
})
export class CountryService {

  private apiUrl: string = environment.apiUrl + '/country';

  constructor(private http: HttpClient, private languageService: LanguageService) { }

  listCountries(): Observable<Country[]> {
    const locale = this.languageService.getSelectedLanguage() || 'en';
    return this.http.get<Country[]>(`${this.apiUrl}`).pipe(
      map((items: Country[], index: number) => {
        return items.sort((a, b) => a.name.localeCompare(b.name, locale));
      }),
      catchError(e => throwError(e))
    );
  }

}
