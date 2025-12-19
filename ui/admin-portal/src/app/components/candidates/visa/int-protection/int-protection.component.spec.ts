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
import {UntypedFormBuilder, ReactiveFormsModule} from '@angular/forms';
import {IntProtectionComponent} from './int-protection.component';
import {CandidateVisaCheckService} from '../../../../services/candidate-visa-check.service';
import {By} from '@angular/platform-browser';
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {NgSelectModule} from "@ng-select/ng-select";
import {AutosaveStatusComponent} from "../../../util/autosave-status/autosave-status.component";

describe('IntProtectionComponent', () => {
  let component: IntProtectionComponent;
  let fixture: ComponentFixture<IntProtectionComponent>;
  let candidateVisaCheckService: jasmine.SpyObj<CandidateVisaCheckService>;

  beforeEach(async () => {
    const candidateVisaCheckServiceSpy = jasmine.createSpyObj('CandidateVisaCheckService', ['someMethod']);

    await TestBed.configureTestingModule({
      imports: [HttpClientTestingModule,ReactiveFormsModule, NgSelectModule],
      declarations: [IntProtectionComponent,AutosaveStatusComponent],
      providers: [
        UntypedFormBuilder,
        {provide: CandidateVisaCheckService, useValue: candidateVisaCheckServiceSpy}
      ]
    }).compileComponents();

    candidateVisaCheckService = TestBed.inject(CandidateVisaCheckService) as jasmine.SpyObj<CandidateVisaCheckService>;
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(IntProtectionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('grounds textarea should be displayed when visaProtection is "Yes"', () => {
    component.form.controls['visaProtection'].setValue('Yes');
    fixture.detectChanges();
    const textarea = fixture.debugElement.query(By.css('#visaProtectionGrounds'));
    expect(textarea).toBeTruthy();
  });

  it('grounds textarea should not be displayed when visaProtection is not "Yes"', () => {
    component.form.controls['visaProtection'].setValue('No');
    fixture.detectChanges();
    const textarea = fixture.debugElement.query(By.css('#visaProtectionGrounds'));
    expect(textarea).toBeFalsy();

    component.form.controls['visaProtection'].setValue(null);
    fixture.detectChanges();
    expect(textarea).toBeFalsy();
  });
});
