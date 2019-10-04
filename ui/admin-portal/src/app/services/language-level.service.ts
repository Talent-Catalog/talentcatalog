import {Injectable} from '@angular/core';
import {environment} from "../../environments/environment";
import {HttpClient} from "@angular/common/http";
import {LanguageLevel} from "../model/language-level";
import {Observable} from "rxjs";
import {SearchResults} from "../model/search-results";
import {User} from "../model/user";
import {Candidate} from "../model/candidate";

@Injectable({
  providedIn: 'root'
})
export class LanguageLevelService {

  private apiUrl: string = environment.apiUrl + '/language-level';

  constructor(private http: HttpClient) { }

  listLanguageLevels(): Observable<LanguageLevel[]> {
    return this.http.get<LanguageLevel[]>(`${this.apiUrl}`);
  }

  search(request): Observable<SearchResults<LanguageLevel>> {
    return this.http.post<SearchResults<LanguageLevel>>(`${this.apiUrl}/search`, request);
  }

  get(id: number): Observable<LanguageLevel> {
    return this.http.get<LanguageLevel>(`${this.apiUrl}/${id}`);
  }

  create(details): Observable<LanguageLevel>  {
    return this.http.post<LanguageLevel>(`${this.apiUrl}`, details);
  }

  update(id: number, details): Observable<LanguageLevel>  {
    return this.http.put<LanguageLevel>(`${this.apiUrl}/${id}`, details);
  }

  delete(id: number): Observable<boolean>  {
    return this.http.delete<boolean>(`${this.apiUrl}/${id}`);
  }

}
