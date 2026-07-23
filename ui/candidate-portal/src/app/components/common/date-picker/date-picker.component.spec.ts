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
import {FormControl, FormsModule} from '@angular/forms';
import {NgbDatepickerModule} from '@ng-bootstrap/ng-bootstrap';
import {TranslateModule} from '@ngx-translate/core';
import {of} from 'rxjs';

import {DatePickerComponent} from './date-picker.component';
import {LanguageService} from '../../../services/language.service';

describe('DatePickerComponent', () => {
  let component: DatePickerComponent;
  let fixture: ComponentFixture<DatePickerComponent>;
  let languageServiceSpy: jasmine.SpyObj<LanguageService>;

  beforeEach(async () => {
    languageServiceSpy = jasmine.createSpyObj<LanguageService>(
      'LanguageService',
      ['loadDatePickerLanguageData']
    );

    languageServiceSpy.loadDatePickerLanguageData.and.returnValue(of(null));

    await TestBed.configureTestingModule({
      declarations: [DatePickerComponent],
      imports: [
        FormsModule,
        NgbDatepickerModule,
        TranslateModule.forRoot()
      ],
      providers: [
        {
          provide: LanguageService,
          useValue: languageServiceSpy
        }
      ]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(DatePickerComponent);
    component = fixture.componentInstance;

    component.control = new FormControl('2026-07-13');

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialise the date from the control', () => {
    expect(component.date).toBe('2026-07-13');
  });

  it('should update the control value', () => {
    component.date = '2026-07-14';

    component.update();

    expect(component.control.value).toBe('2026-07-14');
  });

  it('should clear the date and control value', () => {
    component.clear();

    expect(component.date).toBeNull();
    expect(component.control.value).toBeNull();
  });

  it('should limit future dates when allowFuture is false', () => {
    component.allowFuture = false;

    component.ngOnInit();

    const today = new Date();

    expect(component.maxDate).toEqual({
      year: today.getFullYear(),
      month: today.getMonth() + 1,
      day: today.getDate()
    });
  });

  it('should not set a maximum date when future dates are allowed', () => {
    component.allowFuture = true;

    component.ngOnInit();

    expect(component.maxDate).toBeNull();
  });
});
