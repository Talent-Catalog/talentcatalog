import {Component, Input, OnInit} from '@angular/core';
import {UntypedFormControl} from "@angular/forms";
@Component({
  selector: 'app-det-score-validation',
  templateUrl: './det-score-validation.component.html',
  styleUrls: ['./det-score-validation.component.scss']
})
export class DetScoreValidationComponent implements OnInit{

  @Input() control: UntypedFormControl;

  error: string;
  regex: RegExp;
  value: string;

  constructor() { }

  ngOnInit(): void {
    this.value = this.control.value;
    this.regex = new RegExp('^(1[0-9]|[2-9][0-9]|1[0-5][0-9]|160)$');
  }

  update() {
    if (this.value !== null) {
      if (!this.regex.test(this.value)) {
      // If user has entered non-null value outside allowed range, display error and delete input.
        this.error = "DET grades are always a whole number between 10 and 160."
        this.control.patchValue(0);
      } else {
        this.error = null;
        this.control.patchValue(this.value);
      }
    } else {
      // If field has been updated and value is null, the user has deleted the previous value.
      this.control.patchValue(0);
      this.error = null;
    }
  }

}
