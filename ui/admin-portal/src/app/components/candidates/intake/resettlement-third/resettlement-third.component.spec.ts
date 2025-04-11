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
import {ResettlementThirdComponent} from "./resettlement-third.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {NgSelectModule} from "@ng-select/ng-select";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {AutosaveStatusComponent} from "../../../util/autosave-status/autosave-status.component";
import {CandidateService} from "../../../../services/candidate.service";
import {YesNo} from "../../../../model/candidate";

describe('ResettlementThirdComponent', () => {
  let component: ResettlementThirdComponent;
  let fixture: ComponentFixture<ResettlementThirdComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ResettlementThirdComponent,AutosaveStatusComponent ],
      imports: [HttpClientTestingModule, NgSelectModule, FormsModule, ReactiveFormsModule ],
      providers: [
        { provide: CandidateService }
      ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ResettlementThirdComponent);
    component = fixture.componentInstance;
    component.candidateIntakeData = {
      resettleThird: YesNo.Yes,
      resettleThirdStatus: 'Application in process'
    };
    fixture.detectChanges();
  });

  it('should initialize the form controls with the correct default values', () => {
    expect(component.form.get('resettleThird').value).toBe(YesNo.Yes);
    expect(component.form.get('resettleThirdStatus').value).toBe('Application in process');
  });

  it('should display the status textarea when resettleThird is Yes', () => {
    component.form.patchValue({ resettleThird: 'Yes' });
    fixture.detectChanges();
    const statusTextarea = fixture.nativeElement.querySelector('#resettleThirdStatus');
    expect(statusTextarea).toBeTruthy();
  });

  it('should not display the status textarea when resettleThird is No', () => {
    component.form.patchValue({ resettleThird: 'No' });
    fixture.detectChanges();
    const statusTextarea = fixture.nativeElement.querySelector('#resettleThirdStatus');
    expect(statusTextarea).toBeFalsy();
  });
});
