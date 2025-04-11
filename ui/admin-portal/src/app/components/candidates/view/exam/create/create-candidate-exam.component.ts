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

import {Component, OnInit} from '@angular/core';
import {UntypedFormBuilder, UntypedFormGroup, Validators} from "@angular/forms";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {CandidateExam, Exam} from "../../../../../model/candidate";
import {CandidateExamService, CreateCandidateExamRequest} from "../../../../../services/candidate-exam.service";
import {EnumOption, enumOptions} from "../../../../../util/enum";
import {generateYearArray} from "../../../../../util/year-helper";

@Component({
  selector: 'app-create-candidate-exam',
  templateUrl: './create-candidate-exam.component.html',
  styleUrls: ['./create-candidate-exam.component.scss']
})
export class CreateCandidateExamComponent implements OnInit {
  candidateCertification: CandidateExam;
  candidateForm: UntypedFormGroup;

  candidateId: number;
  countries = [];
  years = [];
  error;
  loading: boolean;
  saving: boolean;
  typing: boolean;

  examOptions: EnumOption[] = enumOptions(Exam);

  constructor(private activeModal: NgbActiveModal,
              private fb: UntypedFormBuilder,
              private candidateExamService: CandidateExamService) {
  }
  ngOnInit() {
    this.loading = true;

    this.years = generateYearArray(1950, true);

    this.candidateForm = this.fb.group({
      exam: ['', [Validators.required]],
      otherExam: ['', null],
      score: ['', [Validators.required]],
      year: ['', [Validators.required]],
      notes: ['']
    });

    // Subscribe to changes in the 'exam' field to toggle validation on 'otherExam'
    this.candidateForm.get('exam').valueChanges.subscribe((selectedExam) => {
      this.toggleOtherExamValidation(selectedExam);
    });

    this.loading = false;
  }

  onSave() {
    this.saving = true;
    const request: CreateCandidateExamRequest = {
      exam: this.candidateForm.value.exam,
      otherExam: this.candidateForm.value.otherExam,
      score: this.candidateForm.value.score,
      year: this.candidateForm.value.year,
      notes: this.candidateForm.value.notes,
    }
    this.candidateExamService.create(this.candidateId,request).subscribe(
      (candidateExam) => {
        this.closeModal(candidateExam);
        this.saving = false;
      },
      (error) => {
        this.error = error;
        this.saving = false;
      });
  }

  closeModal(candidateExam: CandidateExam) {
    this.activeModal.close(candidateExam);
  }

  dismiss() {
    this.activeModal.dismiss(false);
  }

  get isOtherExam(): boolean {
    let other: boolean = false;
    if (this.candidateForm?.value) {
      if (this.candidateForm.value.exam === 'Other') {
        other = true;
      }
    }
    return other;
  }

  get hasSelectedExam(): boolean {
    let found: boolean = false;
    if (this.candidateForm?.value) {
      if (this.candidateForm.value.exam !== null) {
        found = true;
      } else if (this.candidateForm.value.exam === '') {
        found = true
      }
    }
    return found;
  }

  toggleOtherExamValidation(selectedExam: string) {
    const otherExamControl = this.candidateForm.get('otherExam');

    if (selectedExam === 'Other') {
      otherExamControl.setValidators([Validators.required]);
    } else {
      otherExamControl.clearValidators();
    }

    otherExamControl.updateValueAndValidity();
  }
}
