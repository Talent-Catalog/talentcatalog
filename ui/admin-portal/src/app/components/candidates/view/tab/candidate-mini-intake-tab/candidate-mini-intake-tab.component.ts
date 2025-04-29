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

import {Component} from '@angular/core';
import {IntakeComponentTabBase} from '../../../../util/intake/IntakeComponentTabBase';
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {CandidateService} from "../../../../../services/candidate.service";
import {CountryService} from "../../../../../services/country.service";
import {EducationLevelService} from "../../../../../services/education-level.service";
import {OccupationService} from "../../../../../services/occupation.service";
import {LanguageLevelService} from "../../../../../services/language-level.service";
import {CandidateNoteService} from "../../../../../services/candidate-note.service";
import {CandidateExamService, CreateCandidateExamRequest} from "../../../../../services/candidate-exam.service";
import {
  CandidateCitizenshipService,
  CreateCandidateCitizenshipRequest
} from "../../../../../services/candidate-citizenship.service";
import {AuthenticationService} from "../../../../../services/authentication.service";
import {AuthorizationService} from "../../../../../services/authorization.service";
import {EditCandidateContactComponent} from "../../contact/edit/edit-candidate-contact.component";

@Component({
  selector: 'app-candidate-mini-intake-tab',
  templateUrl: './candidate-mini-intake-tab.component.html',
  styleUrls: ['./candidate-mini-intake-tab.component.scss']
})
export class CandidateMiniIntakeTabComponent extends IntakeComponentTabBase {
  clickedOldIntake: boolean;
  constructor(candidateService: CandidateService,
              countryService: CountryService,
              educationLevelService: EducationLevelService,
              occupationService: OccupationService,
              languageLevelService: LanguageLevelService,
              noteService: CandidateNoteService,
              authenticationService: AuthenticationService,
              modalService: NgbModal,
              private authorizationService: AuthorizationService,
              private candidateCitizenshipService: CandidateCitizenshipService,
              private candidateExamService: CandidateExamService) {
    super(candidateService, countryService, educationLevelService, occupationService, languageLevelService, noteService, authenticationService, modalService)
  }

  get miniIntakeComplete() {
    return this.candidate.miniIntakeCompletedDate != null;
  }

  get miniIntakeCompletedBy() {
    let user: string = null;
    if (this.miniIntakeComplete) {
      if (this.candidate.miniIntakeCompletedBy != null) {
        user = this.candidate?.miniIntakeCompletedBy.firstName + " " + this.candidate?.miniIntakeCompletedBy.lastName;
      } else {
        user = "external intake input, see notes for more details."
      }
    }
    return user;
  }

  isPalestinian(): boolean {
    return this.countryService.isPalestine(this.candidate?.nationality)
  }

  addExamRecord(e: MouseEvent) {
    e.stopPropagation();
    this.saving = true;
    const request: CreateCandidateExamRequest = {};
    this.candidateExamService.create(this.candidate.id, request).subscribe(
      (exam) => {
        this.candidateIntakeData.candidateExams.unshift(exam)
        this.saving = false;
      },
      (error) => {
        this.error = error;
        this.saving = false;
      });
  }

  addCitizenshipRecord(e: MouseEvent) {
    // Stop the button from opening/closing the accordion
    e.stopPropagation();
    this.saving = true;
    const request: CreateCandidateCitizenshipRequest = {};
    this.candidateCitizenshipService.create(this.candidate.id, request).subscribe(
      (citizenship) => {
        this.candidateIntakeData.candidateCitizenships.unshift(citizenship)
        this.saving = false;
      },
      (error) => {
        this.error = error;
        this.saving = false;
      });
  }

  isEditable(): boolean {
    return this.authorizationService.isEditableCandidate(this.candidate);
  }

  isReadOnly(): boolean {
    return this.authorizationService.isReadOnly();
  }

  editContactDetails(event: MouseEvent) {
    event.stopPropagation(); // Stop the click event from opening/closing the accordion
    const editCandidateModal = this.modalService.open(EditCandidateContactComponent, {
      centered: true,
      backdrop: 'static'
    });

    editCandidateModal.componentInstance.candidateId = this.candidate.id;

    editCandidateModal.result
    .then((candidate) => this.candidate = candidate)
    .catch(() => { /* Isn't possible */ });
  }

  hasIssues() {
    let issues: String[] = [];
    if (this.candidateIntakeData?.healthIssues == "Yes") {
      issues.push("Health Issues");
    }
    if (this.candidateIntakeData?.crimeConvict == "Yes") {
      issues.push("Crime conviction")
    }
    if (this.candidateIntakeData?.arrestImprison == "Yes") {
      issues.push("Arrest/Imprisoned")
    }
    if (this.candidateIntakeData?.conflict == "Yes") {
      issues.push("Conflict")
    }
    return issues.join(", ");
  }
}
