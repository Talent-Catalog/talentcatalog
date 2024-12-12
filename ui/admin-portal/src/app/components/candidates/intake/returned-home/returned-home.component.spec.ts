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
import {ReturnedHomeComponent} from "./returned-home.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {ReactiveFormsModule} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";
import {AutosaveStatusComponent} from "../../../util/autosave-status/autosave-status.component";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {CandidateService} from "../../../../services/candidate.service";
import {YesNoUnsure} from "../../../../model/candidate";
import {MockCandidate} from "../../../../MockData/MockCandidate";

describe('ReturnedHomeComponent', () => {
  let component: ReturnedHomeComponent;
  let fixture: ComponentFixture<ReturnedHomeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ReturnedHomeComponent,AutosaveStatusComponent ],
      imports: [HttpClientTestingModule, ReactiveFormsModule, NgSelectModule ],
      providers: [
        { provide: CandidateService }
      ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ReturnedHomeComponent);
    component = fixture.componentInstance;
    component.candidateIntakeData = {
      returnedHome: YesNoUnsure.Yes,
      returnedHomeReason: 'Family visit',
      returnedHomeReasonNo: 'Security concerns'
    };
    component.entity = new MockCandidate();
    fixture.detectChanges();
  });

  it('should initialize the form controls with the correct default values', () => {
    expect(component.form.get('returnedHome').value).toBe(YesNoUnsure.Yes);
    expect(component.form.get('returnedHomeReason').value).toBe('Family visit');
    expect(component.form.get('returnedHomeReasonNo').value).toBe('Security concerns');
  });

  it('should display the returnedHomeReason textarea when returnedHome is Yes', () => {
    component.form.get('returnedHome').setValue('Yes');
    fixture.detectChanges();
    const returnedHomeReasonTextarea = fixture.nativeElement.querySelector('#returnedHomeReason');
    expect(returnedHomeReasonTextarea).toBeTruthy();
  });

  it('should display the returnedHomeReasonNo textarea when returnedHome is No', () => {
    component.form.get('returnedHome').setValue('No');
    fixture.detectChanges();
    const returnedHomeReasonNoTextarea = fixture.nativeElement.querySelector('#returnedHomeReasonNo');
    expect(returnedHomeReasonNoTextarea).toBeTruthy();
  });

  it('should not display the returnedHomeReason textarea when returnedHome is No', () => {
    component.form.get('returnedHome').setValue('No');
    fixture.detectChanges();
    const returnedHomeReasonTextarea = fixture.nativeElement.querySelector('#returnedHomeReason');
    expect(returnedHomeReasonTextarea).toBeFalsy();
  });

  it('should not display the returnedHomeReasonNo textarea when returnedHome is Yes', () => {
    component.form.get('returnedHome').setValue('Yes');
    fixture.detectChanges();
    const returnedHomeReasonNoTextarea = fixture.nativeElement.querySelector('#returnedHomeReasonNo');
    expect(returnedHomeReasonNoTextarea).toBeFalsy();
  });
});
