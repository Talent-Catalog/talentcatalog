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
import {ReactiveFormsModule, UntypedFormBuilder} from '@angular/forms';
import {NgSelectModule} from '@ng-select/ng-select';
import {AgeRequirementComponent} from './age-requirement.component';
import {MockCandidateVisaJobCheck} from "../../../../../MockData/MockCandidateVisaCheck";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {AutosaveStatusComponent} from "../../../../util/autosave-status/autosave-status.component";

describe('AgeRequirementComponent', () => {
  let component: AgeRequirementComponent;
  let fixture: ComponentFixture<AgeRequirementComponent>;
  let fb: UntypedFormBuilder;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [AgeRequirementComponent,AutosaveStatusComponent],
      imports: [HttpClientTestingModule,ReactiveFormsModule, NgSelectModule],
      providers: [UntypedFormBuilder]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AgeRequirementComponent);
    component = fixture.componentInstance;
    fb = TestBed.inject(UntypedFormBuilder);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form with correct values', () => {
    const expectedAgeRequirement = 'Yes'; // Assuming 'Yes' is a valid initial value
    component.visaJobCheck = MockCandidateVisaJobCheck;
    component.ngOnInit();
    expect(component.form.get('visaJobAgeRequirement').value).toBe(expectedAgeRequirement);
  });
});
