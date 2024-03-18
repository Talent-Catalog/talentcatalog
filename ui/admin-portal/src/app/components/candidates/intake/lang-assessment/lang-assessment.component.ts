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

import {Component, OnInit} from '@angular/core';
import {FormBuilder} from '@angular/forms';
import {CandidateService} from '../../../../services/candidate.service';
import {IntakeComponentBase} from '../../../util/intake/IntakeComponentBase';

@Component({
  selector: 'app-lang-assessment',
  templateUrl: './lang-assessment.component.html',
  styleUrls: ['./lang-assessment.component.scss']
})
export class LangAssessmentComponent extends IntakeComponentBase implements OnInit {

  errorMsg: string;
  regexpIeltsScore: RegExp;

  constructor(fb: FormBuilder, candidateService: CandidateService) {
    super(fb, candidateService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      englishAssessment: [this.candidateIntakeData?.englishAssessment],
      englishAssessmentScore: [this.candidateIntakeData?.englishAssessmentScore],
      frenchAssessment: [this.candidateIntakeData?.frenchAssessment],
      frenchAssessmentScore: [this.candidateIntakeData?.frenchAssessmentScore],
    });
    this.regexpIeltsScore = new RegExp('^([0-8](\\.5)?$)|(^9$)');
    this.errorMsg = "The IELTS score must be between 0-9 and with decimal increments of .5 only."
  }

}
