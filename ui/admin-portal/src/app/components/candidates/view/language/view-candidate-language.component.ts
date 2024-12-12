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
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {Candidate, hasIeltsExam} from '../../../../model/candidate';
import {CandidateLanguage} from '../../../../model/candidate-language';
import {CandidateLanguageService} from '../../../../services/candidate-language.service';
import {EditCandidateLanguageComponent} from './edit/edit-candidate-language.component';
import {CreateCandidateLanguageComponent} from "./create/create-candidate-language.component";
import {ConfirmationComponent} from "../../../util/confirm/confirmation.component";
import {CandidateService} from "../../../../services/candidate.service";

@Component({
  selector: 'app-view-candidate-language',
  templateUrl: './view-candidate-language.component.html',
  styleUrls: ['./view-candidate-language.component.scss']
})
export class ViewCandidateLanguageComponent implements OnInit {

  @Input() candidate: Candidate;
  @Input() editable: boolean;
  @Input() adminUser: boolean;
  @Input() accordion: boolean = false;

  candidateLanguages: CandidateLanguage[];
  loading: boolean;
  error;

  constructor(private candidateLanguageService: CandidateLanguageService,
              private candidateService: CandidateService,
              private modalService: NgbModal ) {
  }

  ngOnInit() {
  }

  editCandidateLanguage(candidateLanguage: CandidateLanguage) {
    const editCandidateLanguageModal = this.modalService.open(EditCandidateLanguageComponent, {
      centered: true,
      backdrop: 'static'
    });

    editCandidateLanguageModal.componentInstance.candidateLanguage = candidateLanguage;

    editCandidateLanguageModal.result
      .then((candidateLanguage) => this.candidateService.updateCandidate())
      .catch(() => { /* Isn't possible */ });

  }

  createCandidateLanguage() {
    const createCandidateLanguageModal = this.modalService.open(CreateCandidateLanguageComponent, {
      centered: true,
      backdrop: 'static'
    });

    createCandidateLanguageModal.componentInstance.candidateId = this.candidate.id;

    createCandidateLanguageModal.result
      .then((candidateLanguage) => this.candidateService.updateCandidate())
      .catch(() => { /* Isn't possible */ });

  }

  deleteCandidateLanguage(candidateLanguage: CandidateLanguage) {
    const deleteCandidateLanguageModal = this.modalService.open(ConfirmationComponent, {
      centered: true,
      backdrop: 'static'
    });

    deleteCandidateLanguageModal.componentInstance.message = "Are you sure you want to delete this candidate's language?";

    deleteCandidateLanguageModal.result
      .then((result) => {
        if (result === true) {
          this.candidateLanguageService.delete(candidateLanguage.id).subscribe(
            (user) => {
              this.loading = false;
              this.candidateService.updateCandidate()
            },
            (error) => {
              this.error = error;
              this.loading = false;
            });
        }
      })
      .catch(() => { /* Isn't possible */ });
  }

  hasIelts(candidate: Candidate): boolean {
    return hasIeltsExam(candidate);
  }

}
