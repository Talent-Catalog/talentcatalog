import {Component, Input, OnInit} from '@angular/core';
import {AbstractControl} from "@angular/forms";
import {NgbDate, NgbDateStruct} from "@ng-bootstrap/ng-bootstrap";

/**
 * @component TcDatePickerComponent
 * @selector tc-date-picker
 *
 * @description
 * A lightweight single-date picker built on `@ng-bootstrap/ng-bootstrap`’s datepicker.
 * Provides a clean Talent Catalog–styled input with a calendar toggle button, a clear
 * button, optional help text, ISO date validation (`yyyy-mm-dd`), and min/max date control
 * using `allowFuture` and `allowPast`.
 *
 * **Features**
 * - Text input + calendar popover for flexible date entry
 * - Enforces valid `yyyy-mm-dd` formatting with inline error display
 * - Optional help text for guiding users when entering approximate dates
 * - Allows restricting selectable dates via `allowFuture` and `allowPast`
 * - Clear button (`<tc-button>`) to reset the value
 * - Marks the form control as dirty on date selection
 * - Custom day template for styling the currently selected date
 * - Automatically disables input + calendar when the provided form control is disabled
 *
 * **Inputs**
 * - `control: AbstractControl` — required Angular form control for binding the date value
 * - `allowFuture: boolean` — when `false`, prevents selecting dates after today (default: `true`)
 * - `allowPast: boolean` — when `false`, prevents selecting dates before today (default: `true`)
 * - `showHelpText: boolean` — toggles instructional text below the input (default: `true`)
 *
 * **Internal Fields**
 * - `dateString: string | null` — raw ISO date string bound to the input
 * - `date: NgbDate | null` — parsed version of `dateString` for template styling
 * - `minDate: NgbDateStruct | null` — computed from `allowPast`
 * - `maxDate: NgbDateStruct | null` — computed from `allowFuture`
 * - `error: string | null` — validation error message
 *
 * **Date Validation**
 * - Accepts only `yyyy-mm-dd` values
 * - Shows a `<tc-alert type="danger">` error when format is invalid
 * - Updates the form control only when the string matches the expected pattern
 *
 * **User Actions**
 * - Clicking the input toggles the datepicker popover
 * - Clicking the calendar icon also toggles the popover
 * - Clicking the clear (×) button resets the field
 * - Selecting a date marks the form control as dirty and updates the ISO string
 *
 * **Example**
 * ```html
 * <form [formGroup]="form">
 *   <tc-date-picker
 *     [control]="form.controls['dob']"
 *     [allowFuture]="false"
 *     [allowPast]="true"
 *     [showHelpText]="true">
 *   </tc-date-picker>
 * </form>
 * ```
 *
 * **Notes**
 * - Icons are rendered using Font Awesome through `<tc-icon>`
 * - Keyboard + screen reader interactions are inherited from NG Bootstrap’s datepicker
 */

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

  /** The help text supports users entering data, as opposed to e.g. filtering stats */
  @Input() showHelpText: boolean = true;

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
