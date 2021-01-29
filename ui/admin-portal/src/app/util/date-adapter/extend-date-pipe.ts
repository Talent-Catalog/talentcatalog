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

import {Inject, LOCALE_ID, Pipe, PipeTransform} from '@angular/core';
import {DatePipe} from '@angular/common';


// See this stack overflow article explaining this code: https://stackoverflow.com/questions/56020473/override-angular-default-date-pipe
@Pipe({
  name: 'date'
})
export class ExtendDatePipe extends DatePipe implements PipeTransform {
  readonly customFormats = {
    customDefault: 'dd MMM yy',
    customDateTime: 'dd MMM yy, h:mm:ss a',
    customMonthYear: 'MMM yy'
  };

  constructor(@Inject(LOCALE_ID) locale: string) {
    super(locale);
  }

  transform(value: any, format = 'customDefault', timezone?: string, locale?: string): string {
    format = this.customFormats[format] || format;

    return super.transform(value, format, timezone, locale);
  }
}
