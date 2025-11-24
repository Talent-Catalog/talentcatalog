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
import {EditTaskAssignmentComponent} from "./edit-task-assignment.component";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {ComponentFixture, fakeAsync, TestBed, tick} from "@angular/core/testing";
import {TaskAssignmentService} from "../../../../../services/task-assignment.service";
import {ReactiveFormsModule, UntypedFormBuilder} from "@angular/forms";
import {LOCALE_ID, NO_ERRORS_SCHEMA} from "@angular/core";
import {MockCandidate} from "../../../../../MockData/MockCandidate";
import {By} from "@angular/platform-browser";
import {TaskAssignment} from "../../../../../model/task-assignment";
import {of, throwError} from "rxjs";

describe('EditTaskAssignmentComponent', () => {
  let component: EditTaskAssignmentComponent;
  let fixture: ComponentFixture<EditTaskAssignmentComponent>;
  let mockActiveModal: NgbActiveModal;
  let mockTaskAssignmentService: jasmine.SpyObj<TaskAssignmentService>;
  const mockCandidate = new MockCandidate();
  beforeEach(async () => {
    mockActiveModal = jasmine.createSpyObj('NgbActiveModal', ['close', 'dismiss']);
    mockTaskAssignmentService = jasmine.createSpyObj('TaskAssignmentService', ['updateTaskAssignment']);

    await TestBed.configureTestingModule({
      declarations: [ EditTaskAssignmentComponent ],
      imports: [ ReactiveFormsModule ],
      providers: [
        { provide: NgbActiveModal, useValue: mockActiveModal },
        { provide: TaskAssignmentService, useValue: mockTaskAssignmentService },
        UntypedFormBuilder,
        { provide: LOCALE_ID, useValue: 'en-US' }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(EditTaskAssignmentComponent);
    component = fixture.componentInstance;

    // Mock data
    component.taskAssignment = mockCandidate.taskAssignments[0];

    component.loading = false;
    component.saving = false;
    component.error = '';

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize the form with due date and complete status', () => {
    component.ngOnInit();
    expect(component.form.value.dueDate).toBe(component.formatTbbDate(component.taskAssignment.dueDate));
    expect(component.form.value.complete).toBe(component.isComplete);
  });

  it('should display error message when error is set', fakeAsync(() => {
    component.error = 'Sample error message';
    fixture.detectChanges();
    tick();

    const errorAlert = fixture.debugElement.query(By.css('.alert-danger'));
    expect(errorAlert.nativeElement.textContent).toContain('Sample error message');
  }));

  it('should call activeModal.close with taskAssignment when closeModal is called', () => {
    const taskAssignment = {} as TaskAssignment;
    component.closeModal(taskAssignment);
    expect(mockActiveModal.close).toHaveBeenCalledWith(taskAssignment);
  });

  it('should call activeModal.dismiss when dismiss is called', () => {
    component.dismiss();
    expect(mockActiveModal.dismiss).toHaveBeenCalledWith(false);
  });

  it('should call updateTaskAssignment on onSave and handle success', fakeAsync(() => {
    const updatedTaskAssignment = { ...component.taskAssignment, completedDate: new Date() } as TaskAssignment;
    mockTaskAssignmentService.updateTaskAssignment.and.returnValue(of(updatedTaskAssignment));

    component.onSave();
    tick();

    expect(mockTaskAssignmentService.updateTaskAssignment).toHaveBeenCalled();
    expect(mockActiveModal.close).toHaveBeenCalledWith(updatedTaskAssignment);
    expect(component.saving).toBeFalse();
  }));

  it('should call updateTaskAssignment on onSave and handle error', fakeAsync(() => {
    mockTaskAssignmentService.updateTaskAssignment.and.returnValue(throwError('Error'));

    component.onSave();
    tick();

    expect(mockTaskAssignmentService.updateTaskAssignment).toHaveBeenCalled();
    expect(component.error).toBe('Error');
    expect(component.saving).toBeFalse();
  }));
});
