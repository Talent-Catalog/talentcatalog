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

import {Component, EventEmitter, OnInit, Output} from '@angular/core';
import {IntakeComponentBase} from '../../../../util/intake/IntakeComponentBase';
import {EnumOption, enumOptions} from '../../../../../util/enum';
import {CandidateExam, Exam} from '../../../../../model/candidate';
import {UntypedFormBuilder} from '@angular/forms';
import {CandidateService} from '../../../../../services/candidate.service';
import {CandidateExamService} from '../../../../../services/candidate-exam.service';

import {generateYearArray} from '../../../../../util/year-helper';

@Component({
  selector: 'app-candidate-exam-card',
  templateUrl: './candidate-exam-card.component.html',
  styleUrls: ['./candidate-exam-card.component.scss']
})
export class CandidateExamCardComponent extends IntakeComponentBase implements OnInit {

  @Output() delete = new EventEmitter();

  //Drop down values for enumeration
  examOptions: EnumOption[] = enumOptions(Exam);
  years: number[];
  errorMsg: string;
  regexpIeltsScore: RegExp;

  constructor(fb: UntypedFormBuilder, candidateService: CandidateService,
              private candidateExamService: CandidateExamService) {
    super(fb, candidateService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      examId: [this.myRecord?.id],
      examType: [this.myRecord?.exam],
      otherExam: [this.myRecord?.otherExam],
      examScore: [this.myRecord?.score],
      examYear: [this.myRecord?.year],
      examNotes: [this.myRecord?.notes],
    });

    this.years = generateYearArray(1950, true);

    this.regexpIeltsScore = new RegExp('^([0-8](\\.5)?$)|(^9$)');
    this.errorMsg = "The IELTS score must be between 0-9 and with decimal increments of .5 only.";

    this.form.controls['examScore']?.valueChanges.subscribe(score => {
        this.candidateIntakeData.candidateExams[this.myRecordIndex].score = score;
    });

    this.form.controls['examType']?.valueChanges.subscribe(type => {
        this.candidateIntakeData.candidateExams[this.myRecordIndex].exam = type;
    });

    this.form.controls['otherExam']?.valueChanges.subscribe(other => {
        this.candidateIntakeData.candidateExams[this.myRecordIndex].otherExam = other;
    });

  }

  get type() {
    return this.form?.controls?.examType?.value;
  }

  get isOtherExam(): boolean {
    let other: boolean = false;
    if (this.form?.value) {
      if (this.form.value.examType === 'Other') {
        other = true;
      }
    }
    return other;
  }

  get hasSelectedExam(): boolean {
    let found: boolean = false;
    if (this.form?.value) {
      if (this.form.value.examType !== null) {
        found = true;
      } else if (this.form.value.examType === '') {
        found = true
      }
    }
    return found;
  }


  private get myRecord(): CandidateExam {
    return this.candidateIntakeData.candidateExams ?
      this.candidateIntakeData.candidateExams[this.myRecordIndex]
      : null;
  }

  doDelete() {
    this.candidateExamService.delete(this.myRecord.id)
      .subscribe(
        ret => {
        },
        error => {
          this.error = error;
        }
      );
    this.delete.emit();
  }

}
