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

import {AssignTasksCandidateComponent} from "./assign-tasks-candidate.component";
import {ComponentFixture, fakeAsync, TestBed, tick} from "@angular/core/testing";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {TaskService} from "../../../services/task.service";
import {TaskAssignmentService} from "../../../services/task-assignment.service";
import {FormsModule, ReactiveFormsModule, UntypedFormBuilder} from "@angular/forms";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {NgSelectModule} from "@ng-select/ng-select";
import {of, throwError} from "rxjs";
import {MockCandidate} from "../../../MockData/MockCandidate";

describe('AssignTasksCandidateComponent', () => {
  let component: AssignTasksCandidateComponent;
  let fixture: ComponentFixture<AssignTasksCandidateComponent>;
  let activeModalSpy: jasmine.SpyObj<NgbActiveModal>;
  let taskServiceSpy: jasmine.SpyObj<TaskService>;
  let taskAssignmentServiceSpy: jasmine.SpyObj<TaskAssignmentService>;
  let formBuilder: UntypedFormBuilder;
  const mockCandidate = new MockCandidate();
  const mockTasks = mockCandidate.taskAssignments.slice(0, 3).map(assignment => assignment.task);

  beforeEach(async () => {
    const activeModalSpyObj = jasmine.createSpyObj('NgbActiveModal', ['close', 'dismiss']);
    const taskServiceSpyObj = jasmine.createSpyObj('TaskService', ['listTasks']);
    const taskAssignmentServiceSpyObj = jasmine.createSpyObj('TaskAssignmentService', ['createTaskAssignment']);

    await TestBed.configureTestingModule({
      declarations: [AssignTasksCandidateComponent],
      imports: [HttpClientTestingModule,FormsModule,ReactiveFormsModule,NgSelectModule],
      providers: [
        UntypedFormBuilder,
        { provide: NgbActiveModal, useValue: activeModalSpyObj },
        { provide: TaskService, useValue: taskServiceSpyObj },
        { provide: TaskAssignmentService, useValue: taskAssignmentServiceSpyObj }
      ]
    }).compileComponents();

    activeModalSpy = TestBed.inject(NgbActiveModal) as jasmine.SpyObj<NgbActiveModal>;
    taskServiceSpy = TestBed.inject(TaskService) as jasmine.SpyObj<TaskService>;
    taskAssignmentServiceSpy = TestBed.inject(TaskAssignmentService) as jasmine.SpyObj<TaskAssignmentService>;
    formBuilder = TestBed.inject(UntypedFormBuilder);
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AssignTasksCandidateComponent);
    component = fixture.componentInstance;

    taskServiceSpy.listTasks.and.returnValue(of(mockTasks));
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form with default values', () => {
    expect(component.assignForm).toBeDefined();
    expect(component.assignForm.get('task')).toBeTruthy();
    expect(component.assignForm.get('customDate')).toBeTruthy();
    expect(component.assignForm.get('dueDate')).toBeTruthy();
  });

  it('should load all tasks on initialization', fakeAsync(() => {
    taskServiceSpy.listTasks.and.returnValue(of(mockTasks));

    component.ngOnInit();
    tick(); // Wait for observable to resolve

    expect(taskServiceSpy.listTasks).toHaveBeenCalled();
    expect(component.allTasks).toEqual(mockTasks);
    expect(component.loading).toBeFalse();
  }));

  it('should handle error while loading tasks', fakeAsync(() => {
    const errorMessage = 'Error loading tasks';
    taskServiceSpy.listTasks.and.returnValue(throwError(errorMessage));

    component.ngOnInit();
    tick(); // Wait for observable to resolve

    expect(taskServiceSpy.listTasks).toHaveBeenCalled();
    expect(component.error).toEqual(errorMessage);
    expect(component.loading).toBeFalse();
  }));

  it('should calculate the estimated due date based on selected task', () => {
    component.assignForm.patchValue({ task: mockTasks[0] });
    const estimatedDueDate = new Date();
    estimatedDueDate.setDate(estimatedDueDate.getDate() + 7);
    expect(component.estimatedDueDate.toDateString()).toBe(estimatedDueDate.toDateString());
  });

  it('should call taskAssignmentService.createTaskAssignment on save', () => {
    // spyOn(taskAssignmentServiceSpy, 'createTaskAssignment').and.callThrough();
    taskAssignmentServiceSpy.createTaskAssignment.and.returnValue(of())
    component.candidateId = 1;
    component.assignForm.patchValue({
      task: mockTasks[0],
      customDate: false,
      dueDate: new Date()
    });

    component.onSave();
    expect(taskAssignmentServiceSpy.createTaskAssignment).toHaveBeenCalledWith({
      candidateId: 1,
      taskId: 1,
      dueDate: component.assignForm.value.dueDate
    });
  });

  it('should handle errors when saving task assignment', fakeAsync(() => {
    const errorMessage = 'Error saving task assignment';
    taskAssignmentServiceSpy.createTaskAssignment.and.returnValue(throwError(errorMessage));
    component.candidateId = 1;
    component.assignForm.patchValue({
      task: mockTasks[0],
      customDate: false,
      dueDate: new Date()
    });

    component.onSave();
    tick(); // Wait for observable to resolve

    expect(component.error).toBe(errorMessage);
    expect(component.saving).toBeFalse();
  }));

  it('should dismiss modal on cancel', () => {
    component.cancel();
    expect(activeModalSpy.dismiss).toHaveBeenCalled();
  });
});
