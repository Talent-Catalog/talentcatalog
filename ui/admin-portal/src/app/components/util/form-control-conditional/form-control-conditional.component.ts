import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {FormControl} from "@angular/forms";

@Component({
  selector: 'app-form-control-conditional',
  templateUrl: './form-control-conditional.component.html',
  styleUrls: ['./form-control-conditional.component.scss']
})
export class FormControlConditionalComponent implements OnInit, OnChanges {

  @Input() control: FormControl;
  @Input() regex: RegExp;
  @Input() dependantOn: string[];
  @Input() dependantValue: string;
  @Input() errorMsg: string;

  error: string;

  value: string;

  constructor() { }

  ngOnInit(): void {
    this.value = this.control.value;
    this.checkStatus();
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes && changes.dependantValue && changes.dependantValue.previousValue !== changes.dependantValue.currentValue) {
      this.checkStatus();
    }
  }

  update() {
    // Only send the string to the component form if date matches the correct format
    if (this.value != null) {
      if (this.dependantCheck()) {
        if (this.regex.test(this.value)) {
          this.error = null;
          this.control.patchValue(this.value);
        } else {
          this.error = this.errorMsg;
        }
      } else {
        this.control.patchValue(this.value);
      }
    }
  }

  checkStatus() {
    if (this.dependantCheck()) {
      if (this.regex.test(this.value)) {
        this.error = null;
      } else {
        this.error = this.errorMsg;
      }
    } else {
      this.error = null;
    }
  }

  dependantCheck() {
    if (this.dependantOn != null && this.dependantValue != null) {
      return this.dependantOn.some(d =>  d === this.dependantValue)
    } else {
      return true;
    }
  }

}
