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
import {TaskService, UpdateTaskRequest} from './task.service';
import {environment} from '../../environments/environment';
import {Task} from '../model/task';
import {SearchResults} from '../model/search-results';
import {SearchTaskRequest} from '../model/base';
import {MockJob} from "../MockData/MockJob";

describe('TaskService', () => {
  let service: TaskService;
  let httpMock: HttpTestingController;
  const apiUrl = environment.apiUrl + '/task';

  // Sample data
  const mockTasks: Task[] = MockJob.submissionList.tasks;

  const mockTask: Task = { id: 1, displayName: 'Task 1', description: 'Description 1', daysToComplete: 5, docLink: 'http://example.com', optional: false } as Task;

  const mockSearchResults: SearchResults<Task> = {
    content: mockTasks,
    totalElements: 1
  } as SearchResults<Task>;

  const updateRequest: UpdateTaskRequest = {
    displayName: 'Updated Task',
    description: 'Updated Description',
    daysToComplete: 10,
    docLink: 'http://example.com/help',
    optional: true,
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [TaskService]
    });
    service = TestBed.inject(TaskService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should retrieve a list of tasks from the API via GET', () => {
    service.listTasks().subscribe((tasks) => {
      expect(tasks.length).toBe(1);
      expect(tasks).toEqual(mockTasks);
    });

    const req = httpMock.expectOne(apiUrl);
    expect(req.request.method).toBe('GET');
    req.flush(mockTasks);
  });

  it('should retrieve a task by id via GET', () => {
    service.get(1).subscribe((task) => {
      expect(task).toEqual(mockTask);
    });

    const req = httpMock.expectOne(`${apiUrl}/1`);
    expect(req.request.method).toBe('GET');
    req.flush(mockTask);
  });

  it('should search for tasks via POST', () => {
    const searchRequest: SearchTaskRequest = {};

    service.searchPaged(searchRequest).subscribe((results) => {
      expect(results.totalElements).toBe(1);
      expect(results.content).toEqual(mockTasks);
    });

    const req = httpMock.expectOne(`${apiUrl}/search-paged`);
    expect(req.request.method).toBe('POST');
    req.flush(mockSearchResults);
  });

  it('should update a task via PUT', () => {
    service.update(1, updateRequest).subscribe((task) => {
      expect(task).toEqual({ ...mockTask, ...updateRequest });
    });

    const req = httpMock.expectOne(`${apiUrl}/1`);
    expect(req.request.method).toBe('PUT');
    req.flush({ ...mockTask, ...updateRequest });
  });

});
