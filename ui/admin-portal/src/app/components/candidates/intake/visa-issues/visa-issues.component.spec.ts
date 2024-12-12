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
import {VisaIssuesComponent} from "./visa-issues.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {ReactiveFormsModule} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";
import {AutosaveStatusComponent} from "../../../util/autosave-status/autosave-status.component";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {CandidateService} from "../../../../services/candidate.service";
import {YesNoUnsure} from "../../../../model/candidate";

describe('VisaIssuesComponent', () => {
  let component: VisaIssuesComponent;
  let fixture: ComponentFixture<VisaIssuesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ VisaIssuesComponent,AutosaveStatusComponent ],
      imports: [HttpClientTestingModule, ReactiveFormsModule, NgSelectModule ],
      providers: [
        { provide: CandidateService }
      ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(VisaIssuesComponent);
    component = fixture.componentInstance;
    component.candidateIntakeData = {
      visaIssues: YesNoUnsure.Yes,
      visaIssuesNotes: 'Worked for a foreign government'
    };
    component.editable = true;
    fixture.detectChanges();
  });

  it('should initialize the form controls with the correct default values', () => {
    expect(component.form.get('visaIssues').value).toBe(YesNoUnsure.Yes);
    expect(component.form.get('visaIssuesNotes').value).toBe('Worked for a foreign government');
  });

  it('should enable the form controls when editable is true', () => {
    expect(component.form.get('visaIssues').enabled).toBeTrue();
    expect(component.form.get('visaIssuesNotes').enabled).toBeTrue();
  });

  it('should disable the form controls when editable is false', () => {
    component.editable = false;
    component.ngOnInit();
    fixture.detectChanges();

    expect(component.form.get('visaIssues').disabled).toBeTrue();
    expect(component.form.get('visaIssuesNotes').disabled).toBeTrue();
  });

  it('should return true for hasNotes when visaIssues is Yes, No, or Unsure', () => {
    component.form.get('visaIssues').setValue('Yes');
    expect(component.hasNotes).toBeTrue();

    component.form.get('visaIssues').setValue('No');
    expect(component.hasNotes).toBeTrue();

    component.form.get('visaIssues').setValue('Unsure');
    expect(component.hasNotes).toBeTrue();
  });

  it('should return false for hasNotes when visaIssues is null or an empty string', () => {
    component.form.get('visaIssues').setValue(null);
    expect(component.hasNotes).toBeFalse();

    component.form.get('visaIssues').setValue('');
    expect(component.hasNotes).toBeFalse();
  });
});
