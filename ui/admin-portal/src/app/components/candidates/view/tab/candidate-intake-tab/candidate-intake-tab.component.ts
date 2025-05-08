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
import {IntakeComponentTabBase} from "../../../../util/intake/IntakeComponentTabBase";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {CandidateService} from "../../../../../services/candidate.service";
import {CountryService} from "../../../../../services/country.service";
import {EducationLevelService} from "../../../../../services/education-level.service";
import {OccupationService} from "../../../../../services/occupation.service";
import {LanguageLevelService} from "../../../../../services/language-level.service";
import {CandidateNoteService} from "../../../../../services/candidate-note.service";
import {
  CandidateCitizenshipService,
  CreateCandidateCitizenshipRequest
} from "../../../../../services/candidate-citizenship.service";
import {CandidateExamService, CreateCandidateExamRequest} from "../../../../../services/candidate-exam.service";
import {
  CandidateDependantService,
  CreateCandidateDependantRequest
} from "../../../../../services/candidate-dependant.service";
import {AuthenticationService} from "../../../../../services/authentication.service";
import {calculateAge} from "../../../../../model/candidate";
import {AuthorizationService} from "../../../../../services/authorization.service";

@Component({
  selector: 'app-candidate-intake-tab',
  templateUrl: './candidate-intake-tab.component.html',
  styleUrls: ['./candidate-intake-tab.component.scss']
})
export class CandidateIntakeTabComponent extends IntakeComponentTabBase {
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
              private candidateExamService: CandidateExamService,
              private candidateDependantService: CandidateDependantService) {
    super(candidateService, countryService, educationLevelService, occupationService, languageLevelService, noteService, authenticationService, modalService)
  }

  get fullIntakeComplete() {
    return this.candidate.fullIntakeCompletedDate != null;
  }

  get fullIntakeCompletedBy() {
    let user: string = null;
    if (this.fullIntakeComplete) {
      if (this.candidate.fullIntakeCompletedBy != null) {
        user = this.candidate?.fullIntakeCompletedBy.firstName + " " + this.candidate?.fullIntakeCompletedBy.lastName;
      } else {
        user = "external intake input, see notes for more details."
      }
    }
    return user;
  }

  isPalestinian(): boolean {
    return this.countryService.isPalestine(this.candidate?.nationality)
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

  addDependantRecord(e: MouseEvent) {
    e.stopPropagation();
    const request: CreateCandidateDependantRequest = {};
    this.candidateDependantService.create(this.candidate.id, request).subscribe(
      (dependant) => {
        this.candidateIntakeData?.candidateDependants.unshift(dependant)
        this.saving = false;
      },
      (error) => {
        this.error = error;
        this.saving = false;
      });
  }

  getAge(dob: string) {
    let dobDate = new Date(dob);
    if (!Number.isNaN(dobDate.getTime())) {
      return calculateAge(dobDate);
    } else {
      return 'no DOB'
    }
  }

  getGender(gender: string) {
    return gender ? gender.slice(0,1).toUpperCase() : "";
  }

  hasDependantHealthIssues() {
    let health: boolean = false;
    for (let dep of this.candidateIntakeData?.candidateDependants) {
      if (dep.healthConcern == "Yes") {
        health = true;
        break;
      }
    }
    return health;
  }


  isReadOnly() {
    return this.authenticationService.getLoggedInUser().readOnly;
  }

  isEditable(): boolean {
    return this.authorizationService.isEditableCandidate(this.candidate);
  }

}
