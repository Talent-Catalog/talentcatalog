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
import {ReactiveFormsModule} from '@angular/forms';
import {NgSelectModule} from '@ng-select/ng-select';
import {VisaRejectComponent} from './visa-reject.component';
import {CandidateService} from '../../../../services/candidate.service';
import {YesNoUnsure} from '../../../../model/candidate';
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {AutosaveStatusComponent} from "../../../util/autosave-status/autosave-status.component";

describe('VisaRejectComponent', () => {
  let component: VisaRejectComponent;
  let fixture: ComponentFixture<VisaRejectComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ VisaRejectComponent, AutosaveStatusComponent ],
      imports: [HttpClientTestingModule, ReactiveFormsModule, NgSelectModule ],
      providers: [
        { provide: CandidateService }
      ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(VisaRejectComponent);
    component = fixture.componentInstance;
    component.candidateIntakeData = {
      visaReject: YesNoUnsure.Yes,
      visaRejectNotes: 'Rejected due to incomplete documentation'
    };
    component.editable = true;
    fixture.detectChanges();
  });

  it('should initialize the form controls with the correct default values', () => {
    expect(component.form.get('visaReject').value).toBe(YesNoUnsure.Yes);
    expect(component.form.get('visaRejectNotes').value).toBe('Rejected due to incomplete documentation');
  });

  it('should enable the form controls when editable is true', () => {
    expect(component.form.get('visaReject').enabled).toBeTrue();
    expect(component.form.get('visaRejectNotes').enabled).toBeTrue();
  });

  it('should disable the form controls when editable is false', () => {
    component.editable = false;
    component.ngOnInit();
    fixture.detectChanges();

    expect(component.form.get('visaReject').disabled).toBeTrue();
    expect(component.form.get('visaRejectNotes').disabled).toBeTrue();
  });

  it('should display visaRejectNotes field when visaReject is Yes or Unsure', () => {
    component.form.get('visaReject').setValue('Yes');
    fixture.detectChanges();
    let notesField = fixture.nativeElement.querySelector('#visaRejectNotes');
    expect(notesField).not.toBeNull();

    component.form.get('visaReject').setValue('Unsure');
    fixture.detectChanges();
    notesField = fixture.nativeElement.querySelector('#visaRejectNotes');
    expect(notesField).not.toBeNull();
  });

  it('should hide visaRejectNotes field when visaReject is No', () => {
    component.form.get('visaReject').setValue('No');
    fixture.detectChanges();
    const notesField = fixture.nativeElement.querySelector('#visaRejectNotes');
    expect(notesField).toBeNull();
  });
});
