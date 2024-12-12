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
import {generateYearArray} from "../../../util/year-helper";
import {UntypedFormControl} from "@angular/forms";

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
   months = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12];
  constructor() { }

  ngOnInit() {
    this.years = generateYearArray();
    if (this.control.value){
      this.date = new Date(this.control.value);
      this.month = this.date.getMonth()+1;
      this.year = this.date.getFullYear();
    }
  }

  updateMonth(){
    if (!this.date){
      this.date = new Date();
    }
    if (this.month){
      this.date.setMonth(this.month-1);
    } else {
      this.date = null;
    }
    this.control.patchValue(this.date);

  }

  updateYear(){
    if (!this.date){
      this.date = new Date();
    }
    if (this.year){
      this.date.setFullYear(this.year);
    } else {
      this.date = null;
    }
    this.control.patchValue(this.date);
  }



}
