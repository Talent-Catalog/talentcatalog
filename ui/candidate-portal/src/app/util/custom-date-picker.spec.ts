/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 *  the terms of the GNU General Public License as published by the Free
 *  Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

import {TranslationWidth} from '@angular/common';
import {LanguageService} from '../services/language.service';
import {CustomDatepickerI18n} from './custom-date-picker';

describe('CustomDatepickerI18n', () => {
  let languageService: jasmine.SpyObj<LanguageService>;
  let service: CustomDatepickerI18n;

  beforeEach(() => {
    languageService = jasmine.createSpyObj<LanguageService>(
      'LanguageService',
      ['getDatePickerMonthName', 'getDatePickerWeekdayName']
    );
    service = new CustomDatepickerI18n(languageService);
  });

  it('returns the translated short month name', () => {
    languageService.getDatePickerMonthName.and.returnValue('Jan');

    expect(service.getMonthShortName(1)).toBe('Jan');
    expect(languageService.getDatePickerMonthName).toHaveBeenCalledWith(1);
  });

  it('falls back to the month number when no translation is available', () => {
    languageService.getDatePickerMonthName.and.returnValue('');

    expect(service.getMonthShortName(7)).toBe('7');
  });

  it('uses the short month name as the full month name', () => {
    languageService.getDatePickerMonthName.and.returnValue('February');

    expect(service.getMonthFullName(2)).toBe('February');
  });

  it('builds the day aria label from the date parts', () => {
    expect(service.getDayAriaLabel({year: 2026, month: 7, day: 27}))
    .toBe('27-7-2026');
  });

  it('returns the translated weekday label', () => {
    languageService.getDatePickerWeekdayName.and.returnValue('Mon');

    expect(service.getWeekdayLabel(1, TranslationWidth.Abbreviated)).toBe('Mon');
    expect(languageService.getDatePickerWeekdayName).toHaveBeenCalledWith(1);
  });
});
