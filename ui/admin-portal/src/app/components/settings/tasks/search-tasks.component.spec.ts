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
import {SearchTasksComponent} from "./search-tasks.component";
import {TaskService} from "../../../services/task.service";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {NgbModal, NgbModule} from "@ng-bootstrap/ng-bootstrap";
import {AuthorizationService} from "../../../services/authorization.service";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";
import {SearchResults} from "../../../model/search-results";
import {of, throwError} from "rxjs";
import {EditTaskComponent} from "./edit/edit-task.component";
import {Task} from "../../../model/task";
import {MockSavedList} from "../../../MockData/MockSavedList";
import {MockUser} from "../../../MockData/MockUser";

describe('SearchTasksComponent', () => {
  let component: SearchTasksComponent;
  let fixture: ComponentFixture<SearchTasksComponent>;
  let taskServiceSpy: jasmine.SpyObj<TaskService>;
  let modalServiceSpy: jasmine.SpyObj<NgbModal>;
  let authServiceSpy: jasmine.SpyObj<AuthorizationService>;
  const mockResults: SearchResults<Task> = {
    first: false,
    last: false,
    number: 0,
    size: 0,
    totalPages: 0,
    totalElements: 1,
    content: MockSavedList.tasks
  };

  beforeEach(async () => {
    const taskSpy = jasmine.createSpyObj('TaskService', ['searchPaged']);
    const modalSpy = jasmine.createSpyObj('NgbModal', ['open']);
    const authSpy = jasmine.createSpyObj('AuthorizationService', ['isAnAdmin']);

    await TestBed.configureTestingModule({
      declarations: [SearchTasksComponent],
      imports: [FormsModule, ReactiveFormsModule, NgbModule, NgSelectModule],
      providers: [
        { provide: TaskService, useValue: taskSpy },
        { provide: NgbModal, useValue: modalSpy },
        { provide: AuthorizationService, useValue: authSpy },
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(SearchTasksComponent);
    component = fixture.componentInstance;
    taskServiceSpy = TestBed.inject(TaskService) as jasmine.SpyObj<TaskService>;
    modalServiceSpy = TestBed.inject(NgbModal) as jasmine.SpyObj<NgbModal>;
    authServiceSpy = TestBed.inject(AuthorizationService) as jasmine.SpyObj<AuthorizationService>;
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SearchTasksComponent);
    component = fixture.componentInstance;
    taskServiceSpy.searchPaged.and.returnValue(of(mockResults));
    component.loggedInUser = new MockUser();
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize forms and set default values', () => {
    component.ngOnInit();
    expect(component.searchForm).toBeDefined();
    expect(component.pageNumber).toBe(1);
    expect(component.pageSize).toBe(50);
  });

  it('should search tasks', () => {

    component.search();

    expect(taskServiceSpy.searchPaged).toHaveBeenCalled();
    expect(component.results).toEqual(mockResults);
    expect(component.loading).toBeFalse();
  });

  it('should handle search errors', () => {
    taskServiceSpy.searchPaged.and.returnValue(throwError('Error'));

    component.search();

    expect(component.error).toBe('Error');
    expect(component.loading).toBeFalse();
  });

  it('should open edit task modal', () => {
    const task: Task = MockSavedList.tasks[0];
    const modalRefMock = { componentInstance: { taskId: null }, result: Promise.resolve(true) } as any;
    modalServiceSpy.open.and.returnValue(modalRefMock);

    component.editTask(task);

    expect(modalServiceSpy.open).toHaveBeenCalledWith(EditTaskComponent, { centered: true, backdrop: 'static' });
    expect(modalRefMock.componentInstance.taskId).toBe(task.id);
  });

  it('should check if user is admin', () => {
    authServiceSpy.isAnAdmin.and.returnValue(true);
    expect(component.isAnAdmin()).toBeTrue();
  });
});
