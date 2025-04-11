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

import {DateRangePickerComponent} from "./date-range-picker.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {FormsModule} from "@angular/forms";
import {NgbDate, NgbDateStruct, NgbModule} from "@ng-bootstrap/ng-bootstrap";
import {HttpClientTestingModule} from "@angular/common/http/testing";

describe('DateRangePickerComponent', () => {
  let component: DateRangePickerComponent;
  let fixture: ComponentFixture<DateRangePickerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [DateRangePickerComponent],
      imports: [HttpClientTestingModule ,FormsModule, NgbModule]
    }).compileComponents();

    fixture = TestBed.createComponent(DateRangePickerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize', () => {
    expect(component.fromDate).toBeUndefined();
    expect(component.toDate).toBeUndefined();
    expect(component.readonly).toBeFalsy();
    expect(component.displayDate).toBeNull();
  });

  it('should select date when no dates are selected', () => {
    const date = new NgbDate(2023, 7, 1);
    component.selectDate(date);

    expect(component.fromDate).toEqual(date);
    expect(component.toDate).toBeUndefined();
    expect(component.displayDate).toEqual('2023/7/1');
  });

  it('should select start date when start date exists and end date does not', () => {
    const startDate = new NgbDate(2023, 7, 1);
    const endDate = new NgbDate(2023, 7, 5);
    component.fromDate = startDate;
    component.selectDate(endDate);

    expect(component.fromDate).toEqual(startDate);
    expect(component.toDate).toEqual(endDate);
    expect(component.displayDate).toEqual('2023/7/1 - 2023/7/5');
  });

  it('should reset dates when clearDates is called', () => {
    component.fromDate = new NgbDate(2023, 7, 1);
    component.toDate = new NgbDate(2023, 7, 5);
    component.displayDate = '2023/7/1 - 2023/7/5';
    component.clearDates();

    expect(component.fromDate).toBeNull();
    expect(component.toDate).toBeNull();
    expect(component.displayDate).toBeNull();
  });

  it('should emit dateSelected event when dates are selected or cleared', () => {
    const fromDate = new NgbDate(2023, 7, 1);
    const toDate = new NgbDate(2023, 7, 5);

    let emittedFromDate: NgbDateStruct;
    let emittedToDate: NgbDateStruct;

    component.dateSelected.subscribe((dates: { fromDate: NgbDateStruct, toDate: NgbDateStruct }) => {
      emittedFromDate = dates.fromDate;
      emittedToDate = dates.toDate;
    });

    // Select dates
    component.selectDate(fromDate);
    expect(emittedFromDate).toEqual(fromDate);
    expect(emittedToDate).toBeUndefined();

    component.selectDate(toDate);
    expect(emittedFromDate).toEqual(fromDate);
    expect(emittedToDate).toEqual(toDate);

    // Clear dates
    component.clearDates();
    expect(emittedFromDate).toBeNull();
    expect(emittedToDate).toBeNull();
  });

  it('should determine if date is hovered', () => {
    component.fromDate = new NgbDate(2023, 7, 1);
    component.hoveredDate = new NgbDate(2023, 7, 5);

    // Hovered date should be between from date and hovered date
    const hoveredDate1 = new NgbDate(2023, 7, 3);
    expect(component.isHovered(hoveredDate1)).toBeTrue();

    // Should not be hovered if outside range
    const hoveredDate2 = new NgbDate(2023, 6, 30);
    expect(component.isHovered(hoveredDate2)).toBeFalse();
  });

  it('should determine if date is inside range', () => {
    component.fromDate = new NgbDate(2023, 7, 1);
    component.toDate = new NgbDate(2023, 7, 5);

    // Inside range should be between from date and to date
    const insideDate1 = new NgbDate(2023, 7, 3);
    expect(component.isInside(insideDate1)).toBeTrue();

    // Should not be inside range if outside range
    const insideDate2 = new NgbDate(2023, 6, 30);
    expect(component.isInside(insideDate2)).toBeFalse();
  });

  it('should determine if date is part of range', () => {
    component.fromDate = new NgbDate(2023, 7, 1);
    component.toDate = new NgbDate(2023, 7, 5);
    component.hoveredDate = new NgbDate(2023, 7, 3);

    // Convert NgbDateStruct to NgbDate for range checking
    const fromDateNgbDate: NgbDate = new NgbDate(component.fromDate.year, component.fromDate.month, component.fromDate.day);
    const toDateNgbDate: NgbDate = new NgbDate(component.toDate.year, component.toDate.month, component.toDate.day);
    const hoveredDateNgbDate: NgbDate = new NgbDate(component.hoveredDate.year, component.hoveredDate.month, component.hoveredDate.day);

    // From date, to date, hovered date, and inside date should be part of range
    expect(component.isRange(fromDateNgbDate)).toBeTrue();
    expect(component.isRange(toDateNgbDate)).toBeTrue();
    expect(component.isRange(hoveredDateNgbDate)).toBeTrue();

    const insideDate: NgbDate = new NgbDate(2023, 7, 3);
    expect(component.isRange(insideDate)).toBeTrue();

    // Date outside range should not be part of range
    const outsideDate: NgbDate = new NgbDate(2023, 6, 30);
    expect(component.isRange(outsideDate)).toBeFalse();
  });

});
