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

import {Injectable} from "@angular/core";
import {environment} from "../../environments/environment";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {TaskAssignment} from "../model/task-assignment";

export interface UpdateTaskAssignmentRequest {
  completed: boolean,
  abandoned: boolean
  candidateNotes?: string,
}

export interface UpdateUploadTaskAssignmentRequest {
  abandoned: boolean
  candidateNotes?: string,
}

export interface UpdateQuestionTaskAssignmentRequest {
  answer: string;
  abandoned: boolean
  candidateNotes?: string,
}

export interface UpdateTaskCommentRequest {
  candidateNotes: string,
}

@Injectable({
  providedIn: 'root'
})
export class TaskAssignmentService {

  private apiUrl: string = environment.apiUrl + '/task-assignment';

  constructor(private http: HttpClient) { }

  doUploadTask(id: number, formData: FormData): Observable<TaskAssignment> {
    return this.http.post<TaskAssignment>(`${this.apiUrl}/${id}/complete-upload`, formData);
  }

  updateUploadTaskAssignment(id: number, request: UpdateUploadTaskAssignmentRequest) {
    return this.http.put<TaskAssignment>(`${this.apiUrl}/${id}/upload`, request);
  }

  updateQuestionTask(id: number, request: UpdateQuestionTaskAssignmentRequest): Observable<TaskAssignment> {
    return this.http.put<TaskAssignment>(`${this.apiUrl}/${id}/question`, request);
  }

  /**
   * This is used for simple tasks, sending up if the completed checkbox is checked or not as well as the abandoned/notes fields.
   * @param id Id of the task assignment to update.
   * @param request Update Task Assignment Request: completed, abandoned & notes.
   */
  updateTaskAssignment(id: number, request: UpdateTaskAssignmentRequest) {
    return this.http.put<TaskAssignment>(`${this.apiUrl}/${id}`, request);
  }

  /**
   * This is used when a task is already completed to update the comment only.
   * @param id
   * @param request
   */
  updateTaskComment(id: number, request: UpdateTaskCommentRequest) {
    return this.http.put<TaskAssignment>(`${this.apiUrl}/${id}/comment`, request);
  }

}
