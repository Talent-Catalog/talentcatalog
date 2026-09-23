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
import {of} from 'rxjs';

import {LanguageService} from '../../../services/language.service';
import {TcDateRangePickerComponent} from './tc-date-range-picker.component';
import {RouterTestingModule} from "@angular/router/testing";

describe('TcDateRangePickerComponent', () => {
  let component: TcDateRangePickerComponent;
  let fixture: ComponentFixture<TcDateRangePickerComponent>;
  let languageServiceSpy:
    jasmine.SpyObj<LanguageService>;

  beforeEach(() => {
    languageServiceSpy =
      jasmine.createSpyObj<LanguageService>(
        'LanguageService',
        ['loadDatePickerLanguageData']
      );

    languageServiceSpy.loadDatePickerLanguageData
    .and.returnValue(of(null));

    TestBed.configureTestingModule({
      imports: [
        TcDateRangePickerComponent,
        NgbDatepickerModule,
        RouterTestingModule
      ],
      providers: [
        {
          provide: LanguageService,
          useValue: languageServiceSpy
        }
      ]
    });

    fixture = TestBed.createComponent(
      TcDateRangePickerComponent
    );

    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('ngOnInit', () => {
    it('should initialize displayDate and load language data', () => {
      languageServiceSpy.loadDatePickerLanguageData
      .calls.reset();

      component.displayDate = 'old value';

      component.ngOnInit();

      expect(component.displayDate).toBeNull();

      expect(
        languageServiceSpy.loadDatePickerLanguageData
      ).toHaveBeenCalledTimes(1);
    });

    it('should initialize safely when language method is unavailable', () => {
      (component as any).languageService = {};

      component.displayDate = 'old value';

      expect(() => component.ngOnInit())
      .not.toThrow();

      expect(component.displayDate).toBeNull();
    });

    it('should initialize safely without a language service', () => {
      (component as any).languageService = null;

      expect(() => component.ngOnInit())
      .not.toThrow();

      expect(component.displayDate).toBeNull();
    });
  });

  describe('selectDate', () => {
    it('should select the first date', () => {
      const selectedDate =
        new NgbDate(2026, 7, 10);

      const emitSpy = spyOn(
        component.dateSelected,
        'emit'
      );

      component.fromDate = null;
      component.toDate = null;

      component.selectDate(selectedDate);

      expect(component.fromDate)
      .toBe(selectedDate);

      expect(component.toDate).toBeNull();

      expect(component.displayDate)
      .toBe('2026/7/10');

      expect(emitSpy).toHaveBeenCalledOnceWith({
        fromDate: selectedDate,
        toDate: null
      });
    });

    it('should select a later date as the range end', () => {
      const fromDate =
        new NgbDate(2026, 7, 10);

      const toDate =
        new NgbDate(2026, 7, 15);

      const emitSpy = spyOn(
        component.dateSelected,
        'emit'
      );

      component.fromDate = fromDate;
      component.toDate = null;

      component.selectDate(toDate);

      expect(component.fromDate).toBe(fromDate);
      expect(component.toDate).toBe(toDate);

      expect(component.displayDate)
      .toBe('2026/7/10 - 2026/7/15');

      expect(emitSpy).toHaveBeenCalledOnceWith({
        fromDate,
        toDate
      });
    });

    it('should restart when the selected date is before the start', () => {
      const selectedDate =
        new NgbDate(2026, 7, 5);

      component.fromDate =
        new NgbDate(2026, 7, 10);

      component.toDate = null;

      component.selectDate(selectedDate);

      expect(component.fromDate)
      .toBe(selectedDate);

      expect(component.toDate).toBeNull();

      expect(component.displayDate)
      .toBe('2026/7/5');
    });

    it('should restart when the selected date equals the start', () => {
      const selectedDate =
        new NgbDate(2026, 7, 10);

      component.fromDate =
        new NgbDate(2026, 7, 10);

      component.toDate = null;

      component.selectDate(selectedDate);

      expect(component.fromDate)
      .toBe(selectedDate);

      expect(component.toDate).toBeNull();
    });

    it('should restart when a complete range already exists', () => {
      const selectedDate =
        new NgbDate(2026, 8, 1);

      component.fromDate =
        new NgbDate(2026, 7, 10);

      component.toDate =
        new NgbDate(2026, 7, 15);

      component.selectDate(selectedDate);

      expect(component.fromDate)
      .toBe(selectedDate);

      expect(component.toDate).toBeNull();

      expect(component.displayDate)
      .toBe('2026/8/1');
    });
  });

  describe('isHovered', () => {
    it('should return true for a date in the hovered range', () => {
      component.fromDate =
        new NgbDate(2026, 7, 10);

      component.toDate = null;

      component.hoveredDate =
        new NgbDate(2026, 7, 20);

      expect(
        component.isHovered(
          new NgbDate(2026, 7, 15)
        )
      ).toBeTrue();
    });

    it('should return false without a start date', () => {
      component.fromDate = null;
      component.toDate = null;

      component.hoveredDate =
        new NgbDate(2026, 7, 20);

      expect(
        component.isHovered(
          new NgbDate(2026, 7, 15)
        )
      ).toBeFalsy();
    });

    it('should return false when a complete range exists', () => {
      component.fromDate =
        new NgbDate(2026, 7, 10);

      component.toDate =
        new NgbDate(2026, 7, 20);

      component.hoveredDate =
        new NgbDate(2026, 7, 25);

      expect(
        component.isHovered(
          new NgbDate(2026, 7, 15)
        )
      ).toBeFalsy();
    });

    it('should return false without a hovered date', () => {
      component.fromDate =
        new NgbDate(2026, 7, 10);

      component.toDate = null;
      component.hoveredDate = null;

      expect(
        component.isHovered(
          new NgbDate(2026, 7, 15)
        )
      ).toBeFalsy();
    });

    it('should return false outside the hovered range', () => {
      component.fromDate =
        new NgbDate(2026, 7, 10);

      component.toDate = null;

      component.hoveredDate =
        new NgbDate(2026, 7, 20);

      expect(
        component.isHovered(
          new NgbDate(2026, 7, 25)
        )
      ).toBeFalse();
    });
  });

  describe('isInside', () => {
    beforeEach(() => {
      component.fromDate =
        new NgbDate(2026, 7, 10);

      component.toDate =
        new NgbDate(2026, 7, 20);
    });

    it('should return true for a date inside the range', () => {
      expect(
        component.isInside(
          new NgbDate(2026, 7, 15)
        )
      ).toBeTrue();
    });

    it('should return false for a date outside the range', () => {
      expect(
        component.isInside(
          new NgbDate(2026, 7, 25)
        )
      ).toBeFalse();
    });

    it('should return false for range boundaries', () => {
      expect(
        component.isInside(component.fromDate as NgbDate)
      ).toBeFalse();

      expect(
        component.isInside(component.toDate as NgbDate)
      ).toBeFalse();
    });
  });

  describe('isRange', () => {
    it('should identify the start and end dates', () => {
      const fromDate =
        new NgbDate(2026, 7, 10);

      const toDate =
        new NgbDate(2026, 7, 20);

      component.fromDate = fromDate;
      component.toDate = toDate;

      expect(component.isRange(fromDate))
      .toBeTrue();

      expect(component.isRange(toDate))
      .toBeTrue();
    });

    it('should identify a date inside the range', () => {
      component.fromDate =
        new NgbDate(2026, 7, 10);

      component.toDate =
        new NgbDate(2026, 7, 20);

      expect(
        component.isRange(
          new NgbDate(2026, 7, 15)
        )
      ).toBeTrue();
    });

    it('should identify a date in the hovered range', () => {
      component.fromDate =
        new NgbDate(2026, 7, 10);

      component.toDate = null;

      component.hoveredDate =
        new NgbDate(2026, 7, 20);

      expect(
        component.isRange(
          new NgbDate(2026, 7, 15)
        )
      ).toBeTrue();
    });

    it('should return false for an unrelated date', () => {
      component.fromDate =
        new NgbDate(2026, 7, 10);

      component.toDate =
        new NgbDate(2026, 7, 20);

      expect(
        component.isRange(
          new NgbDate(2026, 7, 25)
        )
      ).toBeFalse();
    });
  });

  describe('renderDate', () => {
    it('should render a date', () => {
      expect(
        component.renderDate({
          year: 2026,
          month: 7,
          day: 21
        })
      ).toBe('2026/7/21');
    });

    it('should return an empty string for a null date', () => {
      expect(
        component.renderDate(null)
      ).toBe('');
    });
  });

  describe('clearDates', () => {
    it('should clear the selected range and emit empty values', () => {
      const emitSpy = spyOn(
        component.dateSelected,
        'emit'
      );

      component.fromDate =
        new NgbDate(2026, 7, 10);

      component.toDate =
        new NgbDate(2026, 7, 20);

      component.displayDate =
        '2026/7/10 - 2026/7/20';

      component.clearDates();

      expect(component.fromDate).toBeNull();
      expect(component.toDate).toBeNull();
      expect(component.displayDate).toBeNull();

      expect(emitSpy).toHaveBeenCalledOnceWith({
        fromDate: null,
        toDate: null
      });
    });
  });
});
