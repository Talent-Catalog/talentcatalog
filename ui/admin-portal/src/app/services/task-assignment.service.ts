/*
 * Copyright (c) 2024 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

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
