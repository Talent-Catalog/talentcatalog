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
import {TranslateModule} from '@ngx-translate/core';

import {CandidateTasksComponent} from './candidate-tasks.component';
import {Candidate} from '../../../../../model/candidate';
import {Status} from '../../../../../model/base';
import {TaskAssignment} from '../../../../../model/task-assignment';
import {TaskType} from '../../../../../model/task';

@Component({selector: 'app-error', template: ''})
class ErrorStubComponent {
  @Input() error?: unknown;
}

@Component({selector: 'tc-loading', template: ''})
class TcLoadingStubComponent {
  @Input() loading?: boolean;
}

@Component({selector: 'app-tab-header', template: '<ng-content></ng-content>'})
class TabHeaderStubComponent {}

@Component({selector: 'tc-button', template: '<ng-content></ng-content>'})
class TcButtonStubComponent {
  @Input() size?: string;
  @Input() color?: string;
  @Input() href?: string;
  @Input() target?: string;
}

@Component({selector: 'tc-table', template: '<ng-content></ng-content>'})
class TcTableStubComponent {
  @Input() results?: unknown;
  @Input() loading?: boolean;
  @Input() striped?: boolean;
}

@Component({selector: 'app-candidate-task', template: ''})
class CandidateTaskStubComponent {
  @Input() selectedTask?: TaskAssignment;
  @Input() candidate?: Candidate;
  @Output() back = new EventEmitter<void>();
}

function makeTaskAssignment(id: number, overrides: Partial<TaskAssignment> = {}): TaskAssignment {
  return {
    id,
    abandonedDate: null,
    candidateNotes: '',
    completedDate: null,
    dueDate: new Date('2099-01-01'),
    status: Status.active,
    answer: '',
    task: {
      id,
      name: `task-${id}`,
      description: 'Task description',
      displayName: `Task ${id}`,
      optional: false,
      docLink: null,
      taskType: TaskType.Simple,
      daysToComplete: 5,
      uploadType: null,
      uploadSubfolderName: '',
      uploadableFileTypes: '',
      candidateAnswerField: ''
    } as any,
    ...overrides
  };
}

function makeCandidate(taskAssignments: TaskAssignment[]): Candidate {
  return {
    id: 1,
    taskAssignments
  } as Candidate;
}

describe('CandidateTasksComponent', () => {
  let component: CandidateTasksComponent;
  let fixture: ComponentFixture<CandidateTasksComponent>;

  async function configureAndCreate(options?: {
    candidate?: Candidate;
    loading?: boolean;
    selectedTask?: TaskAssignment | null;
  }) {
    await TestBed.configureTestingModule({
      declarations: [
        CandidateTasksComponent,
        ErrorStubComponent,
        TcLoadingStubComponent,
        TabHeaderStubComponent,
        TcButtonStubComponent,
        TcTableStubComponent,
        CandidateTaskStubComponent
      ],
      imports: [TranslateModule.forRoot()]
    }).compileComponents();

    fixture = TestBed.createComponent(CandidateTasksComponent);
    component = fixture.componentInstance;
    component.candidate = options?.candidate ?? makeCandidate([
      makeTaskAssignment(1, {dueDate: new Date('2099-01-02')}),
      makeTaskAssignment(2, {completedDate: new Date('2024-01-01')})
    ]);
    component.loading = options?.loading ?? false;
    component.selectedTask = options?.selectedTask ?? null;

    fixture.detectChanges();
  }

  afterEach(() => TestBed.resetTestingModule());

  it('should create', async () => {
    await configureAndCreate();
    expect(component).toBeTruthy();
  });

  describe('template tc components', () => {
    it('should render tc-loading, the help tc-button, and tc-table wrappers', async () => {
      await configureAndCreate();

      const loading = fixture.debugElement.query(By.directive(TcLoadingStubComponent));
      const button = fixture.debugElement.query(By.directive(TcButtonStubComponent));
      const tables = fixture.debugElement.queryAll(By.directive(TcTableStubComponent));

      expect(loading.componentInstance.loading).toBeFalse();
      expect(button.componentInstance.size).toBe('sm');
      expect(button.componentInstance.color).toBe('white');
      expect(button.componentInstance.href).toContain('/tasks/overview');
      expect(button.componentInstance.target).toBe('_blank');
      expect(tables.length).toBe(2);
      expect(tables[0].componentInstance.striped).toBeTrue();
      expect(tables[1].componentInstance.striped).toBeTrue();
    });

    it('should render the selected task view when a task is selected', async () => {
      const selectedTask = makeTaskAssignment(5);
      await configureAndCreate({selectedTask});

      const candidateTask = fixture.debugElement.query(By.directive(CandidateTaskStubComponent));
      expect(candidateTask).toBeTruthy();
      expect(candidateTask.componentInstance.selectedTask).toBe(selectedTask);
    });
  });

  describe('behaviour', () => {
    beforeEach(async () => configureAndCreate());

    it('should split ongoing and completed tasks', () => {
      expect(component.ongoingTasks.length).toBe(1);
      expect(component.completedOrAbandonedTasks.length).toBe(1);
    });

    it('should mark overdue required tasks as overdue', () => {
      const overdue = makeTaskAssignment(6, {
        dueDate: new Date('2000-01-01'),
        task: {...makeTaskAssignment(6).task, optional: false} as any
      });

      expect(component.isOverdue(overdue)).toBeTrue();
    });

    it('should select and unselect a task', () => {
      const refreshSpy = spyOn(component.refresh, 'emit');
      const task = component.ongoingTasks[0];

      component.selectTask(task);
      expect(component.selectedTask).toBe(task);

      component.unSelectTask();
      expect(component.selectedTask).toBeNull();
      expect(refreshSpy).toHaveBeenCalled();
    });
  });
});
