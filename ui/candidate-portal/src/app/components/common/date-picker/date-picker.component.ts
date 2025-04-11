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
import {AbstractControl} from "@angular/forms";
import {NgbDateStruct} from "@ng-bootstrap/ng-bootstrap";
import {LanguageService} from "../../../services/language.service";

@Component({
  selector: 'app-date-picker',
  templateUrl: './date-picker.component.html',
  styleUrls: ['./date-picker.component.scss']
})
export class DatePickerComponent implements OnInit {
  @Input() control: AbstractControl;
  // If don't want to allow selection of a future date, set to false.
  @Input() allowFuture: boolean = true;
  date: string;
  today: Date;
  maxDate: NgbDateStruct;
  minDate: NgbDateStruct;

  constructor(public languageService: LanguageService) { }

  ngOnInit(): void {
    this.date = this.control.value;
    this.minDate = {year: 1930, month: 1, day: 1}
    this.today = new Date();
    if (this.allowFuture) {
      this.maxDate = null;
    } else {
      this.maxDate = {year: this.today.getFullYear(), month: this.today.getMonth() + 1, day: this.today.getDate()};
    }

    //Load the day and month translations suitable for the currently selected language.
    //Note that we just need to subscribe - there is no need to process the returned data
    //- that is stored locally in the language service.
    this.languageService.loadDatePickerLanguageData().subscribe();

  }

  update() {
    this.control.patchValue(this.date);
  }

  clear() {
    this.date = null;
    this.control.patchValue(this.date);
  }

}
