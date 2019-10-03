import {Injectable} from '@angular/core';
import {environment} from "../../environments/environment";
import {HttpClient} from "@angular/common/http";
import {Occupation} from "../model/occupation";
import {Observable} from "rxjs";
import {SearchResults} from "../model/search-results";

@Injectable({
  providedIn: 'root'
})
export class OccupationService {

  private apiUrl: string = environment.apiUrl + '/occupation';

  constructor(private http: HttpClient) { }

  listOccupations(): Observable<Occupation[]> {
    return this.http.get<Occupation[]>(`${this.apiUrl}`);
  }

  search(request): Observable<SearchResults<Occupation>> {
    return this.http.post<SearchResults<Occupation>>(`${this.apiUrl}/search`, request);
  }

  get(id: number): Observable<Occupation> {
    return this.http.get<Occupation>(`${this.apiUrl}/${id}`);
  }

  create(details): Observable<Occupation>  {
    return this.http.post<Occupation>(`${this.apiUrl}`, details);
  }

  update(id: number, details): Observable<Occupation>  {
    return this.http.put<Occupation>(`${this.apiUrl}/${id}`, details);
  }

  delete(id: number): Observable<boolean>  {
    return this.http.delete<boolean>(`${this.apiUrl}/${id}`);
  }

}
