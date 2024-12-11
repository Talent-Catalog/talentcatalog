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
  CandidateVisaJobCheck,
  describeFamilyInDestination,
  getDestinationPathwayInfoLink,
  IeltsStatus
} from "../../../../../../../model/candidate";
import {CandidateEducationService} from "../../../../../../../services/candidate-education.service";
import {
  CandidateOccupationService
} from "../../../../../../../services/candidate-occupation.service";
import {CandidateOccupation} from "../../../../../../../model/candidate-occupation";
import {CandidateEducation} from "../../../../../../../model/candidate-education";
import {NgbAccordion} from "@ng-bootstrap/ng-bootstrap";
import {CandidateOpportunity} from "../../../../../../../model/candidate-opportunity";

@Component({
  selector: 'app-visa-job-check-ca',
  templateUrl: './visa-job-check-ca.component.html',
  styleUrls: ['./visa-job-check-ca.component.scss']
})
export class VisaJobCheckCaComponent implements OnInit, AfterViewInit {
  @Input() selectedJobCheck: CandidateVisaJobCheck;
  @Input() candidate: Candidate;
  @Input() candidateIntakeData: CandidateIntakeData;
  @Input() visaCheckRecord: CandidateVisa;

  @ViewChild('visaJobCanada') visaJobCanada: NgbAccordion;

  candOccupations: CandidateOccupation[];
  candQualifications: CandidateEducation[];

  familyInCanada: string;
  partnerIeltsString: string;
  pathwaysInfoLink: string;
  candidateOpportunity: CandidateOpportunity;

  error: string;
  loading: boolean;

  constructor(private candidateEducationService: CandidateEducationService,
              private candidateOccupationService: CandidateOccupationService) {}

  ngOnInit(): void {
    // Get the candidate occupations
    this.candidateOccupationService.get(this.candidate.id).subscribe(
      (response) => {
        this.candOccupations = response;
      }, (error) => {
        this.error = error;
      }
    )
    // Get the candidate qualifications
    this.candidateEducationService.list(this.candidate.id).subscribe(
      (response) => {
        this.candQualifications = response;
      }, (error) => {
        this.error = error;
      }
    )

    // Process & fetch values that need to be displayed.
    this.familyInCanada = describeFamilyInDestination(this.visaCheckRecord);
    if (this.candidateIntakeData?.partnerIelts) {
      this.partnerIeltsString = IeltsStatus[this.candidateIntakeData?.partnerIelts] +
        (this.candidateIntakeData?.partnerIeltsScore ? ', Score: ' + this.candidateIntakeData.partnerIeltsScore : null);
    } else {
      this.partnerIeltsString = null;
    }
    this.pathwaysInfoLink = getDestinationPathwayInfoLink(this.visaCheckRecord.country.id);
    this.candidateOpportunity = this.candidate.candidateOpportunities
      .find(co => co.jobOpp.id == this.selectedJobCheck.jobOpp.id);
  }

  ngAfterViewInit() {
    if(this.visaJobCanada){
      this.visaJobCanada.expandAll();
    }
  }
}




