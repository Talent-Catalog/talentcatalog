import {Injectable} from '@angular/core';
import {environment} from "../../environments/environment";
import {HttpClient} from "@angular/common/http";
import {Language} from "../model/language";
import {Observable} from "rxjs";
import {SearchResults} from "../model/search-results";

@Injectable({
  providedIn: 'root'
})
export class LanguageService {

  private apiUrl: string = environment.apiUrl + '/language';

  constructor(private http: HttpClient) { }

  listLanguages(): Observable<Language[]> {
    return this.http.get<Language[]>(`${this.apiUrl}`);
  }

  search(request): Observable<SearchResults<Language>> {
    return this.http.post<SearchResults<Language>>(`${this.apiUrl}/search`, request);
  }

  get(id: number): Observable<Language> {
    return this.http.get<Language>(`${this.apiUrl}/${id}`);
  }

  create(details): Observable<Language>  {
    return this.http.post<Language>(`${this.apiUrl}`, details);
  }

  update(id: number, details): Observable<Language>  {
    return this.http.put<Language>(`${this.apiUrl}/${id}`, details);
  }

  delete(id: number): Observable<boolean>  {
    return this.http.delete<boolean>(`${this.apiUrl}/${id}`);
  }

}
