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

import {By} from '@angular/platform-browser';
import {NclcScoreValidationComponent} from "./nclc-score-validation.component";
import {ComponentFixture, fakeAsync, TestBed, tick} from "@angular/core/testing";
import {UntypedFormControl, FormsModule, ReactiveFormsModule} from "@angular/forms";

describe('NclcScoreValidationComponent', () => {
  let component: NclcScoreValidationComponent;
  let fixture: ComponentFixture<NclcScoreValidationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [NclcScoreValidationComponent],
      imports: [FormsModule, ReactiveFormsModule]
    }).compileComponents();

    fixture = TestBed.createComponent(NclcScoreValidationComponent);
    component = fixture.componentInstance;
    component.control = new UntypedFormControl();
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize regex and value correctly', () => {
    component.ngOnInit();
    expect(component.regex).toEqual(new RegExp('^([1-9]|10)$'));
    expect(component.value).toBeNull();
  });

  it('should set value from control if available', () => {
    component.control.setValue('5');
    component.ngOnInit();
    expect(component.value).toBe('5');
  });

  it('should update control value on valid input', () => {
    component.value = '5';
    component.update();
    expect(component.control.value).toBe('5');
    expect(component.error).toBeUndefined();
  });

  it('should show error and clear input on invalid input', fakeAsync(() => {
    component.value = '15';
    component.update();
    expect(component.control.value).toBe(0);
    expect(component.error).toBe('NCLC grades are always a whole number between 1 and 10. See tooltip for help.');
    tick(1000);
    expect(component.value).toBeNull();
    tick(3000);
    expect(component.error).toBeNull();
  }));

  it('should set control value to 0 if input is null', () => {
    component.value = null;
    component.update();
    expect(component.control.value).toBe(0);
  });

  it('should display error message in template', fakeAsync(() => {
    component.value = '15';
    component.update();
    fixture.detectChanges();
    const errorElement = fixture.debugElement.query(By.css('.alert-danger'));
    expect(errorElement).toBeTruthy();
    expect(errorElement.nativeElement.textContent).toContain('NCLC grades are always a whole number between 1 and 10. See tooltip for help.');
    tick(4000);
    fixture.detectChanges();
    expect(fixture.debugElement.query(By.css('.alert-danger'))).toBeNull();
  }));
});
