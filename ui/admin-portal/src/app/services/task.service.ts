import {Injectable} from '@angular/core';
import {environment} from "../../environments/environment";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {SearchResults} from "../model/search-results";
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

  get(id: number): Observable<Task> {
    return this.http.get<Task>(`${this.apiUrl}/${id}`);
  }

  update(id: number, details): Observable<Task>  {
    return this.http.put<Task>(`${this.apiUrl}/${id}`, details);
  }


}
