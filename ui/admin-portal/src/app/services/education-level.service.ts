import {Injectable} from '@angular/core';
import {environment} from "../../environments/environment";
import {HttpClient} from "@angular/common/http";
import {EducationLevel} from "../model/education-level";
import {Observable} from "rxjs";
import {SearchResults} from "../model/search-results";
import {User} from "../model/user";
import {Candidate} from "../model/candidate";

@Injectable({
  providedIn: 'root'
})
export class EducationLevelService {

  private apiUrl: string = environment.apiUrl + '/education-level';

  constructor(private http: HttpClient) { }

  listEducationLevels(): Observable<EducationLevel[]> {
    return this.http.get<EducationLevel[]>(`${this.apiUrl}`);
  }

  search(request): Observable<SearchResults<EducationLevel>> {
    return this.http.post<SearchResults<EducationLevel>>(`${this.apiUrl}/search`, request);
  }

  get(id: number): Observable<EducationLevel> {
    return this.http.get<EducationLevel>(`${this.apiUrl}/${id}`);
  }

  create(details): Observable<EducationLevel>  {
    return this.http.post<EducationLevel>(`${this.apiUrl}`, details);
  }

  update(id: number, details): Observable<EducationLevel>  {
    return this.http.put<EducationLevel>(`${this.apiUrl}/${id}`, details);
  }

  delete(id: number): Observable<boolean>  {
    return this.http.delete<boolean>(`${this.apiUrl}/${id}`);
  }

}
