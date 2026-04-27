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

import {Component, EventEmitter, Input, Output, forwardRef} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {By} from '@angular/platform-browser';
import {ControlValueAccessor, NG_VALUE_ACCESSOR, ReactiveFormsModule} from '@angular/forms';
import {TranslateModule} from '@ngx-translate/core';
import {of, throwError} from 'rxjs';

import {CandidateTaskComponent} from './candidate-task.component';
import {TaskAssignmentService} from '../../../../../../services/task-assignment.service';
import {TaskAssignment} from '../../../../../../model/task-assignment';
import {TaskType} from '../../../../../../model/task';
import {Status} from '../../../../../../model/base';

@Component({selector: 'app-error', template: ''})
class ErrorStubComponent {
  @Input() error?: unknown;
}

@Component({selector: 'tc-button', template: '<ng-content></ng-content>'})
class TcButtonStubComponent {
  @Input() size?: string;
  @Input() color?: string;
  @Input() disabled?: boolean;
  @Output() onClick = new EventEmitter<void>();
}

@Component({selector: 'tc-label', template: '<ng-content></ng-content>'})
class TcLabelStubComponent {
  @Input() for?: string;
}

@Component({
  selector: 'tc-textarea',
  template: '',
  providers: [{
    provide: NG_VALUE_ACCESSOR,
    useExisting: forwardRef(() => TcTextareaStubComponent),
    multi: true
  }]
})
class TcTextareaStubComponent implements ControlValueAccessor {
  @Input() id?: string;
  @Input() rows?: string | number;
  @Input() formControlName?: string;
  writeValue(): void {}
  registerOnChange(): void {}
  registerOnTouched(): void {}
}

@Component({selector: 'app-view-upload-task', template: ''})
class ViewUploadTaskStubComponent {
  @Input() form?: unknown;
  @Input() candidate?: unknown;
  @Input() selectedTask?: unknown;
  @Output() successfulUpload = new EventEmitter<any>();
}

@Component({selector: 'app-task-submitted', template: ''})
class TaskSubmittedStubComponent {
  @Input() selectedTask?: unknown;
  @Output() onReturnToTasksClick = new EventEmitter<void>();
}

@Component({selector: 'app-task-abandoned', template: ''})
class TaskAbandonedStubComponent {
  @Input() selectedTask?: unknown;
  @Output() onReturnToTasksClick = new EventEmitter<void>();
}

@Component({selector: 'app-view-form-task', template: ''})
class ViewFormTaskStubComponent {
  @Input() taskAssignment?: unknown;
  @Input() candidate?: unknown;
  @Input() readOnly?: boolean;
  @Output() taskCompleted = new EventEmitter<any>();
}

@Component({selector: 'app-view-simple-task', template: ''})
class ViewSimpleTaskStubComponent {
  @Input() form?: unknown;
  @Input() selectedTask?: unknown;
}

@Component({selector: 'app-view-question-task', template: ''})
class ViewQuestionTaskStubComponent {
  @Input() form?: unknown;
  @Input() selectedTask?: unknown;
}

function makeTaskAssignment(overrides: Partial<TaskAssignment> = {}): TaskAssignment {
  return {
    id: 1,
    abandonedDate: null,
    candidateNotes: 'Existing note',
    completedDate: null,
    dueDate: new Date('2099-01-01'),
    status: Status.active,
    answer: 'Yes',
    task: {
      id: 1,
      name: 'question',
      description: 'Task description',
      displayName: 'Question task',
      optional: false,
      docLink: null,
      taskType: TaskType.Question,
      daysToComplete: 1,
      uploadType: null,
      uploadSubfolderName: '',
      uploadableFileTypes: '',
      candidateAnswerField: '',
      allowedAnswers: [
        {name: 'yes', displayName: 'Yes'}
      ]
    } as any,
    ...overrides
  };
}

describe('CandidateTaskComponent', () => {
  let component: CandidateTaskComponent;
  let fixture: ComponentFixture<CandidateTaskComponent>;
  let taskAssignmentServiceSpy: jasmine.SpyObj<TaskAssignmentService>;

  async function configureAndCreate(options?: {
    selectedTask?: TaskAssignment;
    updateQuestionError?: unknown;
  }) {
    taskAssignmentServiceSpy = jasmine.createSpyObj('TaskAssignmentService', [
      'updateQuestionTask',
      'updateTaskAssignment',
      'updateUploadTaskAssignment',
      'updateTaskComment'
    ]);

    taskAssignmentServiceSpy.updateQuestionTask.and.returnValue(
      options?.updateQuestionError
        ? throwError(options.updateQuestionError)
        : of(makeTaskAssignment())
    );
    taskAssignmentServiceSpy.updateTaskAssignment.and.returnValue(of(makeTaskAssignment()));
    taskAssignmentServiceSpy.updateUploadTaskAssignment.and.returnValue(of(makeTaskAssignment()));
    taskAssignmentServiceSpy.updateTaskComment.and.returnValue(of(makeTaskAssignment()));

    await TestBed.configureTestingModule({
      declarations: [
        CandidateTaskComponent,
        ErrorStubComponent,
        TcButtonStubComponent,
        TcLabelStubComponent,
        TcTextareaStubComponent,
        ViewUploadTaskStubComponent,
        TaskSubmittedStubComponent,
        TaskAbandonedStubComponent,
        ViewFormTaskStubComponent,
        ViewSimpleTaskStubComponent,
        ViewQuestionTaskStubComponent
      ],
      imports: [ReactiveFormsModule, TranslateModule.forRoot()],
      providers: [
        {provide: TaskAssignmentService, useValue: taskAssignmentServiceSpy}
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(CandidateTaskComponent);
    component = fixture.componentInstance;
    component.selectedTask = options?.selectedTask ?? makeTaskAssignment();
    component.candidate = {id: 1} as any;

    fixture.detectChanges();
  }

  afterEach(() => TestBed.resetTestingModule());

  it('should create', async () => {
    await configureAndCreate();
    expect(component).toBeTruthy();
  });

  describe('template tc components', () => {
    it('should render tc-button actions and comment field migration pieces', async () => {
      await configureAndCreate();

      const buttons = fixture.debugElement.queryAll(By.directive(TcButtonStubComponent));
      const labels = fixture.debugElement.queryAll(By.directive(TcLabelStubComponent));
      const textarea = fixture.debugElement.query(By.directive(TcTextareaStubComponent));

      expect(buttons.length).toBe(2);
      expect(buttons[0].componentInstance.size).toBe('sm');
      expect(labels[1].componentInstance.for).toBe('comment');
      expect(textarea.componentInstance.id).toBe('comment');
    });

    it('should render the question task child for question tasks', async () => {
      await configureAndCreate();

      expect(fixture.debugElement.query(By.directive(ViewQuestionTaskStubComponent))).toBeTruthy();
    });

    it('should render the upload task child for upload tasks', async () => {
      await configureAndCreate({
        selectedTask: makeTaskAssignment({
          task: {...makeTaskAssignment().task, taskType: TaskType.Upload} as any
        })
      });

      expect(fixture.debugElement.query(By.directive(ViewUploadTaskStubComponent))).toBeTruthy();
    });
  });

  describe('behaviour', () => {
    beforeEach(async () => configureAndCreate());

    it('should emit back when goBack is called', () => {
      const backSpy = spyOn(component.back, 'emit');

      component.goBack();

      expect(component.selectedTask).toBeNull();
      expect(backSpy).toHaveBeenCalled();
    });

    it('should disable submit when form is pristine', () => {
      expect(component.isSubmitDisabled()).toBeTrue();
    });

    it('should submit question tasks using tc-button flow', () => {
      component.form.patchValue({response: 'yes', comment: 'Done'});
      component.form.markAsDirty();

      component.submitTask();

      expect(taskAssignmentServiceSpy.updateQuestionTask).toHaveBeenCalledWith(1, {
        answer: 'yes',
        abandoned: false,
        candidateNotes: 'Done'
      });
    });

    it('should set error when question-task submission fails', async () => {
      const serverError = {status: 500};
      TestBed.resetTestingModule();
      await configureAndCreate({updateQuestionError: serverError});
      component.form.patchValue({response: 'yes', comment: 'Done'});
      component.form.markAsDirty();

      component.submitTask();

      expect(component.error).toEqual(serverError);
      expect(component.saving).toBeFalse();
    });
  });
});
