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

  constructor() { }

  ngOnInit(): void { }

  onInput(event: any) {
    const input = event.target as HTMLInputElement;
    let value = parseInt(input.value, 10);
    if (!isNaN(value)) {
      if (value > 10 || value < 1) {
        this.error = "NCLC grades are always a whole number between 1 and 10. See tooltip for help."
        setTimeout(
            () => this.error = null, 5000
        )
        setTimeout(
            () => input.value = null, 1000
        )
        this.control.patchValue(0);
      } else {
        this.control.patchValue(value);
      }
    } else {
      this.control.patchValue(0);
    }
  }

}
