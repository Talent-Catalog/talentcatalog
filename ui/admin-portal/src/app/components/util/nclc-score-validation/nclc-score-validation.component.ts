import {Component, Input, OnInit} from '@angular/core';
import {FormControl} from "@angular/forms";

@Component({
  selector: 'app-nclc-score-validation',
  templateUrl: './nclc-score-validation.component.html',
  styleUrls: ['./nclc-score-validation.component.scss']
})
export class NclcScoreValidationComponent implements OnInit {

  @Input() control: FormControl;

  error: string;
  regex: RegExp;
  value: string;

  constructor() { }

  ngOnInit(): void {
    this.value = this.control.value;
    this.regex = new RegExp('^([1-9]|10)$');
  }

  update() {
    if (this.value !== null) {
      if (!this.regex.test(this.value)) {
      // If user has entered non-null value outside allowed range, display error and delete input.
        this.error = "NCLC grades are always a whole number between 1 and 10. See tooltip for help."
        setTimeout(
            () => this.error = null, 4000
        )
        setTimeout(
            () => this.value = null, 1000
        )
        // Patching 0 avoids scenario where user enters e.g. 55 and 5 is erroneously saved.
        // This component uses 0 as numerical equivalent of 'NoResponse',
        // used by server to set null value in DB (see CandidateService).
        this.control.patchValue(0);
      } else {
        this.control.patchValue(this.value);
      }
    } else {
      // If field has been updated and value is null, the user has deleted the previous value.
      this.control.patchValue(0);
    }
  }

}
