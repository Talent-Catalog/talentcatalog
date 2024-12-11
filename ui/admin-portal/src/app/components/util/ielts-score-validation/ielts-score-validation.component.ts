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

import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {UntypedFormControl} from "@angular/forms";

@Component({
  selector: 'app-ielts-score-validation',
  templateUrl: './ielts-score-validation.component.html',
  styleUrls: ['./ielts-score-validation.component.scss']
})
export class IeltsScoreValidationComponent implements OnInit, OnChanges {

  @Input() control: UntypedFormControl;
  @Input() examType: string;

  errorMsg: string;
  regex: RegExp;
  error: string;
  ieltsExams: string[];

  value: string;

  constructor() { }

  ngOnInit(): void {
    this.value = this.control.value;
    this.ieltsExams = ['IELTSGen', 'IELTSAca']
    this.regex = new RegExp('^([0-8](\\.5)?$)|(^9$)');
    this.errorMsg = "The IELTS score must be between 0-9 and with decimal increments of .5 only."
    this.checkStatus();
  }



  ngOnChanges(changes: SimpleChanges) {
    if (changes && changes.examType && changes.examType.previousValue !== changes.examType.currentValue) {
      this.checkStatus();
    }
  }

  update() {
    // Only send the string to the component form if date matches the correct format
    if (this.value != null && this.value !== "") {
      if (this.ieltsExamCheck()) {
        if (this.regex.test(this.value)) {
          this.error = null;
          this.control.patchValue(this.value);
        } else {
          this.error = this.errorMsg;
        }
      } else {
        this.control.patchValue(this.value);
      }
    } else {
      this.control.patchValue('NoResponse');
    }
  }

  checkStatus() {
    if (this.ieltsExamCheck()) {
      if (this.regex.test(this.value)) {
        this.error = null;
      } else {
        this.error = this.errorMsg;
      }
    } else {
      this.error = null;
    }
  }

  ieltsExamCheck() {
    // Check if changing exam type is an ielts, otherwise assume it is an ielts exam.
    if (this.examType != null) {
      return this.ieltsExams?.some(d =>  d === this.examType)
    } else {
      return true;
    }
  }

}
