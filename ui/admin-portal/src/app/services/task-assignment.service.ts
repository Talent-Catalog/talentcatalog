import {Injectable} from '@angular/core';
import {environment} from "../../environments/environment";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {TaskAssignment} from "../model/candidate";


export interface AssignTaskToListRequest {
  savedListId: number,
  taskId: number,
  dueDate?: Date
}

export interface CreateTaskAssignmentRequest {
  candidateId: number,
  taskId: number,
  dueDate?: Date
}

export interface UpdateTaskAssignmentRequest {
  taskAssignmentId: number,
  dueDate?: Date,
  completedDate?: Date,
  complete?: boolean,
  candidateNotes?: string
}

@Injectable({
  providedIn: 'root'
})
export class TaskAssignmentService {
  private apiUrl: string = environment.apiUrl + '/task-assignment';

  constructor(private http: HttpClient) { }

  assignTaskToList(request: AssignTaskToListRequest): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/assign-to-list`, request);
  }

  createTaskAssignment(request: CreateTaskAssignmentRequest): Observable<TaskAssignment> {
    return this.http.post<TaskAssignment>(`${this.apiUrl}`, request);
  }

  updateTaskAssignment(request: UpdateTaskAssignmentRequest): Observable<TaskAssignment> {
    return this.http.put<TaskAssignment>(`${this.apiUrl}/${request.taskAssignmentId}`, request);
  }

  removeTaskAssignment(id: number): Observable<boolean>  {
    return this.http.delete<boolean>(`${this.apiUrl}/${id}`);
  }

}
