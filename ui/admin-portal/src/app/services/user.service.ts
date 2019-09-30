import { Injectable } from '@angular/core';
import { Candidate } from '../model/candidate';
import { Observable } from 'rxjs/index';
import { environment } from '../../environments/environment';
import { HttpClient } from '@angular/common/http';
import { SearchResults } from '../model/search-results';
import {User} from "../model/user";

@Injectable({providedIn: 'root'})
export class UserService {

  private apiUrl = environment.apiUrl + '/user';

  constructor(private http:HttpClient) {}

  search(request): Observable<SearchResults<User>> {
    return this.http.post<SearchResults<User>>(`${this.apiUrl}/search`, request);
  }

  get(id: number): Observable<User> {
    return this.http.get<User>(`${this.apiUrl}/${id}`);
  }

  create(details): Observable<User>  {
    return this.http.post<User>(`${this.apiUrl}`, details);
  }

  update(id: number, details): Observable<User>  {
    return this.http.put<User>(`${this.apiUrl}/${id}`, details);
  }

  delete(id: number): Observable<boolean>  {
    return this.http.delete<boolean>(`${this.apiUrl}/${id}`);
  }
}
