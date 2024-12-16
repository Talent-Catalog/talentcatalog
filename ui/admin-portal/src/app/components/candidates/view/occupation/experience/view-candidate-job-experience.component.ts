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

import {
  Component,
  EventEmitter,
  Input,
  OnChanges,
  OnInit,
  Output,
  SimpleChanges
} from '@angular/core';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {Candidate} from '../../../../../model/candidate';
import {CandidateOccupation} from '../../../../../model/candidate-occupation';
import {CandidateJobExperience} from '../../../../../model/candidate-job-experience';
import {
  CandidateJobExperienceService
} from '../../../../../services/candidate-job-experience.service';
import {EditCandidateJobExperienceComponent} from './edit/edit-candidate-job-experience.component';
import {
  CreateCandidateJobExperienceComponent
} from './create/create-candidate-job-experience.component';
import {UntypedFormGroup} from '@angular/forms';
import {SearchResults} from '../../../../../model/search-results';
import {EditCandidateOccupationComponent} from '../edit/edit-candidate-occupation.component';
import {ConfirmationComponent} from "../../../../util/confirm/confirmation.component";
import {isHtml} from "../../../../../util/string";
import {CandidateService} from "../../../../../services/candidate.service";

@Component({
  selector: 'app-view-candidate-job-experience',
  templateUrl: './view-candidate-job-experience.component.html',
  styleUrls: ['./view-candidate-job-experience.component.scss']
})
export class ViewCandidateJobExperienceComponent implements OnInit, OnChanges {

  @Input() candidate: Candidate;
  @Input() editable: boolean;
  @Input() adminUser: boolean;
  @Input() candidateOccupation: CandidateOccupation;
  @Output() deleteOccupation = new EventEmitter<CandidateOccupation>();

  candidateJobExperienceForm: UntypedFormGroup;
  loading: boolean;
  expanded: boolean;
  error;
  results: SearchResults<CandidateJobExperience>;
  experiences: CandidateJobExperience[];

  constructor(private candidateJobExperienceService: CandidateJobExperienceService,
              private modalService: NgbModal,
              private candidateService: CandidateService) {
  }

  ngOnInit() {
  }

  ngOnChanges(changes: SimpleChanges) {
    this.expanded = false;
    this.experiences = this.candidate?.candidateJobExperiences.filter(je => je.candidateOccupation?.id == this.candidateOccupation?.id);
  }

  editOccupation() {
    const modal = this.modalService.open(EditCandidateOccupationComponent, {
      centered: true,
      backdrop: 'static'
    });

    modal.componentInstance.candidateOccupation = this.candidateOccupation;

    modal.result
      .then((candidateOccupation) => this.candidateService.updateCandidate())
      .catch(() => { /* Isn't possible */
      });

    this.candidateJobExperienceForm.controls['candidateOccupationId'].patchValue(this.candidateOccupation.id);
  }

  createCandidateJobExperience() {
    const createCandidateJobExperienceModal = this.modalService.open(CreateCandidateJobExperienceComponent, {
      centered: true,
      backdrop: 'static'
    });

    createCandidateJobExperienceModal.componentInstance.candidateOccupationId = this.candidateOccupation.id;
    createCandidateJobExperienceModal.componentInstance.candidateId = this.candidate.id;

    createCandidateJobExperienceModal.result
      .then((candidateJobExperience) => this.candidateService.updateCandidate())
      .catch(() => { /* Isn't possible */
      });

  }

  editCandidateJobExperience(candidateJobExperience: CandidateJobExperience) {
    const editCandidateJobExperienceModal = this.modalService.open(EditCandidateJobExperienceComponent, {
      centered: true,
      backdrop: 'static'
    });

    editCandidateJobExperienceModal.componentInstance.candidateJobExperience = candidateJobExperience;

    editCandidateJobExperienceModal.result
      .then((candidateJobExperience) => this.candidateService.updateCandidate())
      .catch(() => { /* Isn't possible */
      });

  }

  deleteCandidateOccupation() {
    // Check if occupation has associated job experience
    if(this.experiences.length == 0) {
        this.deleteOccupation.emit(this.candidateOccupation);
    } else {
      // throw confirmation modal if job experience associated with occupation
      const deleteCandidateOccupationModal = this.modalService.open(ConfirmationComponent, {
        centered: true,
        backdrop: 'static'
      });

      deleteCandidateOccupationModal.componentInstance.message = 'Are you sure you want to delete this occupation? All associated job experiences will also be deleted.';

      deleteCandidateOccupationModal.result
        .then(() => this.deleteOccupation.emit(this.candidateOccupation))
        .catch(() => { /* Isn't possible */
        });
    }
  }

  deleteCandidateJobExperience(candidateJobExperience: CandidateJobExperience) {
    const deleteCandidateJobExperienceModal = this.modalService.open(ConfirmationComponent, {
      centered: true,
      backdrop: 'static'
    });

    deleteCandidateJobExperienceModal.componentInstance.message = 'Are you sure you want to delete this job experience?';

    deleteCandidateJobExperienceModal.result
      .then((result) => {
        if (result === true) {
          this.candidateJobExperienceService.delete(candidateJobExperience.id).subscribe(
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

  get isHtml() {
    return isHtml;
  }
}
