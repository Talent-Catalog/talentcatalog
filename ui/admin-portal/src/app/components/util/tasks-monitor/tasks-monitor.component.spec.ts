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

import {TasksMonitorComponent} from "./tasks-monitor.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {TaskAssignment} from "../../../model/task-assignment";
import {MockCandidate} from "../../../MockData/MockCandidate";
import {Status} from "../../../model/base";
import {TaskType, UploadType} from "../../../model/task";

describe('TasksMonitorComponent', () => {
  let component: TasksMonitorComponent;
  let fixture: ComponentFixture<TasksMonitorComponent>;
  const candidate = new MockCandidate();
  const totalTasks: TaskAssignment[] = candidate.taskAssignments;
  const completedTasks: TaskAssignment[] = candidate.taskAssignments;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [TasksMonitorComponent]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TasksMonitorComponent);
    component = fixture.componentInstance;
    component.totalTasks = totalTasks;
    component.completedTasks = completedTasks;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should set hasOverdue to true if there are overdue tasks', () => {
    component.totalTasks = totalTasks;
    component.ngOnInit();
    expect(component.hasOverdue).toBe(true);
  });

  it('should set hasAbandoned to true if there are abandoned tasks', () => {
    const abandonedTasks: TaskAssignment[] = [
      {
        id: 1,
        abandonedDate: new Date(), // Abandoned date is set
        candidateNotes: '',
        completedDate: null,
        dueDate: new Date('2099-06-25'),
        status: Status.active,
        task: {
          id: 1,
          name: 'Submit CV',
          daysToComplete: 7,
          description: 'Submit your latest CV.',
          displayName: 'CV Submission',
          optional: false,
          helpLink: 'http://example.com/cv-help',
          taskType: TaskType.Upload,
          uploadType: UploadType.cv,
          uploadSubfolderName: 'cvs',
          uploadableFileTypes: 'pdf,doc,docx',
          candidateAnswerField: 'N/A'
        },
        answer: 'Sample Answer'
      }
    ];
    component.totalTasks = abandonedTasks;

    component.ngOnInit();
    expect(component.hasAbandoned).toBe(true);
  });

  it('should set hasCompleted to true if all tasks are completed and none are abandoned', () => {
    const completedTasks: TaskAssignment[] = totalTasks;
    component.completedTasks = completedTasks;
    component.totalTasks = totalTasks;
    component.ngOnInit();
    expect(component.hasCompleted).toBe(true);
  });

  it('should not display tasks monitor if hasCompleted is true', () => {
    const completedTasks: TaskAssignment[] = totalTasks;
    component.completedTasks = completedTasks;
    component.totalTasks = totalTasks;
    component.ngOnInit();
    fixture.detectChanges();
    const tasksMonitorElement: HTMLElement = fixture.nativeElement;
    const tasksMonitorDiv = tasksMonitorElement.querySelector('.d-inline-block');
    expect(tasksMonitorDiv).toBeNull();
  });

  it('should display tasks monitor if hasCompleted is false', () => {
    const tasks: TaskAssignment[] = [
      {
        id: 1,
        abandonedDate: new Date(), // Abandoned date is set
        candidateNotes: '',
        completedDate: null,
        dueDate: new Date('2099-06-25'),
        status: Status.active,
        task: {
          id: 1,
          name: 'Submit CV',
          daysToComplete: 7,
          description: 'Submit your latest CV.',
          displayName: 'CV Submission',
          optional: false,
          helpLink: 'http://example.com/cv-help',
          taskType: TaskType.Upload,
          uploadType: UploadType.cv,
          uploadSubfolderName: 'cvs',
          uploadableFileTypes: 'pdf,doc,docx',
          candidateAnswerField: 'N/A'
        },
        answer: 'Sample Answer'
      }
    ];
    component.completedTasks = tasks;
    component.totalTasks = totalTasks;
    component.ngOnInit();
    fixture.detectChanges();
    const tasksMonitorElement: HTMLElement = fixture.nativeElement;
    const tasksMonitorDiv = tasksMonitorElement.querySelector('.d-inline-block');
    expect(tasksMonitorDiv).not.toBeNull();
  });
});
