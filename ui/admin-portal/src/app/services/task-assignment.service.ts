import { Injectable } from '@angular/core';
import {environment} from "../../environments/environment";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {Task, TaskAssignment} from "../model/candidate";


export interface CreateTaskAssignmentRequest {
  candidateId: number,
  taskId: number
}

@Injectable({
  providedIn: 'root'
})
export class TaskAssignmentService {
  private apiUrl: string = environment.apiUrl + '/task-assignment';

  constructor(private http: HttpClient) { }

  createTaskAssignment(request: CreateTaskAssignmentRequest): Observable<TaskAssignment> {
    return this.http.post<TaskAssignment>(`${this.apiUrl}`, request);
  }

}
