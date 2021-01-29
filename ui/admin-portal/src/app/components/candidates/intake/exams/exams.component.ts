/*
 * Copyright (c) 2021 Talent Beyond Boundaries.
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
import {Candidate, CandidateExam, CandidateIntakeData} from '../../../../model/candidate';
import {CandidateExamService} from '../../../../services/candidate-exam.service';

@Component({
  selector: 'app-exams',
  templateUrl: './exams.component.html',
  styleUrls: ['./exams.component.scss']
})
export class ExamsComponent implements OnInit {

  @Input() candidate: Candidate;
  @Input() candidateIntakeData: CandidateIntakeData;
  error: boolean;
  saving: boolean;

  constructor(
    private candidateExamService: CandidateExamService
  ) {}

  ngOnInit(): void {
  }

  addRecord() {
    this.saving = true;
    const candidateExam: CandidateExam = {};
    this.candidateExamService.create(this.candidate.id, candidateExam).subscribe(
      (exam) => {
        this.candidateIntakeData.candidateExams.push(exam)
        this.saving = false;
      },
      (error) => {
        this.error = error;
        this.saving = false;
      });
  }

  deleteRecord(i: number) {
    this.candidateIntakeData.candidateExams.splice(i, 1);
  }

}
