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

import {Injectable} from '@angular/core';
import {NgbDateStruct} from '@ng-bootstrap/ng-bootstrap';

@Injectable()
export class CustomDateAdapter {
  // String -> NgbDateStruct
  fromModel(value: string): NgbDateStruct {
    if (!value) {
      return null;
    }
    let [y, m, d] = value.toString().split('-');
    return {
      year: +y,
      month: +m,
      day: +d
    } as NgbDateStruct;
  }

  // NgbDateStruct -> String
  toModel(date: NgbDateStruct): string {
    return date ?
      date.year + "-" + ('0' + date.month).slice(-2) + "-" + ('0' + date.day).slice(-2) :
      null;
  }
}

@Injectable()
export class CustomDateParserFormatter {
  // String -> NgbDateStruct
  parse(value: string): NgbDateStruct {
    if (!value) {
      return null;
    }
    let [y, m, d] = value.toString().split('-');
    return {
      year: +y,
      month: +m,
      day: +d
    } as NgbDateStruct;
  }
  // NgbDateStruct -> String
  format(date: NgbDateStruct): string {
    return date ?
      date.year + "-" + ('0' + date.month).slice(-2) + "-" + ('0' + date.day).slice(-2) :
      null;
  }
}

