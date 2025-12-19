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
import {VisaOneEightSixComponent} from './visa-one-eight-six.component';
import {ReactiveFormsModule, UntypedFormBuilder} from '@angular/forms';
import {AutosaveStatusComponent} from "../../../../util/autosave-status/autosave-status.component";
import {NgSelectModule} from "@ng-select/ng-select";
import {HttpClientTestingModule} from "@angular/common/http/testing";

describe('VisaOneEightSixComponent', () => {
  let component: VisaOneEightSixComponent;
  let fixture: ComponentFixture<VisaOneEightSixComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [VisaOneEightSixComponent,AutosaveStatusComponent],
      imports: [HttpClientTestingModule,ReactiveFormsModule,NgSelectModule],
      providers: [UntypedFormBuilder] // Provide the FormBuilder
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(VisaOneEightSixComponent);
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

  it('should return true if eligible186 value is Yes or No', () => {
    component.form.patchValue({ visaJobEligible186: 'Yes' });
    expect(component.hasNotes).toBe(true);
    component.form.patchValue({ visaJobEligible186: 'No' });
    expect(component.hasNotes).toBe(true);
  });

  it('should return false if eligible186 value is undefined or null', () => {
    component.form.patchValue({ visaJobEligible186: undefined });
    expect(component.hasNotes).toBe(false);
    component.form.patchValue({ visaJobEligible186: null });
    expect(component.hasNotes).toBe(false);
  });
});
