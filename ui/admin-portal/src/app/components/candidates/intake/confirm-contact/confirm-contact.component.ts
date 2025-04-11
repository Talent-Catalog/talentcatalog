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
import {Candidate, CandidateIntakeData} from '../../../../model/candidate';
import {dateString} from '../../../../util/date-adapter/date-adapter';

@Component({
  selector: 'app-confirm-contact',
  templateUrl: './confirm-contact.component.html',
  styleUrls: ['./confirm-contact.component.scss']
})
export class ConfirmContactComponent implements OnInit {
  @Input() candidate: Candidate;
  @Input() candidateIntakeData: CandidateIntakeData;

  constructor() { }

  ngOnInit(): void {
  }

  get date(): string {
    if (!this.candidate?.dob) {
      return 'Date of birth not provided';
    }

    let dobString = dateString(this.candidate.dob);
    const dobDate = new Date(this.candidate.dob);
    if (!Number.isNaN(dobDate.getTime())) { // Checks if the date is valid
      dobString += ' (Age ' + this.calculateAge(dobDate) + ')';
    }
    return dobString;
  }

  get gender(): string {
    const gender = this.candidate?.gender?.trim();
    if (!gender) {
      return 'No gender specified';
    }
    return this.capitaliseFirstLetter(gender);
  }

  candidateSurveyAnswer(): string {
    let answer = this.candidate.surveyType?.name;
    if (this.candidate.surveyComment) {
      answer += ' ' + this.candidate.surveyComment;
    }
    return answer;
  }

  private calculateAge(dob: Date): number {
    const currentDate = new Date();
    const currentYear = currentDate.getFullYear();
    const currentMonth = currentDate.getMonth();
    const currentDay = currentDate.getDate();

    const birthYear = dob.getFullYear();
    const birthMonth = dob.getMonth();
    const birthDay = dob.getDate();

    let age = currentYear - birthYear;
    if (currentMonth < birthMonth || (currentMonth === birthMonth && currentDay < birthDay)) {
      age--;
    }

    return age;
  }

  private capitaliseFirstLetter(text: string): string {
    return text && text.length > 0 ? text.charAt(0).toUpperCase() + text.slice(1) : '';
  }

}
