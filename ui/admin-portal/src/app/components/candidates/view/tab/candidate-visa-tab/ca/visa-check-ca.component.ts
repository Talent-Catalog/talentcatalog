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
import {
  Candidate,
  CandidateIntakeData,
  CandidateVisa,
  CandidateVisaJobCheck
} from '../../../../../../model/candidate';

@Component({
  selector: 'app-visa-check-ca',
  templateUrl: './visa-check-ca.component.html',
  styleUrls: ['./visa-check-ca.component.scss']
})
export class VisaCheckCaComponent implements OnInit {
  @Input() candidate: Candidate;
  @Input() candidateIntakeData: CandidateIntakeData;
  @Input() visaCheckRecord: CandidateVisa;
  selectedJob: CandidateVisaJobCheck

  constructor() {}

  ngOnInit() {
    /**
     * Default select the first job in the array on init. This gets changed and updated via
     * two-way data binding of selectedJob on the CandidateVisaJobComponent.
     */
    this.selectedJob = this.visaCheckRecord.candidateVisaJobChecks[0]
  }
}
