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

import {
  Component,
  EventEmitter,
  Input,
  Output,
} from '@angular/core';
import {CandidateExam} from '../../../model/candidate';
import {Exam} from "../../../model/candidate";
import {generateYearArray} from "../../../util/year-helper";

@Component({
  selector: 'app-candidate-exam-card',
  templateUrl: './candidate-exam-card.component.html',
  styleUrls: ['./candidate-exam-card.component.scss']
})
export class CandidateExamCardComponent {

  @Input() exam: CandidateExam;
  @Input() preview: boolean = false;
  @Input() disabled: boolean = false;
  @Output() onDelete = new EventEmitter<CandidateExam>();
  @Output() onEdit = new EventEmitter<CandidateExam>();

  years:number[];

  constructor() {
    this.years=generateYearArray(1950,true);
  }

  deleteExam() {
    this.onDelete.emit(this.exam);
  }

  editExam() {
    this.onEdit.emit(this.exam);
  }
  getExamName(exam: string): string {
    return Exam[exam as keyof typeof Exam] || 'Unknown';
  }
}
