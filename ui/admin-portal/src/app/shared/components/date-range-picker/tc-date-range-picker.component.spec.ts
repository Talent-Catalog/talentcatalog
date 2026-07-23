/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {NgbDate, NgbDatepickerModule} from '@ng-bootstrap/ng-bootstrap';
import {NO_ERRORS_SCHEMA} from "@angular/core";
import {LanguageService} from "../../../services/language.service";
import {of} from "rxjs";

import {TcDateRangePickerComponent} from './tc-date-range-picker.component';

describe('TcDateRangePickerComponent', () => {
  let component: TcDateRangePickerComponent;
  let fixture: ComponentFixture<TcDateRangePickerComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [TcDateRangePickerComponent],
      imports: [NgbDatepickerModule],
      providers: [
        {
          provide: LanguageService,
          useValue: { loadDatePickerLanguageData: () => of(null) }
        }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    });
    fixture = TestBed.createComponent(TcDateRangePickerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize displayDate and load date picker language data', () => {
    const languageService = TestBed.inject(LanguageService) as any;

    const loadSpy = spyOn(
      languageService,
      'loadDatePickerLanguageData'
    ).and.returnValue(of(null));

    component.displayDate = 'old value';

    component.ngOnInit();

    expect(component.displayDate).toBeNull();
    expect(loadSpy).toHaveBeenCalled();
  });

  it('should initialize without calling language setup when service method is unavailable', () => {
    const originalService = (component as any).languageService;
    (component as any).languageService = {};

    component.displayDate = 'old value';

    expect(() => component.ngOnInit()).not.toThrow();
    expect(component.displayDate).toBeNull();

    (component as any).languageService = originalService;
  });

  it('should select the first date', () => {
    const selectedDate = new NgbDate(2026, 7, 10);
    const emitSpy = spyOn(component.dateSelected, 'emit');

    component.fromDate = null;
    component.toDate = null;

    component.selectDate(selectedDate);

    expect(component.fromDate).toBe(selectedDate);
    expect(component.toDate).toBeNull();
    expect(component.displayDate).toBe('2026/7/10');
    expect(emitSpy).toHaveBeenCalledWith({
      fromDate: selectedDate,
      toDate: null
    });
  });

  it('should select a later second date as the range end', () => {
    const fromDate = new NgbDate(2026, 7, 10);
    const toDate = new NgbDate(2026, 7, 15);
    const emitSpy = spyOn(component.dateSelected, 'emit');

    component.fromDate = fromDate;
    component.toDate = null;

    component.selectDate(toDate);

    expect(component.fromDate).toBe(fromDate);
    expect(component.toDate).toBe(toDate);
    expect(component.displayDate)
    .toBe('2026/7/10 - 2026/7/15');
    expect(emitSpy).toHaveBeenCalledWith({
      fromDate,
      toDate
    });
  });

  it('should restart the range when selected date is before the current start', () => {
    const previousFromDate = new NgbDate(2026, 7, 10);
    const selectedDate = new NgbDate(2026, 7, 5);

    component.fromDate = previousFromDate;
    component.toDate = null;

    component.selectDate(selectedDate);

    expect(component.fromDate).toBe(selectedDate);
    expect(component.toDate).toBeNull();
    expect(component.displayDate).toBe('2026/7/5');
  });

  it('should restart the range when a complete range already exists', () => {
    const selectedDate = new NgbDate(2026, 8, 1);

    component.fromDate = new NgbDate(2026, 7, 10);
    component.toDate = new NgbDate(2026, 7, 15);

    component.selectDate(selectedDate);

    expect(component.fromDate).toBe(selectedDate);
    expect(component.toDate).toBeNull();
    expect(component.displayDate).toBe('2026/8/1');
  });

  it('should return true when date is hovered inside an unfinished range', () => {
    component.fromDate = new NgbDate(2026, 7, 10);
    component.toDate = null;
    component.hoveredDate = new NgbDate(2026, 7, 20);

    const date = new NgbDate(2026, 7, 15);

    expect(component.isHovered(date)).toBeTrue();
  });

  it('should return false when hover range conditions are not satisfied', () => {
    const date = new NgbDate(2026, 7, 15);

    component.fromDate = null;
    component.toDate = null;
    component.hoveredDate = null;

    expect(component.isHovered(date)).toBeFalsy();

    component.fromDate = new NgbDate(2026, 7, 10);
    component.toDate = new NgbDate(2026, 7, 20);
    component.hoveredDate = new NgbDate(2026, 7, 25);

    expect(component.isHovered(date)).toBeFalsy();
  });

  it('should detect a date inside the selected range', () => {
    component.fromDate = new NgbDate(2026, 7, 10);
    component.toDate = new NgbDate(2026, 7, 20);

    expect(
      component.isInside(new NgbDate(2026, 7, 15))
    ).toBeTrue();

    expect(
      component.isInside(new NgbDate(2026, 7, 25))
    ).toBeFalse();
  });

  it('should detect range boundaries, inside dates and hovered dates', () => {
    const fromDate = new NgbDate(2026, 7, 10);
    const toDate = new NgbDate(2026, 7, 20);

    component.fromDate = fromDate;
    component.toDate = toDate;
    component.hoveredDate = null;

    expect(component.isRange(fromDate)).toBeTrue();
    expect(component.isRange(toDate)).toBeTrue();

    expect(
      component.isRange(new NgbDate(2026, 7, 15))
    ).toBeTrue();

    expect(
      component.isRange(new NgbDate(2026, 7, 25))
    ).toBeFalse();

    component.toDate = null;
    component.hoveredDate = new NgbDate(2026, 7, 20);

    expect(
      component.isRange(new NgbDate(2026, 7, 15))
    ).toBeTrue();
  });
  it('should render a date and return an empty string for a null date', () => {
    expect(component.renderDate(null)).toBe('');
    expect(
      component.renderDate({
        year: 2026,
        month: 7,
        day: 21
      })
    ).toBe('2026/7/21');
  });

  it('should clear dates and emit an empty range', () => {
    const emitSpy = spyOn(component.dateSelected, 'emit');

    component.fromDate = new NgbDate(2026, 7, 10);
    component.toDate = new NgbDate(2026, 7, 20);
    component.displayDate = '2026/7/10 - 2026/7/20';

    component.clearDates();

    expect(component.fromDate).toBeNull();
    expect(component.toDate).toBeNull();
    expect(component.displayDate).toBeNull();
    expect(emitSpy).toHaveBeenCalledWith({
      fromDate: null,
      toDate: null
    });
  });

});
