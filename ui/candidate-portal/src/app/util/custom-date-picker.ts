/*
 * Copyright (c) 2021 Talent Beyond Boundaries.
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

/*
  This code taken from example here https://stackoverflow.com/a/57856590.
 */

import {Injectable} from "@angular/core";
import {NgbDatepickerI18n, NgbDateStruct} from "@ng-bootstrap/ng-bootstrap";
import {LanguageService} from "../services/language.service";

@Injectable()
export class CustomDatepickerI18n extends NgbDatepickerI18n {

  constructor(private languageService: LanguageService) {
    super();
  }

  /**
   * Avoid null pointer crash if we have added a new language but haven't filled in translations here.
   * This will default to English if it can't find anything else - rather than crashing.
   */
  private safeGetValues() {
    let values = I18N_VALUES[this.languageService.selectedLanguage];
    if (!values) {
      values = I18N_VALUES['en'];
    }
    return values;
  }

  getWeekdayShortName(weekday: number): string {
    return this.safeGetValues().weekdays[weekday - 1];
  }
  getMonthShortName(month: number): string {
    return this.safeGetValues().months[month - 1];
  }
  getMonthFullName(month: number): string {
    return this.getMonthShortName(month);
  }

  getDayAriaLabel(date: NgbDateStruct): string {
    return `${date.day}-${date.month}-${date.year}`;
  }
}

/*
  Potential other option to look at, using CLDR data https://www.npmjs.com/package/cldr-data. Can import calendar data
  for a particular language eg. Arabic: https://github.com/unicode-cldr/cldr-dates-full/blob/master/main/ar/ca-gregorian.json
 */
/* todo
JC - We can probably up load these values from server taken from Locale
 */
const I18N_VALUES = {
  'ar': {
    weekdays: ["اثنين", "ثلاثاء", "أربعاء", "خميس", "جمعة", "سبت",  "أحد" ],
    months: [ "يناير", "فبراير", "مارس", "أبريل", "مايو", "يونيو", "يوليو", "أغسطس", "سبتمبر", "أكتوبر", "نوفمبر", "ديسمبر" ],
  },
  'en': {
    weekdays: ['M', 'Tu', 'W', 'Th', 'F', 'Sa', 'Su'],
    months: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'June', 'July', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'],
  }
};
