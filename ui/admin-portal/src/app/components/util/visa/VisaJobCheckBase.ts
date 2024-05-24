/*
 * Copyright (c) 2024 Talent Beyond Boundaries.
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

import {AfterViewInit, Directive, Input, OnInit, ViewChild} from '@angular/core';
import {
  Candidate,
  CandidateIntakeData,
  CandidateVisa,
  CandidateVisaJobCheck,
  getDestinationPathwayInfoLink
} from "../../../model/candidate";
import {CandidateEducationService} from "../../../services/candidate-education.service";
import {CandidateOccupationService} from "../../../services/candidate-occupation.service";
import {CandidateOccupation} from "../../../model/candidate-occupation";
import {CandidateEducation} from "../../../model/candidate-education";
import {NgbAccordion} from "@ng-bootstrap/ng-bootstrap";
import {describeFamilyInDestination} from "../../../model/candidate-destination";

/**
 * Base class for all countries parent Visa Job component. They all share similarities in:
 * - Having Update SF relocating dependants button
 * - Accordion defaulting to expanded
 * - Loading certain dropdowns
 */
@Directive()
export abstract class VisaJobCheckBase implements OnInit, AfterViewInit {
  @Input() selectedJobCheck: CandidateVisaJobCheck;
  @Input() candidate: Candidate;
  @Input() candidateIntakeData: CandidateIntakeData;
  @Input() visaCheckRecord: CandidateVisa;

  @ViewChild('visaJobAcc') visaJobAcc: NgbAccordion;

  candOccupations: CandidateOccupation[];
  candQualifications: CandidateEducation[];
  pathwaysInfoLink: string;
  family: string;

  error: string;
  loading: boolean;

  public constructor(protected candidateEducationService: CandidateEducationService,
                     protected candidateOccupationService: CandidateOccupationService) {}

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
    this.family = describeFamilyInDestination(this.visaCheckRecord?.country.id, this.candidateIntakeData);
    this.pathwaysInfoLink = getDestinationPathwayInfoLink(this.visaCheckRecord.country.id);
  }

  /**
   * Set all accordion panels to default to open for visa job accordion.
   */
  ngAfterViewInit() {
    this.visaJobAcc.expandAll();
  }
}
