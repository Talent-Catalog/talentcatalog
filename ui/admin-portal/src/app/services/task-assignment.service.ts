import {Injectable} from '@angular/core';
import {environment} from "../../environments/environment";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {TaskAssignment} from "../model/candidate";


export interface CreateTaskAssignmentRequest {
  candidateId: number,
  taskId: number,
  dueDate?: Date
}

export interface UpdateTaskAssignmentRequest {
  id: number,
  dueDate?: Date,
  completedDate?: Date,
  complete?: boolean
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

  updateTaskAssignment(request: UpdateTaskAssignmentRequest): Observable<TaskAssignment> {
    return this.http.put<TaskAssignment>(`${this.apiUrl}/${request.id}`, request);
  }

  removeTaskAssignment(id: number): Observable<boolean>  {
    return this.http.delete<boolean>(`${this.apiUrl}/${id}`);
  }

}
