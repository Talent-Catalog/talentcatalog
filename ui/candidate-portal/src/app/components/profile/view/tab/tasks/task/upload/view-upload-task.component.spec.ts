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

import {Component, EventEmitter, Input, Output} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {By} from '@angular/platform-browser';
import {FormGroup, ReactiveFormsModule} from '@angular/forms';
import {TranslateModule} from '@ngx-translate/core';
import {of, throwError} from 'rxjs';

import {ViewUploadTaskComponent} from './view-upload-task.component';
import {TaskAssignmentService} from '../../../../../../../services/task-assignment.service';
import {TaskAssignment} from '../../../../../../../model/task-assignment';
import {TaskType} from '../../../../../../../model/task';
import {Status} from '../../../../../../../model/base';

@Component({selector: 'app-error', template: ''})
class ErrorStubComponent {
  @Input() error?: unknown;
}

@Component({selector: 'app-file-upload', template: ''})
class FileUploadStubComponent {
  @Input() uploading?: boolean;
  @Output() uploadStarted = new EventEmitter<{files: File[]; type: string}>();
}

@Component({selector: 'tc-button', template: '<ng-content></ng-content>'})
class TcButtonStubComponent {
  @Input() size?: string;
  @Input() type?: string;
  @Input() color?: string;
  @Input() routerLink?: unknown;
}

function makeTaskAssignment(overrides: Partial<TaskAssignment> = {}): TaskAssignment {
  return {
    id: 1,
    abandonedDate: null,
    candidateNotes: '',
    completedDate: null,
    dueDate: new Date('2099-01-01'),
    status: Status.active,
    answer: '',
    task: {
      id: 1,
      name: 'upload',
      description: 'Upload documents',
      displayName: 'Upload task',
      optional: false,
      docLink: null,
      taskType: TaskType.Upload,
      daysToComplete: 1,
      uploadType: null,
      uploadSubfolderName: '',
      uploadableFileTypes: '',
      candidateAnswerField: ''
    } as any,
    ...overrides
  };
}

describe('ViewUploadTaskComponent', () => {
  let component: ViewUploadTaskComponent;
  let fixture: ComponentFixture<ViewUploadTaskComponent>;
  let taskAssignmentServiceSpy: jasmine.SpyObj<TaskAssignmentService>;

  async function configureAndCreate(options?: {
    selectedTask?: TaskAssignment;
    uploadError?: unknown;
  }) {
    taskAssignmentServiceSpy = jasmine.createSpyObj('TaskAssignmentService', ['doUploadTask']);
    if (options?.uploadError) {
      taskAssignmentServiceSpy.doUploadTask.and.returnValue(throwError(options.uploadError));
    } else {
      taskAssignmentServiceSpy.doUploadTask.and.returnValue(of(makeTaskAssignment({
        completedDate: new Date('2024-01-01')
      })));
    }

    await TestBed.configureTestingModule({
      declarations: [
        ViewUploadTaskComponent,
        ErrorStubComponent,
        FileUploadStubComponent,
        TcButtonStubComponent
      ],
      imports: [ReactiveFormsModule, TranslateModule.forRoot()],
      providers: [
        {provide: TaskAssignmentService, useValue: taskAssignmentServiceSpy}
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ViewUploadTaskComponent);
    component = fixture.componentInstance;
    component.form = new FormGroup({});
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
    it('should render the upload file child and the view tc-button', async () => {
      await configureAndCreate();

      const fileUpload = fixture.debugElement.query(By.directive(FileUploadStubComponent));
      const button = fixture.debugElement.query(By.directive(TcButtonStubComponent));

      expect(fileUpload).toBeTruthy();
      expect(button.componentInstance.size).toBe('sm');
      expect(button.componentInstance.type).toBe('outline');
      expect(button.componentInstance.routerLink).toEqual(['edit', 'upload']);
    });
  });

  describe('completeUploadTask', () => {
    beforeEach(async () => configureAndCreate());

    it('should upload files and emit successfulUpload on success', () => {
      const successfulUploadSpy = spyOn(component.successfulUpload, 'emit');
      const file = new File(['x'], 'resume.pdf', {type: 'application/pdf'});

      component.completeUploadTask({files: [file]});

      expect(taskAssignmentServiceSpy.doUploadTask).toHaveBeenCalled();
      expect(successfulUploadSpy).toHaveBeenCalled();
      expect(component.filesUploaded).toEqual([file]);
      expect(component.uploading).toBeFalse();
    });

    it('should set error on upload failure', async () => {
      const serverError = {status: 500};
      TestBed.resetTestingModule();
      await configureAndCreate({uploadError: serverError});
      const file = new File(['x'], 'resume.pdf', {type: 'application/pdf'});

      component.completeUploadTask({files: [file]});

      expect(component.error).toEqual(serverError);
      expect(component.uploading).toBeFalse();
    });
  });
});
