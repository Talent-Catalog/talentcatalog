import {Injectable} from '@angular/core';
import {environment} from "../../environments/environment";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {Language} from "../model/language";

@Injectable({
  providedIn: 'root'
})
export class LanguageService {

  private apiUrl: string = environment.apiUrl + '/language';

  constructor(private http: HttpClient) { }

  createLanguage(request): Observable<Language> {
    return this.http.post<Language>(`${this.apiUrl}`, request);
  }

  deleteLanguage(id: number): Observable<any> {
    return this.http.delete<any>(`${this.apiUrl}/${id}`);
  }
}
