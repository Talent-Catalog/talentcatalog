import {Injectable} from '@angular/core';
import {environment} from "../../environments/environment";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {TaskAssignment} from "../model/task-assignment";


export interface TaskListRequest {
  savedListId: number,
  taskId: number
}

export interface CreateTaskAssignmentRequest {
  candidateId: number,
  taskId: number,
  dueDate?: Date
}

export interface UpdateTaskAssignmentRequest {
  taskAssignmentId: number,
  completed: boolean,
  abandoned: boolean,
  dueDate?: Date,
  candidateNotes?: string
}

@Injectable({
  providedIn: 'root'
})
export class TaskAssignmentService {
  private apiUrl: string = environment.apiUrl + '/task-assignment';

  constructor(private http: HttpClient) { }

  assignTaskToList(request: TaskListRequest): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/assign-to-list`, request);
  }

  search(request: TaskListRequest): Observable<TaskAssignment[]> {
    return this.http.post<TaskAssignment[]>(`${this.apiUrl}/search`, request);
  }

  removeTaskFromList(request: TaskListRequest): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/remove-from-list`, request);
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
