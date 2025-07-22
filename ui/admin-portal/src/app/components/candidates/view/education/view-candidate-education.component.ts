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
import {Candidate} from "../../../../model/candidate";
import {CandidateEducation} from "../../../../model/candidate-education";
import {CandidateEducationService} from "../../../../services/candidate-education.service";
import {EditCandidateEducationComponent} from "./edit/edit-candidate-education.component";
import {CreateCandidateEducationComponent} from "./create/create-candidate-education.component";
import {ConfirmationComponent} from "../../../util/confirm/confirmation.component";
import {CandidateService} from "../../../../services/candidate.service";
import {EditMaxEducationLevelComponent} from "./edit--candidate-max-education-level/edit-candidate-max-education-level";

@Component({
  selector: 'app-view-candidate-education',
  templateUrl: './view-candidate-education.component.html',
  styleUrls: ['./view-candidate-education.component.scss']
})
export class ViewCandidateEducationComponent implements OnInit {

  @Input() candidate: Candidate;
  @Input() editable: boolean;
  @Input() adminUser: boolean;

  candidateEducations: CandidateEducation[];
  loading: boolean;
  error;

  constructor(private candidateEducationService: CandidateEducationService,
              private candidateService: CandidateService,
              private modalService: NgbModal ) {
  }

  ngOnInit() {
  }

  editMaxEducationLevel() {
    const modalRef = this.modalService.open(EditMaxEducationLevelComponent, {
      centered: true,
      backdrop: 'static'
    });

    modalRef.componentInstance.currentLevel = this.candidate.maxEducationLevel;
    modalRef.result.then((newLevel) => {
        if (newLevel && newLevel.id) {
          const candidatePayload = {
            maxEducationLevel: newLevel.id
          };

          this.candidateService.updateMaxEducationLevel(this.candidate.id, candidatePayload).subscribe(() => {
            this.candidateService.updateCandidate(); // refresh candidate
          });
        }
    }).catch(() => { /* dismissed */ });
  }


  editCandidateEducation(candidateEducation: CandidateEducation) {
    const editCandidateEducationModal = this.modalService.open(EditCandidateEducationComponent, {
      centered: true,
      backdrop: 'static'
    });

    editCandidateEducationModal.componentInstance.candidateEducation = candidateEducation;

    editCandidateEducationModal.result
      .then((candidateEducation) => this.candidateService.updateCandidate())
      .catch(() => { /* Isn't possible */ });

  }

  createCandidateEducation() {
    const createCandidateEducationModal = this.modalService.open(CreateCandidateEducationComponent, {
      centered: true,
      backdrop: 'static'
    });

    createCandidateEducationModal.componentInstance.candidateId = this.candidate.id;

    createCandidateEducationModal.result
      .then((candidateEducation) => this.candidateService.updateCandidate())
      .catch(() => { /* Isn't possible */ });

  }

  deleteCandidateEducation(candidateEducation: CandidateEducation) {
    const deleteCandidateEducationModal = this.modalService.open(ConfirmationComponent, {
      centered: true,
      backdrop: 'static'
    });

    deleteCandidateEducationModal.componentInstance.message = 'Are you sure you want to delete this education?';

    deleteCandidateEducationModal.result
      .then((result) => {
        if (result === true) {
          this.candidateEducationService.delete(candidateEducation.id).subscribe(
            (user) => {
              this.loading = false;
              this.candidateService.updateCandidate();
            },
            (error) => {
              this.error = error;
              this.loading = false;
            });
        }
      })
      .catch(() => { /* Isn't possible */ });
  }

}
