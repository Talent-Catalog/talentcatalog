import {Injectable} from '@angular/core';
import {environment} from "../../environments/environment";
import {HttpClient} from "@angular/common/http";
import {Nationality} from "../model/nationality";
import {Observable} from "rxjs";
import {SearchResults} from "../model/search-results";
import {User} from "../model/user";
import {Candidate} from "../model/candidate";

@Injectable({
  providedIn: 'root'
})
export class NationalityService {

  private apiUrl: string = environment.apiUrl + '/nationality';

  constructor(private http: HttpClient) { }

  listNationalities(): Observable<Nationality[]> {
    return this.http.get<Nationality[]>(`${this.apiUrl}`);
  }

  search(request): Observable<SearchResults<Nationality>> {
    return this.http.post<SearchResults<Nationality>>(`${this.apiUrl}/search`, request);
  }

  get(id: number): Observable<Nationality> {
    return this.http.get<Nationality>(`${this.apiUrl}/${id}`);
  }

  create(details): Observable<Nationality>  {
    return this.http.post<Nationality>(`${this.apiUrl}`, details);
  }

  update(id: number, details): Observable<Nationality>  {
    return this.http.put<Nationality>(`${this.apiUrl}/${id}`, details);
  }

  delete(id: number): Observable<boolean>  {
    return this.http.delete<boolean>(`${this.apiUrl}/${id}`);
  }

}
