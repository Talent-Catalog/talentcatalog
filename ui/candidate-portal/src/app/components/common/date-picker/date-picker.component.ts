import {Component, Input, OnInit} from '@angular/core';
import {FormControl} from "@angular/forms";
import {LanguageService} from "../../../services/language.service";

@Component({
  selector: 'app-date-picker',
  templateUrl: './date-picker.component.html',
  styleUrls: ['./date-picker.component.scss']
})
export class DatePickerComponent implements OnInit {
  @Input() control: FormControl;
  date;

  constructor(private languageService: LanguageService) { }

  ngOnInit(): void {
    this.date = this.control.value;
  }

  update() {
    this.control.patchValue(this.date);
  }

  clear() {
    this.date = null;
    this.control.patchValue(this.date);
  }

}
