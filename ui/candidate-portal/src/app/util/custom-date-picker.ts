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

/*
  This code based on example here https://stackoverflow.com/a/57856590.
 */

import {Injectable} from "@angular/core";
import {NgbDatepickerI18n, NgbDateStruct} from "@ng-bootstrap/ng-bootstrap";
import {LanguageService} from "../services/language.service";
import {TranslationWidth} from "@angular/common";

@Injectable()
export class CustomDatepickerI18n extends NgbDatepickerI18n {

  constructor(private languageService: LanguageService) {
    super();
  }
  getMonthShortName(month: number): string {
    return this.languageService.getDatePickerMonthName(month);
  }
  getMonthFullName(month: number): string {
    return this.getMonthShortName(month);
  }

  getDayAriaLabel(date: NgbDateStruct): string {
    return `${date.day}-${date.month}-${date.year}`;
  }

  getWeekdayLabel(weekday: number, width?: TranslationWidth): string {
    return this.languageService.getDatePickerWeekdayName(weekday);
  }
}
