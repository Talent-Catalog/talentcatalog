import { Injectable } from '@angular/core';
import {environment} from "../../environments/environment";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {SearchResults} from "../model/search-results";
import {CandidateAttachment} from "../model/candidate-attachment";
import {Task} from "../model/task";

@Injectable({
  providedIn: 'root'
})
export class TaskService {

  private apiUrl: string = environment.apiUrl + '/task';

  constructor(private http: HttpClient) { }


  listTasks(): Observable<Task[]> {
    return this.http.get<Task[]>(`${this.apiUrl}`);
  }

  searchPaged(request): Observable<SearchResults<Task>> {
    return this.http.post<SearchResults<Task>>(`${this.apiUrl}/search-paged`, request);
  }

}
