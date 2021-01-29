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
    return dateString(this.candidate.dob)
  }

}
