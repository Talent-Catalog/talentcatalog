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

import {AssignTasksListComponent} from "./assign-tasks-list.component";
import {ComponentFixture, fakeAsync, TestBed, tick} from "@angular/core/testing";
import {NgbActiveModal, NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {TaskService} from "../../../services/task.service";
import {TaskAssignmentService} from "../../../services/task-assignment.service";
import {SavedListService} from "../../../services/saved-list.service";
import {ConfirmationComponent} from "../../util/confirm/confirmation.component";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {FormsModule, ReactiveFormsModule, UntypedFormBuilder} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";
import {MockCandidate} from "../../../MockData/MockCandidate";
import {of, throwError} from "rxjs";
import {SavedList} from "../../../model/saved-list";
import {MockSavedList} from "../../../MockData/MockSavedList";
import {TaskType} from "../../../model/task";

describe('AssignTasksListComponent', () => {
  let component: AssignTasksListComponent;
  let fixture: ComponentFixture<AssignTasksListComponent>;
  let activeModalSpy: jasmine.SpyObj<NgbActiveModal>;
  let modalServiceSpy: jasmine.SpyObj<NgbModal>;
  let taskServiceSpy: jasmine.SpyObj<TaskService>;
  let taskAssignmentServiceSpy: jasmine.SpyObj<TaskAssignmentService>;
  let savedListServiceSpy: jasmine.SpyObj<SavedListService>;
  const mockCandidate = new MockCandidate();
  const mockTasks = mockCandidate.taskAssignments.slice(0, 3).map(assignment => assignment.task);

  beforeEach(async () => {
    const activeModalSpyObj = jasmine.createSpyObj('NgbActiveModal', ['close', 'dismiss']);
    const modalServiceSpyObj = jasmine.createSpyObj('NgbModal', ['open']);
    const taskServiceSpyObj = jasmine.createSpyObj('TaskService', ['listTasks']);
    const taskAssignmentServiceSpyObj = jasmine.createSpyObj('TaskAssignmentService', ['assignTaskToList', 'removeTaskFromList']);
    const savedListServiceSpyObj = jasmine.createSpyObj('SavedListService', ['get']);

    await TestBed.configureTestingModule({
      declarations: [AssignTasksListComponent, ConfirmationComponent],
      imports: [HttpClientTestingModule,FormsModule,ReactiveFormsModule,NgSelectModule],
      providers: [
        UntypedFormBuilder,
        { provide: NgbActiveModal, useValue: activeModalSpyObj },
        { provide: NgbModal, useValue: modalServiceSpyObj },
        { provide: TaskService, useValue: taskServiceSpyObj },
        { provide: TaskAssignmentService, useValue: taskAssignmentServiceSpyObj },
        { provide: SavedListService, useValue: savedListServiceSpyObj }
      ]
    }).compileComponents();

    activeModalSpy = TestBed.inject(NgbActiveModal) as jasmine.SpyObj<NgbActiveModal>;
    modalServiceSpy = TestBed.inject(NgbModal) as jasmine.SpyObj<NgbModal>;
    taskServiceSpy = TestBed.inject(TaskService) as jasmine.SpyObj<TaskService>;
    taskAssignmentServiceSpy = TestBed.inject(TaskAssignmentService) as jasmine.SpyObj<TaskAssignmentService>;
    savedListServiceSpy = TestBed.inject(SavedListService) as jasmine.SpyObj<SavedListService>;
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AssignTasksListComponent);
    component = fixture.componentInstance;
    taskServiceSpy.listTasks.and.returnValue(of(mockTasks));
    component.savedList = MockSavedList;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form on ngOnInit', () => {
    component.ngOnInit();
    expect(component.assignForm).toBeDefined();
    expect(component.assignForm.get('task')).toBeDefined();
  });

  it('should load tasks on ngOnInit', fakeAsync(() => {
    const tasks = mockTasks;
    taskServiceSpy.listTasks.and.returnValue(of(tasks));

    component.ngOnInit();
    tick();

    expect(component.allTasks).toEqual(tasks);
    expect(component.loading).toBeFalse();
    expect(taskServiceSpy.listTasks).toHaveBeenCalled();
  }));

  it('should handle error when loading tasks', fakeAsync(() => {
    const errorMessage = 'Error loading tasks';
    taskServiceSpy.listTasks.and.returnValue(throwError(errorMessage));

    component.ngOnInit();
    tick();

    expect(component.error).toEqual(errorMessage);
    expect(component.loading).toBeFalse();
    expect(taskServiceSpy.listTasks).toHaveBeenCalled();
  }));

  it('should calculate estimated due date correctly', () => {
    const task = mockTasks[0];
    component.assignForm.patchValue({ task });
    const estDate = new Date();
    estDate.setDate(estDate.getDate() + task.daysToComplete);

    const dueDate = component.estimatedDueDate;

    // Compare only the year, month, and date to avoid time comparison issues
    expect(dueDate.getFullYear()).toEqual(estDate.getFullYear());
    expect(dueDate.getMonth()).toEqual(estDate.getMonth());
    expect(dueDate.getDate()).toEqual(estDate.getDate());
  });


  it('should save task association', fakeAsync(() => {
    const task = mockTasks[0];
    const savedList: SavedList = MockSavedList;
    component.savedList = savedList;
    component.assignForm.patchValue({ task });

    taskAssignmentServiceSpy.assignTaskToList.and.returnValue(of(null));
    savedListServiceSpy.get.and.returnValue(of(savedList));

    component.onSave();
    tick();

    expect(taskAssignmentServiceSpy.assignTaskToList).toHaveBeenCalledWith({ savedListId: savedList.id, taskId: task.id });
    expect(savedListServiceSpy.get).toHaveBeenCalledWith(savedList.id);
    expect(component.savedList.tasks).toEqual(savedList.tasks);
    expect(component.loading).toBeFalse();
  }));

  it('should handle error when saving task association', fakeAsync(() => {
    const errorMessage = 'Error saving task association';
    const task = mockTasks[0];
    component.assignForm.patchValue({ task });

    taskAssignmentServiceSpy.assignTaskToList.and.returnValue(throwError(errorMessage));
    component.onSave();
    tick();

    expect(component.error).toEqual(errorMessage);
    expect(component.loading).toBeFalse();
  }));

  it('should remove task association', fakeAsync(() => {
    const task = mockTasks[0];
    const savedList = MockSavedList;
    component.savedList = savedList;
    const modalRefMock = {
      componentInstance: { message: '' , title:''},
      result: Promise.resolve(true)
    } as any;
    modalServiceSpy.open.and.returnValue(modalRefMock);
    taskAssignmentServiceSpy.removeTaskFromList.and.returnValue(of(null));
    savedListServiceSpy.get.and.returnValue(of(savedList));

    component.removeTask(task);
    tick();

    expect(taskAssignmentServiceSpy.removeTaskFromList).toHaveBeenCalledWith({ savedListId: savedList.id, taskId: task.id });
    expect(savedListServiceSpy.get).toHaveBeenCalledWith(savedList.id);
    expect(component.savedList.tasks).toEqual(savedList.tasks);
    expect(component.loading).toBeFalse();
  }));

  it('should handle error when removing task association', fakeAsync(() => {
    const errorMessage = 'Error removing task association';
    const task = mockTasks[0];
    component.savedList = MockSavedList;

    const modalRefMock = {
      componentInstance: { message: '' , title:''},
      result: Promise.resolve(true)
    } as any;
    modalServiceSpy.open.and.returnValue(modalRefMock);

    taskAssignmentServiceSpy.removeTaskFromList.and.returnValue(throwError(errorMessage));

    component.removeTask(task);
    tick();

    expect(component.error).toEqual(errorMessage);
    expect(component.loading).toBeFalse();
  }));

  it('should search for tasks by type or name', () => {
    const task = mockTasks[0];
    expect(component.searchTypeOrName('CV Submission', task)).toBeTrue();
    expect(component.searchTypeOrName(TaskType.Upload, task)).toBeTrue();
    expect(component.searchTypeOrName('not found', task)).toBeFalse();
  });
});
