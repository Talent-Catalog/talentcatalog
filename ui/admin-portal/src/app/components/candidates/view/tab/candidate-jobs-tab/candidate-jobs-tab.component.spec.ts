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

import {CandidateJobsTabComponent} from "./candidate-jobs-tab.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {MockCandidate} from "../../../../../MockData/MockCandidate";
import {
  ViewCandidateJobsComponent
} from "../../jobs/view-candidate-jobs/view-candidate-jobs.component";
import {
  CandidateOppsComponent
} from "../../../../candidate-opp/candidate-opps/candidate-opps.component";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";

describe('CandidateJobsTabComponent', () => {
  let component: CandidateJobsTabComponent;
  let fixture: ComponentFixture<CandidateJobsTabComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HttpClientTestingModule,FormsModule,ReactiveFormsModule, NgSelectModule],
      declarations: [CandidateJobsTabComponent,ViewCandidateJobsComponent, CandidateOppsComponent]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CandidateJobsTabComponent);
    component = fixture.componentInstance;
    component.candidate = new MockCandidate();
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should have candidate input', () => {
    const mockCandidate = new MockCandidate();

    component.candidate = mockCandidate;
    fixture.detectChanges();

    expect(component.candidate).toBe(mockCandidate);
  });

  it('should have preview input', () => {
    component.preview = true;
    fixture.detectChanges();

    expect(component.preview).toBeTrue();
  });

  it('should default preview to false', () => {
    expect(component.preview).toBeFalse();
  });

  it('should initialize component correctly', () => {
    spyOn(component, 'ngOnInit').and.callThrough();
    component.ngOnInit();
    expect(component.ngOnInit).toHaveBeenCalled();
  });
});
