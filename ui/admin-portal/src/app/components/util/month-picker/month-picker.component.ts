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

import {Component, Input, OnInit} from '@angular/core';
import {generateYearArray} from '../../../util/year-helper';
import {UntypedFormControl} from '@angular/forms';

@Component({
  selector: 'app-month-picker',
  templateUrl: './month-picker.component.html',
  styleUrls: ['./month-picker.component.scss']
})
export class MonthPickerComponent implements OnInit {

  @Input() control: UntypedFormControl;

  month;
  year;
  date;

  years: number[];
  months = ['Jan', 'Feb', 'March', 'April', 'May', 'June', 'July', 'Aug', 'Sept', 'Oct', 'Nov', 'Dec'];

  constructor() { }

  ngOnInit() {
    this.years = generateYearArray(1950, true);
    if (this.control.value){
      this.date = new Date(this.control.value);
      this.month = this.months[this.date.getMonth()];
      this.year = this.date.getFullYear();
    } else {
      this.month = null;
      this.year = null;
    }
  }

  updateMonth(){
    if (!this.date){
      this.date = new Date();
      //Set date as the 1st as default
      this.date.setDate(1);
    }
    if (this.month){
      this.date.setMonth(this.months.indexOf(this.month));
    } else {
      this.date = null;
    }
    this.control.patchValue(this.date);

  }

  updateYear(){
    if (!this.date){
      this.date = new Date();
      //Set date as the 1st as default
      this.date.setDate(1);
    }
    if (this.year){
      this.date.setFullYear(this.year);
    } else {
      this.date = null;
    }
    this.control.patchValue(this.date);
  }



}
