import {Injectable} from '@angular/core';
import {environment} from "../../environments/environment";
import {HttpClient} from "@angular/common/http";
import {Translation} from "../model/translation";
import {Observable} from "rxjs";
import {SearchResults} from "../model/search-results";

@Injectable({
  providedIn: 'root'
})
export class TranslationService {

  private apiUrl: string = environment.apiUrl + '/translation';

  constructor(private http: HttpClient) { }

  listTranslations(): Observable<Translation[]> {
    return this.http.get<Translation[]>(`${this.apiUrl}`);
  }

  search(type: string, request): Observable<SearchResults<Translation>> {
    return this.http.post<SearchResults<Translation>>(`${this.apiUrl}/${type}`, request);
  }

  get(id: number): Observable<Translation> {
    return this.http.get<Translation>(`${this.apiUrl}/${id}`);
  }

  create(details): Observable<Translation>  {
    return this.http.post<Translation>(`${this.apiUrl}`, details);
  }

  update(id: number, details): Observable<Translation>  {
    return this.http.put<Translation>(`${this.apiUrl}/${id}`, details);
  }

  delete(id: number): Observable<boolean>  {
    return this.http.delete<boolean>(`${this.apiUrl}/${id}`);
  }

}
