import {Injectable} from '@angular/core';
import {environment} from "../../environments/environment";
import {HttpClient} from "@angular/common/http";
import {Country} from "../model/country";
import {Observable} from "rxjs";
import {SearchResults} from "../model/search-results";

@Injectable({
  providedIn: 'root'
})
export class CountryService {

  private apiUrl: string = environment.apiUrl + '/country';

  constructor(private http: HttpClient) { }

  listCountries(): Observable<Country[]> {
    return this.http.get<Country[]>(`${this.apiUrl}`);
  }

  searchPaged(request): Observable<SearchResults<Country>> {
    return this.http.post<SearchResults<Country>>(`${this.apiUrl}/search-paged`, request);
  }

  get(id: number): Observable<Country> {
    return this.http.get<Country>(`${this.apiUrl}/${id}`);
  }

  create(details): Observable<Country>  {
    return this.http.post<Country>(`${this.apiUrl}`, details);
  }

  update(id: number, details): Observable<Country>  {
    return this.http.put<Country>(`${this.apiUrl}/${id}`, details);
  }

  delete(id: number): Observable<boolean>  {
    return this.http.delete<boolean>(`${this.apiUrl}/${id}`);
  }

}
