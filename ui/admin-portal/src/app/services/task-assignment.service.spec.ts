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

import {TestBed} from '@angular/core/testing';
import {HttpClientTestingModule, HttpTestingController} from '@angular/common/http/testing';
import {TaskAssignmentService, TaskListRequest, CreateTaskAssignmentRequest, UpdateTaskAssignmentRequest} from './task-assignment.service';
import {environment} from '../../environments/environment';
import {TaskAssignment} from '../model/task-assignment';
import {MockCandidate} from "../MockData/MockCandidate";

describe('TaskAssignmentService', () => {
  let service: TaskAssignmentService;
  let httpMock: HttpTestingController;
  const apiUrl = environment.apiUrl + '/task-assignment';

  const mockTaskAssignment: TaskAssignment = new MockCandidate().taskAssignments[0];

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [TaskAssignmentService]
    });
    service = TestBed.inject(TaskAssignmentService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should assign a task to a list via PUT', () => {
    const request: TaskListRequest = { savedListId: 1, taskId: 1 };

    service.assignTaskToList(request).subscribe(response => {
      expect(response).toBeNull();
    });

    const req = httpMock.expectOne(`${apiUrl}/assign-to-list`);
    expect(req.request.method).toBe('PUT');
    req.flush(null); // since the API response is void
  });

  it('should search for task assignments via POST', () => {
    const request: TaskListRequest = { savedListId: 1, taskId: 1 };

    service.search(request).subscribe((taskAssignments) => {
      expect(taskAssignments.length).toBe(1);
      expect(taskAssignments).toEqual([mockTaskAssignment]);
    });

    const req = httpMock.expectOne(`${apiUrl}/search`);
    expect(req.request.method).toBe('POST');
    req.flush([mockTaskAssignment]);
  });

  it('should remove a task from a list via PUT', () => {
    const request: TaskListRequest = { savedListId: 1, taskId: 1 };

    service.removeTaskFromList(request).subscribe(response => {
      expect(response).toBeNull();
    });

    const req = httpMock.expectOne(`${apiUrl}/remove-from-list`);
    expect(req.request.method).toBe('PUT');
    req.flush(null); // since the API response is void
  });

  it('should create a task assignment via POST', () => {
    const request: CreateTaskAssignmentRequest = { candidateId: 1, taskId: 1, dueDate: new Date() };

    service.createTaskAssignment(request).subscribe((taskAssignment) => {
      expect(taskAssignment).toEqual(mockTaskAssignment);
    });

    const req = httpMock.expectOne(apiUrl);
    expect(req.request.method).toBe('POST');
    req.flush(mockTaskAssignment);
  });

  it('should update a task assignment via PUT', () => {
    const request: UpdateTaskAssignmentRequest = { taskAssignmentId: 1, completed: true, abandoned: false, dueDate: new Date(), candidateNotes: 'Updated Notes' };

    service.updateTaskAssignment(request).subscribe((taskAssignment) => {
      expect(taskAssignment).toEqual({ ...mockTaskAssignment, ...request });
    });

    const req = httpMock.expectOne(`${apiUrl}/1`);
    expect(req.request.method).toBe('PUT');
    req.flush({ ...mockTaskAssignment, ...request });
  });

  it('should remove a task assignment via DELETE', () => {
    service.removeTaskAssignment(1).subscribe((response) => {
      expect(response).toBeTrue();
    });

    const req = httpMock.expectOne(`${apiUrl}/1`);
    expect(req.request.method).toBe('DELETE');
    req.flush(true);
  });

});
