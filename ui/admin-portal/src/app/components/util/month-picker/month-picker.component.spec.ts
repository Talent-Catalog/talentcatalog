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

import {MonthPickerComponent} from "./month-picker.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {DebugElement} from "@angular/core";
import {UntypedFormControl, FormsModule, ReactiveFormsModule} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";
import {By} from "@angular/platform-browser";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {generateYearArray} from "../../../util/year-helper";

describe('MonthPickerComponent', () => {
  let component: MonthPickerComponent;
  let fixture: ComponentFixture<MonthPickerComponent>;
  let debugElement: DebugElement;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [MonthPickerComponent],
      imports: [HttpClientTestingModule, FormsModule, ReactiveFormsModule, NgSelectModule]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(MonthPickerComponent);
    component = fixture.componentInstance;
    debugElement = fixture.debugElement;
    component.control = new UntypedFormControl();
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize years and months correctly', () => {
    component.ngOnInit();
    expect(component.years).toEqual(generateYearArray(1950, true));
    expect(component.months).toEqual(['Jan', 'Feb', 'March', 'April', 'May', 'June', 'July', 'Aug', 'Sept', 'Oct', 'Nov', 'Dec']);
  });

  it('should initialize with empty month and year when control has no value', () => {
    component.ngOnInit();
    expect(component.month).toBeNull();
    expect(component.year).toBeNull();
  });

  it('should initialize with correct month and year when control has value', () => {
    const date = new Date(2020, 5, 1);
    component.control.setValue(date.toISOString());
    component.ngOnInit();
    expect(component.month).toBe('June');
    expect(component.year).toBe(2020);
  });

  it('should update month correctly', () => {
    component.month = 'July';
    component.updateMonth();
    expect(component.control.value.getMonth()).toBe(6); // Months are 0-indexed in JS Date
  });

  it('should update year correctly', () => {
    component.year = 2021;
    component.updateYear();
    expect(component.control.value.getFullYear()).toBe(2021);
  });

  it('should update control value when month is changed', () => {
    component.month = 'Aug';
    component.updateMonth();
    fixture.detectChanges();
    expect(component.control.value.getMonth()).toBe(7); // August is 7th month (0-indexed)
  });

  it('should handle null month correctly', () => {
    component.month = null;
    component.updateMonth();
    expect(component.control.value).toBeNull();
  });

  it('should handle null year correctly', () => {
    component.year = null;
    component.updateYear();
    expect(component.control.value).toBeNull();
  });

  it('should update year and patch control value', () => {
    component.year = 2021;
    component.updateYear();
    expect(component.control.value).toBeTruthy();
    const date = new Date(component.control.value);
    expect(date.getDate()).toBe(1); // Check if the date is set to the 1st by default
  });

  it('should update month and patch control value', () => {
    component.month = 'Aug';
    component.updateMonth();
    expect(component.control.value).toBeTruthy();
    const date = new Date(component.control.value);
    expect(date.getDate()).toBe(1); // Check if the date is set to the 1st by default
  });
});
