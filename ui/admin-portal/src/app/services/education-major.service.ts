import {Injectable} from '@angular/core';
import {environment} from "../../environments/environment";
import {HttpClient} from "@angular/common/http";
import {EducationMajor} from "../model/education-major";
import {Observable} from "rxjs";
import {SearchResults} from "../model/search-results";
import {User} from "../model/user";
import {Candidate} from "../model/candidate";

@Injectable({
  providedIn: 'root'
})
export class EducationMajorService {

  private apiUrl: string = environment.apiUrl + '/education-major';

  constructor(private http: HttpClient) { }

  listNationalities(): Observable<EducationMajor[]> {
    return this.http.get<EducationMajor[]>(`${this.apiUrl}`);
  }

  search(request): Observable<SearchResults<EducationMajor>> {
    return this.http.post<SearchResults<EducationMajor>>(`${this.apiUrl}/search`, request);
  }

  get(id: number): Observable<EducationMajor> {
    return this.http.get<EducationMajor>(`${this.apiUrl}/${id}`);
  }

  create(details): Observable<EducationMajor>  {
    return this.http.post<EducationMajor>(`${this.apiUrl}`, details);
  }

  update(id: number, details): Observable<EducationMajor>  {
    return this.http.put<EducationMajor>(`${this.apiUrl}/${id}`, details);
  }

  delete(id: number): Observable<boolean>  {
    return this.http.delete<boolean>(`${this.apiUrl}/${id}`);
  }

}
