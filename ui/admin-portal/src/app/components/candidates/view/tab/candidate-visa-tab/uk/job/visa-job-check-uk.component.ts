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

import {AfterViewInit, Component, Input, OnInit, ViewChild} from '@angular/core';
import {
  Candidate,
  CandidateIntakeData,
  CandidateVisa,
  CandidateVisaJobCheck
} from "../../../../../../../model/candidate";
import {NgbAccordion} from "@ng-bootstrap/ng-bootstrap";
import {CandidateOpportunity} from "../../../../../../../model/candidate-opportunity";

@Component({
  selector: 'app-visa-job-check-uk',
  templateUrl: './visa-job-check-uk.component.html',
  styleUrls: ['./visa-job-check-uk.component.scss']
})
export class VisaJobCheckUkComponent implements OnInit, AfterViewInit {
  @Input() selectedJobCheck: CandidateVisaJobCheck;
  @Input() candidate: Candidate;
  @Input() candidateIntakeData: CandidateIntakeData;
  @Input() visaCheckRecord: CandidateVisa;

  @ViewChild('visaJobUk') visaJobUk: NgbAccordion;

  candidateOpportunity: CandidateOpportunity;

  error: string;

  constructor() {}

  ngOnInit() {
    this.candidateOpportunity = this.candidate.candidateOpportunities
      .find(co => co.jobOpp.id == this.selectedJobCheck.jobOpp.id);
  }

  ngAfterViewInit() {
    if(this.visaJobUk){
      this.visaJobUk.expandAll();
    }
  }
}
