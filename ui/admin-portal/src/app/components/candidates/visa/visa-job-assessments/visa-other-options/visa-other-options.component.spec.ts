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
import {VisaOtherOptionsComponent} from './visa-other-options.component';
import {ReactiveFormsModule, UntypedFormBuilder} from '@angular/forms';
import {AutosaveStatusComponent} from "../../../../util/autosave-status/autosave-status.component";
import {NgSelectModule} from "@ng-select/ng-select";
import {HttpClientTestingModule} from "@angular/common/http/testing";

describe('VisaOtherOptionsComponent', () => {
  let component: VisaOtherOptionsComponent;
  let fixture: ComponentFixture<VisaOtherOptionsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [VisaOtherOptionsComponent,AutosaveStatusComponent],
      imports: [HttpClientTestingModule,ReactiveFormsModule,NgSelectModule],
      providers: [UntypedFormBuilder] // Provide the FormBuilder
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(VisaOtherOptionsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should display error message if error is set', () => {
    component.error = 'Some error occurred';
    fixture.detectChanges();
    const errorElement: HTMLElement = fixture.nativeElement.querySelector('div');
    expect(errorElement.textContent).toContain('Some error occurred');
  });

  it('should return true if eligibleOther value is not NoResponse', () => {
    component.form.patchValue({ visaJobEligibleOther: 'Yes' });
    expect(component.hasNotes).toBe(true);
    component.form.patchValue({ visaJobEligibleOther: 'No' });
    expect(component.hasNotes).toBe(true);
  });

  it('should return false if eligibleOther value is NoResponse', () => {
    component.form.patchValue({ visaJobEligibleOther: 'NoResponse' });
    expect(component.hasNotes).toBe(false);
  });
});
