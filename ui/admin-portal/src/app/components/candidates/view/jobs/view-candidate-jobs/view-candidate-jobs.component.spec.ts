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

import {HttpClientTestingModule} from "@angular/common/http/testing";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {ViewCandidateJobsComponent} from "./view-candidate-jobs.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {MockCandidate} from "../../../../../MockData/MockCandidate";
import {
  CandidateOppsComponent
} from "../../../../candidate-opp/candidate-opps/candidate-opps.component";

describe('ViewCandidateJobsComponent', () => {
  let component: ViewCandidateJobsComponent;
  let fixture: ComponentFixture<ViewCandidateJobsComponent>;

  const mockCandidate = new MockCandidate();
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HttpClientTestingModule,FormsModule,ReactiveFormsModule],
      declarations: [ ViewCandidateJobsComponent, CandidateOppsComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {

    fixture = TestBed.createComponent(ViewCandidateJobsComponent);
    component = fixture.componentInstance;
    component.candidate = mockCandidate;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize with provided inputs', () => {
    const preview = true;
    component.candidate = mockCandidate;
    component.preview = preview;
    fixture.detectChanges();

    expect(component.candidate).toBe(mockCandidate);
    expect(component.preview).toBe(preview);
  });
});
