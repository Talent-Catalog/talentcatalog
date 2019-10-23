import {Injectable} from '@angular/core';
import {environment} from "../../environments/environment";
import {HttpClient} from "@angular/common/http";
import {TranslationItem} from "../model/translation-item";
import {Observable} from "rxjs";
import {SearchResults} from "../model/search-results";
import {Translation} from "../model/translation";

@Injectable({
  providedIn: 'root'
})
export class TranslationService {

  private apiUrl: string = environment.apiUrl + '/translation';

  constructor(private http: HttpClient) { }

  listTranslations(): Observable<Translation[]> {
    return this.http.get<Translation[]>(`${this.apiUrl}`);
  }

  search(type: string, request): Observable<SearchResults<TranslationItem>> {
    return this.http.post<SearchResults<TranslationItem>>(`${this.apiUrl}/${type}`, request);
  }

  create(details): Observable<Translation>  {
    return this.http.post<Translation>(`${this.apiUrl}`, details);
  }

  update(id: number, details): Observable<Translation>  {
    return this.http.put<Translation>(`${this.apiUrl}/${id}`, details);
  }

}
