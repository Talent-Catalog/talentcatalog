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
import {EligiblePathwaysComponent} from './eligible-pathways.component';
import {CandidateVisaCheckService} from '../../../../../services/candidate-visa-check.service';
import {AutosaveStatusComponent} from "../../../../util/autosave-status/autosave-status.component";
import {HttpClientTestingModule} from "@angular/common/http/testing";

describe('EligiblePathwaysComponent', () => {
  let component: EligiblePathwaysComponent;
  let fixture: ComponentFixture<EligiblePathwaysComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [EligiblePathwaysComponent,AutosaveStatusComponent],
      imports: [HttpClientTestingModule,ReactiveFormsModule],
      providers: [UntypedFormBuilder, CandidateVisaCheckService]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(EligiblePathwaysComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form with visaJobEligiblePathways value', () => {
    const mockVisaJobCheck = {
      id: 1,
      eligiblePathways: 'Skilled Migration, Family Stream'
    };

    component.visaJobCheck = mockVisaJobCheck;
    component.ngOnInit();

    expect(component.form.get('visaJobEligiblePathways').value).toBe('Skilled Migration, Family Stream');
  });

  it('should update visaJobEligiblePathways value on form changes', () => {
    const mockVisaJobCheck = {
      id: 1,
      eligiblePathways: 'Skilled Migration, Family Stream'
    };
    const newEligiblePathways = 'Employer Nomination Scheme';

    component.visaJobCheck = {id:2,eligiblePathways:newEligiblePathways};
    component.ngOnInit();
    component.form.get('visaJobEligiblePathways').setValue(newEligiblePathways);

    expect(component.visaJobCheck.eligiblePathways).toBe(newEligiblePathways);
  });
});
