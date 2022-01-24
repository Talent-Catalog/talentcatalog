import { Injectable } from '@angular/core';
import {environment} from "../../environments/environment";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import { Task } from '../model/candidate';

@Injectable({
  providedIn: 'root'
})
export class TaskService {

  private apiUrl: string = environment.apiUrl + '/task';

  constructor(private http: HttpClient) { }


  listTasks(): Observable<Task[]> {
    return this.http.get<Task[]>(`${this.apiUrl}`);
  }

}
