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
import {EditTaskComponent} from "./edit-task.component";
import {TaskService, UpdateTaskRequest} from "../../../../services/task.service";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {ReactiveFormsModule, UntypedFormBuilder} from "@angular/forms";
import {ComponentFixture, fakeAsync, TestBed, tick} from "@angular/core/testing";
import {NgSelectModule} from "@ng-select/ng-select";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {of, throwError} from "rxjs";
import {Task} from "../../../../model/task";
import {NgxWigModule} from "ngx-wig";

describe('EditTaskComponent', () => {
  let component: EditTaskComponent;
  let fixture: ComponentFixture<EditTaskComponent>;
  let taskServiceSpy: jasmine.SpyObj<TaskService>;
  let ngbActiveModalSpy: jasmine.SpyObj<NgbActiveModal>;
  let formBuilder: UntypedFormBuilder;
  // @ts-expect-error
  const taskData:Task = {
    displayName: 'Task 1',
    description: 'Description of Task 1',
    daysToComplete: 3,
    optional: false,
    docLink: 'https://example.com'
  };
  //@ts-expect-error
  const updatedTask: Task = {
    id: 1,
    displayName: 'Updated Task',
    description: 'Updated description',
    daysToComplete: 5,
    optional: true,
    docLink: 'https://updated-link.com'
  };

  beforeEach(async () => {
    const taskServiceSpyObj = jasmine.createSpyObj('TaskService', ['get', 'update']);
    const ngbActiveModalSpyObj = jasmine.createSpyObj('NgbActiveModal', ['close', 'dismiss']);

    await TestBed.configureTestingModule({
      declarations: [EditTaskComponent],
      imports: [ReactiveFormsModule,NgSelectModule,HttpClientTestingModule, NgxWigModule],
      providers: [
        { provide: TaskService, useValue: taskServiceSpyObj },
        { provide: NgbActiveModal, useValue: ngbActiveModalSpyObj }
      ]
    }).compileComponents();

    taskServiceSpy = TestBed.inject(TaskService) as jasmine.SpyObj<TaskService>;
    ngbActiveModalSpy = TestBed.inject(NgbActiveModal) as jasmine.SpyObj<NgbActiveModal>;
    formBuilder = TestBed.inject(UntypedFormBuilder);
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(EditTaskComponent);
    component = fixture.componentInstance;
    component.taskId = 1; // Assuming a task ID is provided
    taskServiceSpy.get.and.returnValue(of(taskData));
    taskServiceSpy.update.and.returnValue(of(updatedTask));
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load task data on initialization', fakeAsync(() => {
    component.ngOnInit();
    tick(); // Waiting for async operation to complete
    expect(taskServiceSpy.get).toHaveBeenCalledWith(1);
    expect(component.taskForm.value).toEqual({
      displayName: 'Task 1',
      description: 'Description of Task 1',
      daysToComplete: 3,
      optional: false,
      docLink: 'https://example.com'
    });
    expect(component.loading).toBeFalse();
  }));

  it('should call onSave and close modal when task is successfully updated', fakeAsync(() => {
    component.taskForm.patchValue({
      displayName: 'Updated Task',
      description: 'Updated description',
      daysToComplete: 5,
      optional: true,
      docLink: 'https://updated-link.com'
    });

    component.onSave();
    tick(); // Waiting for async operation to complete

    const expectedRequest: UpdateTaskRequest = {
      displayName: 'Updated Task',
      description: 'Updated description',
      daysToComplete: 5,
      optional: true,
      docLink: 'https://updated-link.com'
    };
    expect(taskServiceSpy.update).toHaveBeenCalledWith(1, expectedRequest);
    expect(ngbActiveModalSpy.close).toHaveBeenCalledWith(updatedTask);
    expect(component.saving).toBeFalse();
  }));

  it('should handle error when updating task fails', fakeAsync(() => {
    const errorResponse = { status: 500, message: 'Internal Server Error' };
    taskServiceSpy.update.and.returnValue(throwError(errorResponse));

    component.onSave();
    tick(); // Waiting for async operation to complete

    expect(component.error).toEqual(errorResponse);
    expect(component.saving).toBeFalse();
  }));

  it('should dismiss modal when dismiss is called', () => {
    component.dismiss();
    expect(ngbActiveModalSpy.dismiss).toHaveBeenCalledWith(false);
  });
});
