/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

import {ComponentRef, EventEmitter, SimpleChange} from '@angular/core';

import {Candidate} from '../../../../../../../model/candidate';
import {ICandidateFormComponent} from '../../../../../../../model/candidate-form';
import {TaskAssignment} from '../../../../../../../model/task-assignment';
import {CandidateFormService} from '../../../../../../../services/candidate-form.service';
import {ViewFormTaskComponent} from './view-form-task.component';

class TestCandidateFormComponent
  implements ICandidateFormComponent {
  candidate: Candidate;
  readOnly = false;
  submitted = new EventEmitter<unknown>();
}

describe('ViewFormTaskComponent', () => {
  let component: ViewFormTaskComponent;
  let candidateFormServiceSpy:
    jasmine.SpyObj<CandidateFormService>;
  let viewContainerSpy: any;
  let formComponentRef: ComponentRef<TestCandidateFormComponent>;
  let submitted: EventEmitter<unknown>;

  beforeEach(() => {
    candidateFormServiceSpy =
      jasmine.createSpyObj<CandidateFormService>(
        'CandidateFormService',
        ['getFormComponentByName']
      );

    submitted = new EventEmitter<unknown>();

    formComponentRef = {
      instance: {
        candidate: null,
        readOnly: false,
        submitted
      },
      setInput: jasmine.createSpy('setInput')
    } as unknown as ComponentRef<TestCandidateFormComponent>;

    viewContainerSpy = jasmine.createSpyObj(
      'ViewContainerRef',
      [
        'clear',
        'createComponent'
      ]
    );

    viewContainerSpy.createComponent.and.returnValue(
      formComponentRef
    );

    component = new ViewFormTaskComponent(
      candidateFormServiceSpy
    );

    component.candidate = {
      id: 123
    } as Candidate;

    component.vc = viewContainerSpy;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize with no loaded form', () => {
    expect(component.currentlyLoadedFormName).toBeNull();
    expect(component.currentlyLoadedFormRef).toBeNull();
    expect(component.readOnly).toBeFalse();
    expect(component.error).toBeUndefined();
  });

  it('should update the loaded form when readOnly changes', () => {
    component.currentlyLoadedFormRef = formComponentRef;
    component.readOnly = true;

    component.ngOnChanges({
      readOnly: new SimpleChange(
        false,
        true,
        false
      )
    });

    expect(formComponentRef.setInput)
    .toHaveBeenCalledOnceWith(
      'readOnly',
      true
    );
  });

  it('should safely handle readOnly changes before a form is loaded', () => {
    component.currentlyLoadedFormRef = null;
    component.readOnly = true;

    expect(() => {
      component.ngOnChanges({
        readOnly: new SimpleChange(
          false,
          true,
          false
        )
      });
    }).not.toThrow();
  });

  it('should report an error when the task is not a form task', () => {
    component.taskAssignment = createTaskAssignment({
      taskName: 'ordinaryTask',
      formName: null
    });

    component.ngOnChanges({
      taskAssignment: new SimpleChange(
        null,
        component.taskAssignment,
        true
      )
    });

    expect(component.error).toBe(
      'Angular ViewFormTaskComponent: Task ordinaryTask is not a FormTask.'
    );

    expect(
      candidateFormServiceSpy.getFormComponentByName
    ).not.toHaveBeenCalled();

    expect(viewContainerSpy.createComponent)
    .not.toHaveBeenCalled();
  });

  it('should report an error when no component mapping exists', () => {
    candidateFormServiceSpy.getFormComponentByName
    .and.returnValue(undefined);

    component.taskAssignment = createTaskAssignment({
      taskName: 'missingFormTask',
      formName: 'MissingForm'
    });

    component.ngOnChanges({
      taskAssignment: new SimpleChange(
        null,
        component.taskAssignment,
        true
      )
    });

    expect(
      candidateFormServiceSpy.getFormComponentByName
    ).toHaveBeenCalledOnceWith('MissingForm');

    expect(component.error).toBe(
      'Angular ViewFormTaskComponent: No Component found matching Candidate Form ' +
      'MissingForm, associated with Form Task missingFormTask' +
      '. Add a mapping to the componentMap in CandidateFormService.ts.'
    );

    expect(viewContainerSpy.createComponent)
    .not.toHaveBeenCalled();
  });

  it('should load the component mapped to the form name', () => {
    candidateFormServiceSpy.getFormComponentByName
    .and.returnValue(TestCandidateFormComponent);

    component.taskAssignment = createTaskAssignment({
      taskName: 'myFormTask',
      formName: 'MyFirstForm'
    });

    component.ngOnChanges({
      taskAssignment: new SimpleChange(
        null,
        component.taskAssignment,
        true
      )
    });

    expect(
      candidateFormServiceSpy.getFormComponentByName
    ).toHaveBeenCalledOnceWith('MyFirstForm');

    expect(viewContainerSpy.clear)
    .toHaveBeenCalledTimes(1);

    expect(viewContainerSpy.createComponent)
    .toHaveBeenCalledOnceWith(
      TestCandidateFormComponent
    );

    expect(component.currentlyLoadedFormRef)
    .toBe(formComponentRef);

    expect(component.currentlyLoadedFormName)
    .toBe('MyFirstForm');

    expect(formComponentRef.setInput)
    .toHaveBeenCalledWith(
      'candidate',
      component.candidate
    );

    expect(formComponentRef.setInput)
    .toHaveBeenCalledWith(
      'readOnly',
      false
    );
  });

  it('should not reload a form that is already loaded', () => {
    candidateFormServiceSpy.getFormComponentByName
    .and.returnValue(TestCandidateFormComponent);

    component.currentlyLoadedFormName = 'MyFirstForm';
    component.currentlyLoadedFormRef = formComponentRef;

    component.taskAssignment = createTaskAssignment({
      taskName: 'myFormTask',
      formName: 'MyFirstForm'
    });

    component.ngOnChanges({
      taskAssignment: new SimpleChange(
        null,
        component.taskAssignment,
        false
      )
    });

    expect(
      candidateFormServiceSpy.getFormComponentByName
    ).toHaveBeenCalledOnceWith('MyFirstForm');

    expect(viewContainerSpy.clear)
    .not.toHaveBeenCalled();

    expect(viewContainerSpy.createComponent)
    .not.toHaveBeenCalled();
  });

  it('should set a completed task form to read-only', () => {
    candidateFormServiceSpy.getFormComponentByName
    .and.returnValue(TestCandidateFormComponent);

    component.taskAssignment = createTaskAssignment({
      formName: 'MyFirstForm',
      completedDate: new Date('2026-01-01')
    });

    component.ngOnChanges({
      taskAssignment: new SimpleChange(
        null,
        component.taskAssignment,
        true
      )
    });

    expect(formComponentRef.setInput)
    .toHaveBeenCalledWith(
      'readOnly',
      true
    );
  });

  it('should set an abandoned task form to read-only', () => {
    candidateFormServiceSpy.getFormComponentByName
    .and.returnValue(TestCandidateFormComponent);

    component.taskAssignment = createTaskAssignment({
      formName: 'MyFirstForm',
      abandonedDate: new Date('2026-01-01')
    });

    component.ngOnChanges({
      taskAssignment: new SimpleChange(
        null,
        component.taskAssignment,
        true
      )
    });

    expect(formComponentRef.setInput)
    .toHaveBeenCalledWith(
      'readOnly',
      true
    );
  });

  it('should return the dynamically created component reference', () => {
    component.readOnly = false;

    const result = component.load(
      TestCandidateFormComponent
    );

    expect(result).toBe(formComponentRef);

    expect(viewContainerSpy.clear)
    .toHaveBeenCalledTimes(1);

    expect(viewContainerSpy.createComponent)
    .toHaveBeenCalledOnceWith(
      TestCandidateFormComponent
    );

    expect(formComponentRef.setInput)
    .toHaveBeenCalledWith(
      'candidate',
      component.candidate
    );
  });

  it('should emit taskCompleted when the dynamic form is submitted', () => {
    candidateFormServiceSpy.getFormComponentByName
    .and.returnValue(TestCandidateFormComponent);

    component.taskAssignment = createTaskAssignment({
      formName: 'MyFirstForm'
    });

    const taskCompletedSpy = spyOn(
      component.taskCompleted,
      'emit'
    );

    component.ngOnChanges({
      taskAssignment: new SimpleChange(
        null,
        component.taskAssignment,
        true
      )
    });

    submitted.emit({});

    expect(taskCompletedSpy)
    .toHaveBeenCalledOnceWith(
      component.taskAssignment
    );

    expect(formComponentRef.setInput)
    .toHaveBeenCalledWith(
      'readOnly',
      true
    );
  });

  it('should emit the current assignment when onSubmitted is called', () => {
    component.taskAssignment = createTaskAssignment({
      formName: 'MyFirstForm'
    });

    component.currentlyLoadedFormRef = formComponentRef;

    const taskCompletedSpy = spyOn(
      component.taskCompleted,
      'emit'
    );

    component.onSubmitted();

    expect(taskCompletedSpy)
    .toHaveBeenCalledOnceWith(
      component.taskAssignment
    );

    expect(formComponentRef.setInput)
    .toHaveBeenCalledOnceWith(
      'readOnly',
      true
    );
  });
});

interface TaskAssignmentOptions {
  taskName?: string;
  formName?: string | null;
  completedDate?: Date | null;
  abandonedDate?: Date | null;
}

function createTaskAssignment(
  options: TaskAssignmentOptions = {}
): TaskAssignment {
  const taskName =
    options.taskName ?? 'candidateFormTask';

  const formName =
    options.formName === undefined
      ? 'MyFirstForm'
      : options.formName;

  return {
    id: 1,
    completedDate: options.completedDate ?? null,
    abandonedDate: options.abandonedDate ?? null,
    task: {
      name: taskName,
      candidateForm: formName
        ? {
          name: formName
        }
        : null
    }
  } as TaskAssignment;
}
