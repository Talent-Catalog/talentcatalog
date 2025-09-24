import {Component, Input, OnInit} from '@angular/core';
import {AbstractControl} from "@angular/forms";
import {NgbDate, NgbDateStruct} from "@ng-bootstrap/ng-bootstrap";

@Component({
  selector: 'tc-date-picker',
  templateUrl: './tc-date-picker.component.html',
  styleUrls: ['./tc-date-picker.component.scss']
})
export class TcDatePickerComponent implements OnInit {
  @Input() control: AbstractControl;

  // If don't want to allow selection of a future date, set to true.
  @Input() allowFuture: boolean = true;

  // If don't want to allow selection of a past date, set to true.
  @Input() allowPast: boolean = true;

  dateString: string;
  today: Date;
  maxDate: NgbDateStruct;
  minDate: NgbDateStruct;
  error: string;
  date: NgbDate;

  constructor() { }

  ngOnInit(): void {
    this.dateString = this.control.value;
    this.date = this.stringToNgbDate(this.dateString);
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
    const customDatePattern = /^\d{4}\-(0?[1-9]|1[012])\-(0?[1-9]|[12][0-9]|3[01])$/;
    // Only send the string to the component form if date is null or matches the correct format
    if (this.dateString == null || this.dateString.match(customDatePattern) ) {
      this.error = null;
      this.control.patchValue(this.dateString);
    } else {
      this.error = 'Incorrect date format, please type date in yyyy-mm-dd';
    }
    this.date = this.stringToNgbDate(this.dateString);
  }

  clear() {
    this.dateString = null;
    this.control.patchValue(this.dateString);
  }

  /**
   * This method allows us to style the selected date in the custom date picker template.
   * The NgbDatepicker only accepts NgbDate values, not strings, so the selected date string needs to be
   * converted to a date value so it can be passed into the template and then styled.
   * @param s date string
   * @private
   */
  private stringToNgbDate(s: string): NgbDate | null {
    let year: number;
    let month: number;
    let day: number;

    if (s != null && s.length !== 0) {
      const [yearStr, monthStr, dayStr] = s.split('-')
      year = Number(yearStr);
      month = Number(monthStr);
      day = Number(dayStr);
      if (!isNaN(year) && !isNaN(month) && !isNaN(day)) {
        return new NgbDate(year, month, day);
      } else {
        return null;
      }
    } else {
      return null
    }
  }
}
