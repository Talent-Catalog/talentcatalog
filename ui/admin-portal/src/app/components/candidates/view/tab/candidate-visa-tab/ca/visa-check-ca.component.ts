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
import {Candidate, CandidateIntakeData, CandidateVisa} from '../../../../../../model/candidate';
import {FormGroup} from '@angular/forms';
import {CandidateService} from "../../../../../../services/candidate.service";
import {LocalStorageService} from "angular-2-local-storage";

@Component({
  selector: 'app-visa-check-ca',
  templateUrl: './visa-check-ca.component.html',
  styleUrls: ['./visa-check-ca.component.scss']
})
export class VisaCheckCaComponent implements OnInit {
  @Input() candidate: Candidate;
  @Input() candidateIntakeData: CandidateIntakeData;
  @Input() visaCheckRecord: CandidateVisa;
  form: FormGroup;

  constructor(private candidateService: CandidateService,
              private localStorageService: LocalStorageService) {}

  ngOnInit() {
      this.setSelectedVisaCheckIndex(this.candidateIntakeData?.candidateVisaChecks?.indexOf(this.visaCheckRecord));
  }

  private setSelectedVisaCheckIndex(index: number) {
    this.localStorageService.set('VisaCheckIndex', index);
  }
}
