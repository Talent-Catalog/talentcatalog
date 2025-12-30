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
  calculateAge,
  Candidate,
  CandidateIntakeData,
  CandidateVisa,
  CandidateVisaJobCheck,
  describeFamilyInDestination,
  getIeltsScoreTypeString
} from "../../../../../../../model/candidate";
import {OccupationService} from "../../../../../../../services/occupation.service";
import {
  CandidateOccupationService
} from "../../../../../../../services/candidate-occupation.service";
import {CandidateOccupation} from "../../../../../../../model/candidate-occupation";
import {CandidateEducationService} from "../../../../../../../services/candidate-education.service";
import {CandidateEducation} from "../../../../../../../model/candidate-education";
import {Occupation} from "../../../../../../../model/occupation";
import {TcAccordionComponent} from "../../../../../../../shared/components/accordion/tc-accordion.component";
import {CandidateOpportunity} from "../../../../../../../model/candidate-opportunity";

@Component({
  selector: 'app-visa-job-check-au',
  templateUrl: './visa-job-check-au.component.html',
  styleUrls: ['./visa-job-check-au.component.scss']
})
export class VisaJobCheckAuComponent implements OnInit, AfterViewInit {
  @Input() selectedJobCheck: CandidateVisaJobCheck;
  @Input() candidate: Candidate;
  @Input() candidateIntakeData: CandidateIntakeData;
  @Input() visaCheckRecord: CandidateVisa;

  @ViewChild('visaJobAus') visaJobAus: TcAccordionComponent;

  candOccupations: CandidateOccupation[];
  candQualifications: CandidateEducation[];
  occupations: Occupation[];
  yrsExp: CandidateOccupation;
  familyInAus: string;
  candidateAge: number;
  candidateOpportunity: CandidateOpportunity;

  error: string;

  constructor(private candidateEducationService: CandidateEducationService,
              private candidateOccupationService: CandidateOccupationService,
              private occupationService: OccupationService) {}

  ngOnInit() {
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

    // Get the list of all occupations
    this.occupationService.listOccupations().subscribe(
      (results) => {
        this.occupations = results;
      }
    )

    this.familyInAus = describeFamilyInDestination(this.visaCheckRecord);
    const dobDate = new Date(this.candidate.dob);
    this.candidateAge = calculateAge(dobDate);
    this.candidateOpportunity = this.candidate.candidateOpportunities
      .find(co => co.jobOpp.id == this.selectedJobCheck.jobOpp.id);
  }

  ngAfterViewInit() {
    if(this.visaJobAus){
      this.visaJobAus.openAll();
    }
  }

  get ieltsScoreType(): string {
    return getIeltsScoreTypeString(this.candidate);
  }

}
