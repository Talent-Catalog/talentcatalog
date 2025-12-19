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

import {Pipe, PipeTransform} from '@angular/core';

@Pipe({
  name: 'filterAll'
})
export class FilterPipe implements PipeTransform {
  transform(value: any, searchText: any): any {
    if (!searchText) {
      return value;
    }
    // Check if the search text is Nan (not a number) if yes, search for name else search for candidate number
    if (isNaN(searchText)) {
      return value.filter((data) => this.matchValue(data, searchText));
    } else {
      return value.filter((data) => this.matchNumber(data, searchText))
    }
  }

  matchValue(data, value) {
    const fullName = data['user']['firstName'] + ' ' + data['user']['lastName'];
    return new RegExp(value, 'gi').test(fullName);
  }

  matchNumber(data, value) {
    return Object.keys(data).map((key) => {
      return new RegExp(value, 'gi').test(data['candidateNumber']);
    }).some(result => result);
  }
}
