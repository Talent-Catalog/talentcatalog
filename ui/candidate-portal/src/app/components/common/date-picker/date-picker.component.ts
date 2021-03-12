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
  // If don't want to allow selection of a future date, set to false.
  @Input() allowFuture: boolean = true;
  date: string;
  today: Date;
  maxDate: NgbDateStruct;
  minDate: NgbDateStruct;

  constructor() { }

  ngOnInit(): void {
    this.date = this.control.value;
    this.minDate = {year: 1930, month: 1, day: 1}
    this.today = new Date();
    if (this.allowFuture) {
      this.maxDate = null;
    } else {
      this.maxDate = {year: this.today.getFullYear(), month: this.today.getMonth() + 1, day: this.today.getDate()};
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
