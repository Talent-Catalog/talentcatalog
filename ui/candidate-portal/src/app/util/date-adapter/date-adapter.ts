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

import {formatDate} from '@angular/common';

export function dateString(dateObj: Date): string {
  return formatDate(dateObj, 'dd MMM yy' , 'en-US');
}

/**
 * Converts a date (which will normally include both date and time) and converts it to just a date.
 * <p/>
 * @param date Can be anything that can be converted to a Date - eg a standard date format string.
 */
export function toDateOnly(date: any): Date {
  if (!date) {
    //For null dates, returned the oldest possible date so that they sort to the bottom.
    return new Date(0);
  }
  //Convert the incoming date into a proper Date object
  const origDate = new Date(date);
  //Return a new date just constructed from the incoming dates, year, month and day
  return new Date(Date.UTC(origDate.getFullYear(), origDate.getMonth(), origDate.getDate()));
}
