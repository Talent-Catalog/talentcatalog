import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {NgbDate, NgbDateStruct} from "@ng-bootstrap/ng-bootstrap";

@Component({
  selector: 'app-date-range-picker',
  templateUrl: './date-range-picker.component.html',
  styleUrls: ['./date-range-picker.component.scss']
})
export class DateRangePickerComponent implements OnInit {

  @Input() fromDate: NgbDateStruct;
  @Input() toDate: NgbDateStruct;

  @Output() dateSelected = new EventEmitter();

  hoveredDate: NgbDate;
  displayDate: string;

  constructor() {}

  ngOnInit(): void {
    this.displayDate = null;
  }

  selectDate(date: any) {
    if (!this.fromDate && !this.toDate) {
      this.fromDate = date;
    } else if (this.fromDate && !this.toDate && date.after(this.fromDate)) {
      this.toDate = date;
    } else {
      this.toDate = null;
      this.fromDate = date;
    }
    this.updateRenderedDate();
    this.dateSelected.emit({
      fromDate: this.fromDate,
      toDate: this.toDate
    });
  }

  isHovered(date: NgbDate) {
    return this.fromDate && !this.toDate && this.hoveredDate && date.after(this.fromDate) && date.before(this.hoveredDate);
  }

  isInside(date: NgbDate) {
    return date.after(this.fromDate) && date.before(this.toDate);
  }

  isRange(date: NgbDate) {
    return date.equals(this.fromDate) || date.equals(this.toDate) || this.isInside(date) || this.isHovered(date);
  }

  renderDate(d: NgbDateStruct) {
    if (!d) {
      return ''
    }
    return `${d.year}/${d.month}/${d.day}`;
  }

  private updateRenderedDate() {
    this.displayDate = this.toDate
      ? this.renderDate(this.fromDate) + ' - ' + this.renderDate(this.toDate)
      : this.renderDate(this.fromDate);
  }

  clearDates() {
    this.fromDate = null;
    this.toDate = null;
    this.displayDate = null;
    this.dateSelected.emit({
      fromDate: null,
      toDate: null
    });
  }
}
