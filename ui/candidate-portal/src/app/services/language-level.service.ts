import {Injectable} from '@angular/core';
import {environment} from "../../environments/environment";
import {HttpClient} from "@angular/common/http";
import {LanguageLevel} from "../model/language-level";
import {Observable} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class LanguageLevelService {

  private apiUrl: string = environment.apiUrl + '/language-level';

  constructor(private http: HttpClient) { }

  listLanguageLevels(): Observable<LanguageLevel[]> {
    return this.http.get<LanguageLevel[]>(`${this.apiUrl}`);
  }

}
