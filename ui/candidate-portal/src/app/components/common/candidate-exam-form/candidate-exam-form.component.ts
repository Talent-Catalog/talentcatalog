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

import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {UntypedFormBuilder, UntypedFormGroup, Validators} from "@angular/forms";
import {Candidate, CandidateExam, Exam} from "../../../model/candidate";
import {Router} from "@angular/router";
import {CandidateService} from "../../../services/candidate.service";
import {RegistrationService} from "../../../services/registration.service";
import {CandidateExamService} from "../../../services/candidate-exam.service";
import {generateYearArray} from "../../../util/year-helper";

@Component({
  selector: 'app-candidate-exam-form',
  templateUrl: './candidate-exam-form.component.html',
  styleUrls: ['./candidate-exam-form.component.scss']
})
export class CandidateExamFormComponent implements OnInit {

  @Input() exam: CandidateExam;
  candidate: Candidate;

  @Input() disabled: boolean = false;

  @Output() saved = new EventEmitter<CandidateExam>();

  error: any;
  saving: boolean;

  form: UntypedFormGroup;
  years: number[];
  examListEnum: { key: string, value: string }[] = [];
  constructor(private fb: UntypedFormBuilder,
              private candidateService: CandidateService,
              private candidateExamService: CandidateExamService,
              public registrationService: RegistrationService) { }
  ngOnInit() {
    this.candidateService.getCandidateAdditionalInfo().subscribe(candidate=>{
      this.candidate = candidate;
    })
    this.saving = false;
    this.years=generateYearArray(1950,true);
    this.examListEnum = Object.keys(Exam).map(key => ({ key, value: Exam[key] }));
    /* Intialise the form */
    const ex = this.exam;
    this.form = this.fb.group({
      id: [ex ? ex.id : null],
      exam: [ex ? ex.exam : null , Validators.required],
      otherExam: [ex ? ex.otherExam : null , Validators.required],
      score: [ex ? ex.score : null , Validators.required],
      year: [ex ? ex.year : null , Validators.required],
      notes: [ex ? ex.notes : null ]
    });
    // Subscribe to changes in the examType control
    this.form.get('exam').valueChanges.subscribe(value => {
      this.toggleOtherExamValidator(value);
    });

    // Set the validator for the otherExam field
    this.toggleOtherExamValidator(this.form.get('exam').value);
  };
  isOtherExamSelected(): boolean {
    return this.form.get('exam').value === 'Other';
  }

  toggleOtherExamValidator(value: string) {
    const otherExamControl = this.form.get('otherExam');
    if (value === 'Other') {
      otherExamControl.setValidators(Validators.required);
    } else {
      otherExamControl.clearValidators();
    }
    otherExamControl.updateValueAndValidity();
  }
  save() {
    this.error = null;
    this.saving = true;

    // If the candidate hasn't changed anything, skip the update service call
    if (this.form.pristine) {
      this.saved.emit(this.exam);
      return;
    }

    if (!this.form.value.id) {
      this.candidateExamService.createCandidateExam(this.candidate.id,this.form.value).subscribe(
        (response) => {
          this.saved.emit(response);
        },
        (error) => {
          this.error = error;
          this.saving = false;
        },
      );
    } else {
      this.candidateExamService.updateCandidateExam(this.exam.id, this.form.value).subscribe(
        (response) => {
          this.saved.emit(response);
        },
        (error) => {
          this.error = error;
          this.saving = false;
        }
      );
    }
  }

}
