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

import {Component, Input} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {By} from '@angular/platform-browser';

import {MockCandidate} from '../../../../../MockData/MockCandidate';
import {Candidate} from '../../../../../model/candidate';
import {CandidateTaskTabComponent} from './candidate-task-tab.component';

@Component({
  selector: 'app-view-candidate-tasks',
  template: ''
})
class MockViewCandidateTasksComponent {
  @Input() candidate: Candidate;
  @Input() editable: boolean;
}

describe('CandidateTaskTabComponent', () => {
  let component: CandidateTaskTabComponent;
  let fixture: ComponentFixture<CandidateTaskTabComponent>;
  let mockCandidate: Candidate;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [
        CandidateTaskTabComponent,
        MockViewCandidateTasksComponent
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(CandidateTaskTabComponent);
    component = fixture.componentInstance;
    mockCandidate = new MockCandidate();
  });

  it('should create', () => {
    component.candidate = mockCandidate;

    fixture.detectChanges();

    expect(component).toBeTruthy();
  });

  it('should initialize without changing its inputs', () => {
    component.candidate = mockCandidate;
    component.editable = true;

    component.ngOnInit();

    expect(component.candidate).toBe(mockCandidate);
    expect(component.editable).toBeTrue();
  });

  it('should pass the candidate and editable=true to the task view', () => {
    component.candidate = mockCandidate;
    component.editable = true;

    fixture.detectChanges();

    const taskView = getTaskView();
    expect(taskView.candidate).toBe(mockCandidate);
    expect(taskView.editable).toBeTrue();
  });

  it('should pass editable=false to the task view', () => {
    component.candidate = mockCandidate;
    component.editable = false;

    fixture.detectChanges();

    const taskView = getTaskView();
    expect(taskView.candidate).toBe(mockCandidate);
    expect(taskView.editable).toBeFalse();
  });

  it('should display tasks correctly based on candidate data', () => {
    const candidateData = mockCandidate;
    component.candidate = candidateData;
    component.editable = true;
    fixture.detectChanges();

    const mockViewCandidateTasksComponent = fixture.debugElement.children[0].componentInstance;
    expect(mockViewCandidateTasksComponent.candidate).toBe(candidateData);
    expect(mockViewCandidateTasksComponent.editable).toBeTrue();
  });


  function getTaskView(): MockViewCandidateTasksComponent {
    return fixture.debugElement
    .query(By.directive(MockViewCandidateTasksComponent))
      .componentInstance;
  }
});
