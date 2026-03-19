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

import {Component, Input, forwardRef} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {By} from '@angular/platform-browser';
import {ControlValueAccessor, FormControl, FormGroup, NG_VALUE_ACCESSOR, ReactiveFormsModule} from '@angular/forms';
import {TranslateModule} from '@ngx-translate/core';

import {ViewQuestionTaskComponent} from './view-question-task.component';
import {TaskAssignment} from '../../../../../../../model/task-assignment';
import {TaskType} from '../../../../../../../model/task';
import {Status} from '../../../../../../../model/base';

@Component({selector: 'tc-label', template: '<ng-content></ng-content>'})
class TcLabelStubComponent {
  @Input() for?: string;
}

@Component({
  selector: 'ng-select',
  template: '',
  providers: [{
    provide: NG_VALUE_ACCESSOR,
    useExisting: forwardRef(() => NgSelectStubComponent),
    multi: true
  }]
})
class NgSelectStubComponent implements ControlValueAccessor {
  @Input() id?: string;
  @Input() items?: unknown[];
  @Input() clearable?: boolean;
  @Input() bindLabel?: string;
  @Input() bindValue?: string;
  @Input() formControlName?: string;
  writeValue(): void {}
  registerOnChange(): void {}
  registerOnTouched(): void {}
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

function makeTaskAssignment(overrides: Partial<TaskAssignment> = {}): TaskAssignment {
  return {
    id: 1,
    abandonedDate: null,
    candidateNotes: '',
    completedDate: null,
    dueDate: new Date('2099-01-01'),
    status: Status.active,
    answer: 'Saved answer',
    task: {
      id: 1,
      name: 'question',
      description: 'Question prompt',
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
        {name: 'yes', displayName: 'Yes'},
        {name: 'no', displayName: 'No'}
      ]
    } as any,
    ...overrides
  };
}

describe('ViewQuestionTaskComponent', () => {
  let component: ViewQuestionTaskComponent;
  let fixture: ComponentFixture<ViewQuestionTaskComponent>;

  async function configureAndCreate(options?: {
    selectedTask?: TaskAssignment;
    form?: FormGroup;
  }) {
    await TestBed.configureTestingModule({
      declarations: [
        ViewQuestionTaskComponent,
        TcLabelStubComponent,
        NgSelectStubComponent,
        TcTextareaStubComponent
      ],
      imports: [ReactiveFormsModule, TranslateModule.forRoot()]
    }).compileComponents();

    fixture = TestBed.createComponent(ViewQuestionTaskComponent);
    component = fixture.componentInstance;
    component.selectedTask = options?.selectedTask ?? makeTaskAssignment();
    component.form = options?.form ?? new FormGroup({
      response: new FormControl('yes')
    });

    fixture.detectChanges();
  }

  afterEach(() => TestBed.resetTestingModule());

  it('should create', async () => {
    await configureAndCreate();
    expect(component).toBeTruthy();
  });

  describe('template tc components', () => {
    it('should render tc-label and ng-select.tc-select when allowed answers exist', async () => {
      await configureAndCreate();

      const label = fixture.debugElement.query(By.directive(TcLabelStubComponent));
      const select = fixture.debugElement.query(By.directive(NgSelectStubComponent));

      expect(label.componentInstance.for).toBe('responseDropdown');
      expect(select.componentInstance.id).toBe('responseDropdown');
      expect(select.nativeElement.classList).toContain('tc-select');
    });

    it('should render tc-label and tc-textarea when there are no allowed answers', async () => {
      await configureAndCreate({
        selectedTask: makeTaskAssignment({
          task: {
            ...makeTaskAssignment().task,
            allowedAnswers: []
          } as any
        })
      });

      const label = fixture.debugElement.query(By.directive(TcLabelStubComponent));
      const textarea = fixture.debugElement.query(By.directive(TcTextareaStubComponent));

      expect(label.componentInstance.for).toBe('responseAnswer');
      expect(textarea.componentInstance.id).toBe('responseAnswer');
    });

    it('should render the saved answer in read-only mode when task is completed', async () => {
      await configureAndCreate({
        selectedTask: makeTaskAssignment({
          completedDate: new Date('2024-01-01')
        })
      });

      const text = (fixture.nativeElement as HTMLElement).textContent || '';
      expect(text).toContain('Saved answer');
    });
  });
});
