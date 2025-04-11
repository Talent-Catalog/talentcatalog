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
import {BrowseTasksComponent} from "./browse-tasks.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {TaskService} from "../../../services/task.service";
import {AuthenticationService} from "../../../services/authentication.service";
import {MockCandidate} from "../../../MockData/MockCandidate";
import {FormsModule, ReactiveFormsModule, UntypedFormBuilder} from "@angular/forms";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {NgSelectModule} from "@ng-select/ng-select";
import {of, throwError} from "rxjs";
import {SearchResults} from "../../../model/search-results";
import {Task} from "../../../model/task";
import {RouterTestingModule} from "@angular/router/testing";
import {ViewTaskDetailsComponent} from "../view-task-details/view-task-details.component";
import {NgbPaginationModule} from "@ng-bootstrap/ng-bootstrap";
import {UpdatedByComponent} from "../../util/user/updated-by/updated-by.component";
import {LocalStorageService} from "../../../services/local-storage.service";

describe('BrowseTasksComponent', () => {
  let component: BrowseTasksComponent;
  let fixture: ComponentFixture<BrowseTasksComponent>;
  let taskServiceSpy: jasmine.SpyObj<TaskService>;
  let localStorageServiceSpy: jasmine.SpyObj<LocalStorageService>;
  let authenticationServiceSpy: jasmine.SpyObj<AuthenticationService>;

  const mockCandidate = new MockCandidate();
  const mockTasks = mockCandidate.taskAssignments.slice(0, 3).map(assignment => assignment.task);
  const mockResults: SearchResults<Task> = {
    first: false,
    last: false,
    number: 0,
    size: 0,
    totalPages: 0,
    totalElements: 1,
    content: mockTasks
  };  beforeEach(async () => {
    const taskService = jasmine.createSpyObj('TaskService', ['searchPaged']);
    const localStorageService = jasmine.createSpyObj('LocalStorageService', ['get', 'set']);
    const authenticationService = jasmine.createSpyObj('AuthenticationService', ['getLoggedInUser']);

    await TestBed.configureTestingModule({
      declarations: [ BrowseTasksComponent, ViewTaskDetailsComponent, UpdatedByComponent ],
      imports: [HttpClientTestingModule, RouterTestingModule,NgbPaginationModule,FormsModule,ReactiveFormsModule,NgSelectModule],
      providers: [
        UntypedFormBuilder,
        { provide: TaskService, useValue: taskService },
        { provide: LocalStorageService, useValue: localStorageService },
        { provide: AuthenticationService, useValue: authenticationService }
      ]
    })
    .compileComponents();

    taskServiceSpy = TestBed.inject(TaskService) as jasmine.SpyObj<TaskService>;
    localStorageServiceSpy = TestBed.inject(LocalStorageService) as jasmine.SpyObj<LocalStorageService>;
    authenticationServiceSpy = TestBed.inject(AuthenticationService) as jasmine.SpyObj<AuthenticationService>;

    taskServiceSpy.searchPaged.and.returnValue(of(mockResults));
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(BrowseTasksComponent);
    component = fixture.componentInstance;
    taskServiceSpy.searchPaged.and.returnValue(of(mockResults));
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load tasks on init', () => {
    expect(taskServiceSpy.searchPaged).toHaveBeenCalled();
    expect(component.results.content.length).toBe(3);
  });

  it('should handle error on task load', () => {
    taskServiceSpy.searchPaged.and.returnValue(throwError('Error'));
    component.search();
    expect(component.error).toBe('Error');
  });

  it('should select a task', () => {
    const task = mockTasks[0];
    component.select(task);
    expect(component.selectedTask).toBe(task);
    expect(localStorageServiceSpy.set).toHaveBeenCalledWith('BrowseKeyTasks', task.id);
  });

  it('should handle keyboard navigation', () => {
    component.results = mockResults;

    const eventDown = new KeyboardEvent('keydown', { key: 'ArrowDown' });
    const eventUp = new KeyboardEvent('keydown', { key: 'ArrowUp' });

    component.keyDown(eventDown);
    expect(component.selectedIndex).toBe(1);
    expect(component.selectedTask).toBe(mockTasks[1]);

    component.keyDown(eventUp);
    expect(component.selectedIndex).toBe(0);
    expect(component.selectedTask).toBe(mockTasks[0]);
  });


});
