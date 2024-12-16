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
import {CandidateVisaJobService} from "../../../../../../services/candidate-visa-job.service";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {LocalStorageService} from "../../../../../../services/local-storage.service";

@Component({
  selector: 'app-visa-check-au',
  templateUrl: './visa-check-au.component.html',
  styleUrls: ['./visa-check-au.component.scss']
})
export class VisaCheckAuComponent implements OnInit {

  @Input() candidate: Candidate;
  @Input() candidateIntakeData: CandidateIntakeData;
  @Input() visaCheckRecord: CandidateVisa;
  selectedJob: CandidateVisaJobCheck

  currentYear: string;
  birthYear: string;

  constructor(private candidateVisaJobService: CandidateVisaJobService,
              private modalService: NgbModal,
              private localStorageService: LocalStorageService) {}

  ngOnInit() {
    this.currentYear = new Date().getFullYear().toString();
    this.birthYear = this.candidate?.dob?.toString().slice(0, 4);
    /**
     * Default select the first job in the array on init. This gets changed and updated via
     * two-way data binding of selectedJob on the CandidateVisaJobComponent.
     */
    this.selectedJob = this.visaCheckRecord.candidateVisaJobChecks[0]
  }
}
