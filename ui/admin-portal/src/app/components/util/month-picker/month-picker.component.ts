import {Component, Input, OnInit} from '@angular/core';
import {generateYearArray} from '../../../util/year-helper';
import {FormControl} from '@angular/forms';

@Component({
  selector: 'app-month-picker',
  templateUrl: './month-picker.component.html',
  styleUrls: ['./month-picker.component.scss']
})
export class MonthPickerComponent implements OnInit {

  @Input() control: FormControl;

  month;
  year;
  date;

  years: number[];
  months = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12];

  constructor() { }

  ngOnInit() {
    this.years = generateYearArray(1950, true);
    if (this.control.value){
      this.date = new Date(this.control.value);
      this.month = this.date.getMonth() + 1;
      this.year = this.date.getFullYear();
    }
  }

  updateMonth(){
    if (!this.date){
      this.date = new Date();
    }
    if (this.month){
      this.date.setMonth(this.month - 1);
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
