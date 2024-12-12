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

import {LeftHomeReasonComponent} from "./left-home-reason.component";
import {CandidateService} from "../../../../services/candidate.service";
import {ComponentFixture, TestBed, waitForAsync} from "@angular/core/testing";
import {UntypedFormBuilder, FormsModule, ReactiveFormsModule} from "@angular/forms";
import {AutosaveStatusComponent} from "../../../util/autosave-status/autosave-status.component";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {NgSelectModule} from "@ng-select/ng-select";
import {MockCandidate} from "../../../../MockData/MockCandidate";
import {CandidateIntakeData, IntRecruitReason, LeftHomeReason} from "../../../../model/candidate";

describe('LeftHomeReasonComponent', () => {
  let component: LeftHomeReasonComponent;
  let fixture: ComponentFixture<LeftHomeReasonComponent>;
  const mockCandidateIntakeData: CandidateIntakeData = {
    leftHomeReasons: [LeftHomeReason.Job],
    leftHomeNotes: 'Notes'
  };
  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ LeftHomeReasonComponent, AutosaveStatusComponent ],
      imports: [HttpClientTestingModule,NgSelectModule,FormsModule,ReactiveFormsModule],

      providers: [
        UntypedFormBuilder,
        { provide: CandidateService }
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(LeftHomeReasonComponent);
    component = fixture.componentInstance;
    component.entity = new MockCandidate();
    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize the form with provided data', () => {
    component.candidateIntakeData = mockCandidateIntakeData;
    component.ngOnInit();
    expect(component.form.value).toEqual({
      leftHomeReasons: [{ key: 'Job Opportunities', stringValue: undefined }], // Adjusted to match the enum value
      leftHomeNotes: 'Notes'
    });
  });

  it('should handle "Other" reason option properly', () => {
    component.form.get('leftHomeReasons').setValue([{ key: 'Other', stringValue: 'Other' }]);
    fixture.detectChanges();
    expect(component.hasOther).toBeTruthy();
    expect(component.form.get('leftHomeNotes').enabled).toBe(true);
  });
});
