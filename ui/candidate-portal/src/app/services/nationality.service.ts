import {Injectable} from '@angular/core';
import {environment} from "../../environments/environment";
import {HttpClient} from "@angular/common/http";
import {Nationality} from "../model/nationality";
import {Observable, throwError} from "rxjs";
import {catchError, map} from "rxjs/operators";
import {LanguageService} from "./language.service";
import {Country} from "../model/country";

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
        if (locale == 'en') {
          let iraqi: Nationality = items.find(x=> x.name == "Iraqi");
          let jordanian: Nationality = items.find(x=> x.name == "Jordanian");
          let palestinian: Nationality = items.find(x=> x.name == "Palestinian");
          let syrian: Nationality = items.find(x=> x.name == "Syrian");
          items.splice(0,0, syrian);
          items.splice(0,0, palestinian);
          items.splice(0,0, jordanian);
          items.splice(0,0, iraqi);
        }
        return items;
      }),
      catchError(e => throwError(e))
    );
  }

}
