import {Component, Input, OnInit} from '@angular/core';
import {FormControl} from "@angular/forms";

@Component({
  selector: 'app-nclc-score-validation',
  templateUrl: './nclc-score-validation.component.html',
  styleUrls: ['./nclc-score-validation.component.scss']
})
export class NclcScoreValidationComponent implements OnInit {

  @Input() control: FormControl;

  errorMsg: string;
  regex: RegExp;
  error: string;

  value: string;

  constructor() { }

  ngOnInit(): void {
    this.value = this.control.value;
    this.regex = new RegExp('^([1-9]|10)$');
    this.errorMsg = "NCLC grades are always a whole number between 1 and 10. See tooltip for help."
    this.checkStatus();
  }

  update() {
    // Only send the string to the component form if entry matches the correct format
    if (this.value != null && this.value !== "") {
        if (this.regex.test(this.value)) {
          this.error = null;
          this.control.patchValue(this.value);
        } else {
          this.error = this.errorMsg;
        }
      } else {
        this.control.patchValue('NoResponse');
      }
    }

  checkStatus() {
    if (this.regex.test(this.value)) {
      this.error = null;
    } else {
      this.error = this.errorMsg;
    }
  }

  onInput(event: any) {
    const input = event.target as HTMLInputElement;
    let value = parseInt(input.value, 10);
    if (value > 10 || input.value.length > 2) {
      input.value = value.toString().slice(0, 1);
    }
  }

}
