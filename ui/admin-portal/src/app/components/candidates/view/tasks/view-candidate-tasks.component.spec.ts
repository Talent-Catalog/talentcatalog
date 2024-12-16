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

import {ComponentFixture, TestBed} from '@angular/core/testing';
import {ViewCandidateTasksComponent} from './view-candidate-tasks.component';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {CandidateService} from '../../../../services/candidate.service';
import {TaskAssignmentService} from '../../../../services/task-assignment.service';
import {CandidateAttachmentService} from '../../../../services/candidate-attachment.service';
import {of} from 'rxjs';
import {NO_ERRORS_SCHEMA} from '@angular/core';
import {MockCandidate} from "../../../../MockData/MockCandidate";

describe('ViewCandidateTasksComponent', () => {
  let component: ViewCandidateTasksComponent;
  let fixture: ComponentFixture<ViewCandidateTasksComponent>;
  let candidateService: jasmine.SpyObj<CandidateService>;
  let taskAssignmentService: jasmine.SpyObj<TaskAssignmentService>;
  let candidateAttachmentService: jasmine.SpyObj<CandidateAttachmentService>;

  const candidate  = new MockCandidate();

  beforeEach(async () => {
    const candidateServiceSpy = jasmine.createSpyObj('CandidateService', ['get']);
    const taskAssignmentServiceSpy = jasmine.createSpyObj('TaskAssignmentService', ['removeTaskAssignment']);
    const candidateAttachmentServiceSpy = jasmine.createSpyObj('CandidateAttachmentService', ['listByType']);

    await TestBed.configureTestingModule({
      declarations: [ViewCandidateTasksComponent],
      providers: [
        {provide: CandidateService, useValue: candidateServiceSpy},
        {provide: TaskAssignmentService, useValue: taskAssignmentServiceSpy},
        {provide: CandidateAttachmentService, useValue: candidateAttachmentServiceSpy},
        NgbModal
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    candidateService = TestBed.inject(CandidateService) as jasmine.SpyObj<CandidateService>;
    taskAssignmentService = TestBed.inject(TaskAssignmentService) as jasmine.SpyObj<TaskAssignmentService>;
    candidateAttachmentService = TestBed.inject(CandidateAttachmentService) as jasmine.SpyObj<CandidateAttachmentService>;
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ViewCandidateTasksComponent);
    component = fixture.componentInstance;
    component.candidate = candidate;
    component.ngOnInit();
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should highlight overdue tasks correctly', () => {
    component.today = new Date();
    fixture.detectChanges();
    const overdueTask1 = component.isOverdue(candidate.taskAssignments[0]);
    const overdueTask2 = component.isOverdue(candidate.taskAssignments[1]);
    const notOverdueTask = component.isOverdue(candidate.taskAssignments[2]);

    expect(overdueTask1).toBeTrue();
    expect(overdueTask2).toBeFalse();
    expect(notOverdueTask).toBeFalse();
    candidateService.get.and.returnValue(of(candidate));
    // Trigger ngOnChanges manually
    component.ngOnChanges({  candidate: {
        currentValue: component.candidate,
        previousValue: null,
        firstChange: true,
        isFirstChange: () => true
      }} );
    fixture.detectChanges();
    const taskElements = fixture.nativeElement.querySelectorAll('.set-height p');
    expect(taskElements[0].classList).toContain('text-danger');
    expect(taskElements[0].classList).toContain('fw-bolder');
    expect(taskElements[1].classList).not.toContain('text-danger');
    expect(taskElements[1].classList).not.toContain('fw-bolder');
    expect(taskElements[2].classList).not.toContain('text-danger');
    expect(taskElements[2].classList).not.toContain('fw-bolder');
  });
});
