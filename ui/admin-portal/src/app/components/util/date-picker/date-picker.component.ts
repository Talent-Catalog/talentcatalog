import {Component, Input, OnInit} from '@angular/core';
import {FormControl} from "@angular/forms";
import {NgbDateStruct} from "@ng-bootstrap/ng-bootstrap";

@Component({
  selector: 'app-date-picker',
  templateUrl: './date-picker.component.html',
  styleUrls: ['./date-picker.component.scss']
})
export class DatePickerComponent implements OnInit {
  @Input() control: FormControl;

  // If don't want to allow selection of a future date, set to true.
  @Input() allowFuture: boolean = true;

  // If don't want to allow selection of a past date, set to true.
  @Input() allowPast: boolean = true;

  date: string;
  today: Date;
  maxDate: NgbDateStruct;
  minDate: NgbDateStruct;

  constructor() { }

  ngOnInit(): void {
    this.date = this.control.value;
    this.today = new Date();

    // If allow future is not true, leave as default max date. Otherwise set to today.
    if (this.allowFuture) {
      this.maxDate = null;
    } else {
      this.maxDate = {year: this.today.getFullYear(), month: this.today.getMonth() + 1, day: this.today.getDate()};
    }

    // If allow past date is true, set min date to 100 years in the past. Otherwise set to today.
    if (this.allowPast) {
      // If no past date is false
      this.minDate = {year: this.today.getFullYear() - 100, month: 1, day: 1}
    } else {
      this.minDate = {year: this.today.getFullYear(), month: this.today.getMonth() + 1, day: this.today.getDate()};
    }
  }

  update() {
    this.control.patchValue(this.date);
  }

  clear() {
    this.date = null;
    this.control.patchValue(this.date);
  }
}
