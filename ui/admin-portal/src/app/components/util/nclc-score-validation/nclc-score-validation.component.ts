/*
 * Copyright (c) 2024 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

import {Component, Input, OnInit} from '@angular/core';
import {UntypedFormControl} from "@angular/forms";

@Component({
  selector: 'app-nclc-score-validation',
  templateUrl: './nclc-score-validation.component.html',
  styleUrls: ['./nclc-score-validation.component.scss']
})
export class NclcScoreValidationComponent implements OnInit {

  @Input() control: UntypedFormControl;

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
