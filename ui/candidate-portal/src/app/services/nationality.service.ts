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
        items.sort((a, b) => a.name.localeCompare(b.name, locale));
        //Bit of a hack, which only works in English, for putting some names
        //at top.
        if (locale === 'en') {
          const iraqi: Nationality = items.find(x => x.name === "Iraqi");
          if (iraqi) {
            items.splice(0, 0, iraqi);
          }
          const jordanian: Nationality = items.find(x => x.name === "Jordanian");
          if (jordanian) {
            items.splice(0, 0, jordanian);
          }
          const palestinian: Nationality = items.find(x => x.name === "Palestinian");
          if (palestinian) {
            items.splice(0, 0, palestinian);
          }
          const syrian: Nationality = items.find(x => x.name === "Syrian");
          if (syrian) {
            items.splice(0, 0, syrian);
          }
        }
        return items;
      }),
      catchError(e => throwError(e))
    );
  }

}
