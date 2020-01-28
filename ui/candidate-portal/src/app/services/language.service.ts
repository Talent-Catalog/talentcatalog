import {Injectable} from '@angular/core';
import {environment} from "../../environments/environment";
import {HttpClient} from "@angular/common/http";
import {Language, SystemLanguage} from "../model/language";
import {Observable, throwError} from "rxjs";
import {catchError, map} from "rxjs/operators";

@Injectable({
  providedIn: 'root'
})
export class LanguageService {

  private apiUrl: string = environment.apiUrl + '/language';

  selectedLanguage: string = 'en';

  constructor(private http: HttpClient) { }

  listLanguages(): Observable<Language[]> {
    return this.http.get<Language[]>(`${this.apiUrl}`).pipe(
      map((items: Language[], index: number) => {
        return items.sort((a, b) => a.name.localeCompare(b.name, this.selectedLanguage));
      }),
      catchError(e => throwError(e))
    );
  }


  getLanguage(language){
    return this.http.get<Language>(`${this.apiUrl}/${language}`)
  }

  listSystemLanguages(): Observable<SystemLanguage[]> {
    return this.http.get<SystemLanguage[]>(`${this.apiUrl}/system`);
  }

  getSelectedLanguage(): string {
    return this.selectedLanguage;
  }

  setSelectedLanguage(selectedLanguage: string) {
    this.selectedLanguage = selectedLanguage;
  }
}
