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

import {Component, Input} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {By} from '@angular/platform-browser';
import {FormControl, FormGroup, ReactiveFormsModule} from '@angular/forms';
import {TranslateModule} from '@ngx-translate/core';

import {ViewSimpleTaskComponent} from './view-simple-task.component';
import {TaskAssignment} from '../../../../../../../model/task-assignment';
import {TaskType} from '../../../../../../../model/task';

@Component({selector: 'tc-label', template: '<ng-content></ng-content>'})
class TcLabelStubComponent {
  @Input() for?: string;
}

@Component({selector: 'tc-description', template: '<ng-content></ng-content>'})
class TcDescriptionStubComponent {}

function makeTaskAssignment(overrides: Partial<TaskAssignment> = {}): TaskAssignment {
  return {
    id: 1,
    task: {
      id: 1,
      taskType: TaskType.Simple,
      docLink: null
    },
    ...overrides
  } as TaskAssignment;
}

describe('ViewSimpleTaskComponent', () => {
  let component: ViewSimpleTaskComponent;
  let fixture: ComponentFixture<ViewSimpleTaskComponent>;

  async function configureAndCreate(options?: {
    selectedTask?: TaskAssignment;
  }) {
    await TestBed.configureTestingModule({
      declarations: [
        ViewSimpleTaskComponent,
        TcLabelStubComponent,
        TcDescriptionStubComponent
      ],
      imports: [ReactiveFormsModule, TranslateModule.forRoot()]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ViewSimpleTaskComponent);
    component = fixture.componentInstance;
    component.form = new FormGroup({
      completed: new FormControl(false)
    });
    component.selectedTask = options?.selectedTask ?? makeTaskAssignment();
    fixture.detectChanges();
  }

  afterEach(() => TestBed.resetTestingModule());

  it('should create', async () => {
    await configureAndCreate();

    expect(component).toBeTruthy();
  });

  describe('template tc components', () => {
    beforeEach(async () => configureAndCreate());

    it('should render the migrated tc-label, checkbox, and tc-description', () => {
      const label = fixture.debugElement.query(By.directive(TcLabelStubComponent));
      const description = fixture.debugElement.query(By.directive(TcDescriptionStubComponent));
      const checkbox = fixture.nativeElement.querySelector('input#completed') as HTMLInputElement;

      expect(label.componentInstance.for).toBe('completed');
      expect(description).toBeTruthy();
      expect(checkbox).toBeTruthy();
      expect(checkbox.type).toBe('checkbox');
    });
  });

  describe('ngOnInit', () => {
    it('should set hasDoc to false when there is no doc link', async () => {
      await configureAndCreate();

      expect(component.hasDoc).toBeFalse();
    });

    it('should set hasDoc to true when the task has a doc link', async () => {
      await configureAndCreate({
        selectedTask: makeTaskAssignment({
          task: {
            ...makeTaskAssignment().task,
            docLink: 'https://example.com/task.pdf'
          } as any
        })
      });

      expect(component.hasDoc).toBeTrue();
    });
  });
});
