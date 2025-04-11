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
import {IntRecruitmentComponent} from "./int-recruitment.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {AutosaveStatusComponent} from "../../../util/autosave-status/autosave-status.component";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {NgSelectModule} from "@ng-select/ng-select";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {CandidateService} from "../../../../services/candidate.service";
import {CandidateIntakeData, IntRecruitReason} from "../../../../model/candidate";

describe('IntRecruitmentComponent', () => {
  let component: IntRecruitmentComponent;
  let fixture: ComponentFixture<IntRecruitmentComponent>;
  const mockCandidateIntakeData: CandidateIntakeData = {
    intRecruitReasons: [IntRecruitReason.Other],
    intRecruitOther: "Other"
  }
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [IntRecruitmentComponent, AutosaveStatusComponent],
      imports: [HttpClientTestingModule,NgSelectModule,FormsModule,ReactiveFormsModule],
      providers: [
        { provide: CandidateService }
      ]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(IntRecruitmentComponent);
    component = fixture.componentInstance;
    component.candidateIntakeData = mockCandidateIntakeData; // Assign mock candidate data
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form with candidate data', () => {
    expect(component.form.get('intRecruitReasons').value[0]).toEqual({ key: 'Other', stringValue: 'Other' });
    expect(component.form.get('intRecruitOther').value).toEqual(mockCandidateIntakeData.intRecruitOther);
  });

  it('should display other textarea when "Other" option is selected', () => {
    // Simulate selecting "Other" option
    component.form.get('intRecruitReasons').setValue([{ key: 'Other', stringValue: 'Other' }]);
    fixture.detectChanges();

    const otherTextarea = fixture.nativeElement.querySelector('#intRecruitOther');
    expect(otherTextarea).toBeTruthy();
  });

  it('should not display other textarea when "Other" option is not selected', () => {
    // Simulate selecting other options
    component.form.get('intRecruitReasons').setValue([{ key: 'Option1', stringValue: 'Option 1' }]);
    fixture.detectChanges();

    const otherTextarea = fixture.nativeElement.querySelector('#intRecruitOther');
    expect(otherTextarea).toBeFalsy();
  });
});

