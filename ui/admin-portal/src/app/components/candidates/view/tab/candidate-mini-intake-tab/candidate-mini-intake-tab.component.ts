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

import {Component} from '@angular/core';
import {IntakeComponentTabBase} from '../../../../util/intake/IntakeComponentTabBase';
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {
  OldIntakeInputComponent
} from "../../../../util/old-intake-input-modal/old-intake-input.component";
import {CandidateService} from "../../../../../services/candidate.service";
import {CountryService} from "../../../../../services/country.service";
import {EducationLevelService} from "../../../../../services/education-level.service";
import {OccupationService} from "../../../../../services/occupation.service";
import {LanguageLevelService} from "../../../../../services/language-level.service";
import {CandidateNoteService} from "../../../../../services/candidate-note.service";
import {
  CandidateExamService,
  CreateCandidateExamRequest
} from "../../../../../services/candidate-exam.service";
import {
  CandidateCitizenshipService,
  CreateCandidateCitizenshipRequest
} from "../../../../../services/candidate-citizenship.service";
import {AuthenticationService} from "../../../../../services/authentication.service";

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
              private candidateCitizenshipService: CandidateCitizenshipService,
              private candidateExamService: CandidateExamService) {
    super(candidateService, countryService, educationLevelService, occupationService, languageLevelService, noteService, authenticationService, modalService)
  }

  public inputOldIntakeNote(formName: string, button) {
    this.clickedOldIntake = true;
    // Popup modal to gather who and when.
    const oldIntakeInputModal = this.modalService.open(OldIntakeInputComponent, {
      centered: true,
      backdrop: 'static'
    });

    oldIntakeInputModal.componentInstance.candidateId = this.candidate.id;
    oldIntakeInputModal.componentInstance.formName = formName;

    oldIntakeInputModal.result
      .then((country) => button.textContent = 'Note created!')
      .catch(() => { /* Isn't possible */
      });
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

}
