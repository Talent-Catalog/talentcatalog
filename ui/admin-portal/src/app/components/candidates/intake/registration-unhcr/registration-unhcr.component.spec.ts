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
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {NgSelectModule} from '@ng-select/ng-select';
import {RegistrationUnhcrComponent} from './registration-unhcr.component';
import {CandidateService} from '../../../../services/candidate.service';
import {UnhcrStatus, YesNo} from "../../../../model/candidate";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {AutosaveStatusComponent} from "../../../util/autosave-status/autosave-status.component";

describe('RegistrationUnhcrComponent', () => {
  let component: RegistrationUnhcrComponent;
  let fixture: ComponentFixture<RegistrationUnhcrComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ RegistrationUnhcrComponent,AutosaveStatusComponent ],
      imports: [HttpClientTestingModule, NgSelectModule, FormsModule, ReactiveFormsModule ],
      providers: [
        { provide: CandidateService }
      ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(RegistrationUnhcrComponent);
    component = fixture.componentInstance;
    component.candidateIntakeData = { unhcrStatus: UnhcrStatus.RegisteredAsylum, unhcrNumber: '12345', unhcrFile: 3, unhcrConsent: YesNo.Yes, unhcrNotes: 'Sample note' };
    component.showAll = true;
    fixture.detectChanges();
  });

  it('should initialize the form control with the correct default values', () => {
    expect(component.form.get('unhcrStatus').value).toBe(UnhcrStatus.RegisteredAsylum);
    expect(component.form.get('unhcrNumber').value).toBe('12345');
    expect(component.form.get('unhcrFile').value).toBe(3);
    expect(component.form.get('unhcrConsent').value).toBe(YesNo.Yes);
    expect(component.form.get('unhcrNotRegStatus').value).toBeNull();
    expect(component.form.get('unhcrNotes').value).toBe('Sample note');
  });

  it('should validate form controls for registered status', () => {
    component.form.patchValue({ unhcrStatus: 'RegisteredAsylum' });
    fixture.detectChanges();
    expect(component.isRegistered).toBeTrue();
    expect(component.form.get('unhcrNumber').valid).toBeTrue();
    expect(component.form.get('unhcrFile').valid).toBeTrue();
    expect(component.form.get('unhcrConsent').valid).toBeTrue();
  });

  it('should validate form controls for not registered status', () => {
    component.form.patchValue({ unhcrStatus: 'NotRegistered' });
    fixture.detectChanges();
    expect(component.isNotRegistered).toBeTrue();
    expect(component.form.get('unhcrNotRegStatus').valid).toBeTrue();
  });

  it('should display additional notes when there is a status', () => {
    component.form.patchValue({ unhcrStatus: 'RegisteredAsylum' });
    fixture.detectChanges();
    expect(component.hasNotes).toBeTrue();
    expect(component.form.get('unhcrNotes').valid).toBeTrue();
  });

  it('should not display additional notes when status is null or NoResponse', () => {
    component.form.patchValue({ unhcrStatus: null });
    fixture.detectChanges();
    expect(component.hasNotes).toBeFalse();

    component.form.patchValue({ unhcrStatus: 'NoResponse' });
    fixture.detectChanges();
    expect(component.hasNotes).toBeFalse();
  });
});
