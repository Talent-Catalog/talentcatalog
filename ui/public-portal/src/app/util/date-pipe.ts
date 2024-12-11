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

import {Inject, LOCALE_ID, Pipe, PipeTransform} from '@angular/core';
import {DatePipe} from '@angular/common';


// See this stack overflow article explaining this code: https://stackoverflow.com/questions/56020473/override-angular-default-date-pipe
@Pipe({
  name: 'date'
})
export class ExtendDatePipe extends DatePipe implements PipeTransform {
  readonly customFormats = {
    customDefault: 'yyyy-MM-dd',
    customDateTime: 'yyyy-MM-dd, h:mm:ss a',
    customMonthYear: 'MMM yy'
  };

  constructor(@Inject(LOCALE_ID) locale: string) {
    super(locale);
  }

// Theoretically this should return string | null like the super class - but that doesn't work
// See https://stackoverflow.com/questions/64806103/extending-angular-datepipe-errors-in-angular-11-worked-in-angular-10
  transform(value: Date | string | number,
            format = 'customDefault', timezone?: string, locale?: string): any {
    format = this.customFormats[format] || format;

    return super.transform(value, format, timezone, locale);
  }
}
