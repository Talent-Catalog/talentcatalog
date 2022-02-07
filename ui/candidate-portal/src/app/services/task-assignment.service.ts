/*
 * Copyright (c) 2022 Talent Beyond Boundaries.
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
import {TaskAssignment, YesNo} from "../model/candidate";

// todo merge the update task assignment request with the simple task/question task request.
// This would then only one Save button rather than two on the same page.
// So a candidate can complete the simple/question task, and add a comment and click one save button.
export interface UpdateTaskAssignmentRequest {
  taskAssignmentId: number,
  abandoned: boolean
  completed: boolean,
  dueDate?: Date,
  completedDate?: Date,
  candidateNotes?: string,
  completeSimple?: boolean,
  completeQuestion?: string,
  completeYNQuestion?: YesNo
}

export interface CompleteSimpleTaskRequest {
  completed: boolean;
}

export interface CompleteQuestionTaskRequest {
  answer: string;
}

@Injectable({
  providedIn: 'root'
})
export class TaskAssignmentService {

  private apiUrl: string = environment.apiUrl + '/task-assignment';

  constructor(private http: HttpClient) { }

  completeSimpleTask(id: number, request: CompleteSimpleTaskRequest): Observable<TaskAssignment> {
    return this.http.post<TaskAssignment>(`${this.apiUrl}/${id}/complete`, request);
  }

  completeQuestionTask(id: number, request: CompleteQuestionTaskRequest): Observable<TaskAssignment> {
    return this.http.post<TaskAssignment>(`${this.apiUrl}/${id}/complete-question`, request);
  }

  completeYNQuestionTask(id: number, request: CompleteQuestionTaskRequest): Observable<TaskAssignment> {
    return this.http.post<TaskAssignment>(`${this.apiUrl}/${id}/complete-question`, request);
  }

  completeUploadTask(id: number, formData: FormData): Observable<TaskAssignment> {
    return this.http.post<TaskAssignment>(`${this.apiUrl}/${id}/complete-upload`, formData);
  }

  update(id: number, request: UpdateTaskAssignmentRequest) {
    return this.http.put<TaskAssignment>(`${this.apiUrl}/${id}`, request);
  }

}
