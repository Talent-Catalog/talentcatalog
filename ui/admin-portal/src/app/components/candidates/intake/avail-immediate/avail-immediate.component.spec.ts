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

import {AvailImmediateComponent} from "./avail-immediate.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {FormsModule, ReactiveFormsModule, UntypedFormBuilder} from "@angular/forms";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {NgSelectModule} from "@ng-select/ng-select";
import {CandidateService} from "../../../../services/candidate.service";
import {AvailImmediateReason, YesNo} from "../../../../model/candidate";
import {AutosaveStatusComponent} from "../../../util/autosave-status/autosave-status.component";
import {MockCandidate} from "../../../../MockData/MockCandidate";

describe('AvailImmediateComponent', () => {
  let component: AvailImmediateComponent;
  let fixture: ComponentFixture<AvailImmediateComponent>;
  let fb: UntypedFormBuilder;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [AvailImmediateComponent, AutosaveStatusComponent],
      imports: [HttpClientTestingModule, NgSelectModule,FormsModule,ReactiveFormsModule],
      providers: [
        UntypedFormBuilder,
        { provide: CandidateService }
      ]
    }).compileComponents();

    fb = TestBed.inject(UntypedFormBuilder);
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AvailImmediateComponent);
    component = fixture.componentInstance;
    component.entity= new MockCandidate();
    component.candidateIntakeData = {
      availImmediate: YesNo.Yes,
      availImmediateJobOps: 'Software Development',
      availImmediateReason: AvailImmediateReason.Health,
      availImmediateNotes: 'Some additional notes'
    };
    fixture.detectChanges(); // Trigger initial data binding
  });

  it('should initialize the form correctly with the provided data', () => {
    expect(component.form).toBeTruthy();
    const availImmediateControl = component.form.get('availImmediate');
    const availImmediateJobOpsControl = component.form.get('availImmediateJobOps');
    const availImmediateReasonControl = component.form.get('availImmediateReason');
    const availImmediateNotesControl = component.form.get('availImmediateNotes');

    expect(availImmediateControl.value).toBe(YesNo.Yes);
    expect(availImmediateJobOpsControl.value).toBe('Software Development');
    expect(availImmediateReasonControl.value).toBe(AvailImmediateReason.Health);
    expect(availImmediateNotesControl.value).toBe('Some additional notes');
  });
});
