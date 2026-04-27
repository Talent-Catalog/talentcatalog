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

import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {Candidate} from "../../../../model/candidate";
import {CandidateOccupation} from "../../../../model/candidate-occupation";
import {CandidateService} from "../../../../services/candidate.service";
import {CandidateOccupationService} from "../../../../services/candidate-occupation.service";
import {CandidateJobExperience} from "../../../../model/candidate-job-experience";
import {
  EditCandidateJobExperienceComponent
} from "./experience/edit/edit-candidate-job-experience.component";
import {CreateCandidateOccupationComponent} from "./create/create-candidate-occupation.component";

@Component({
  selector: 'app-view-candidate-occupation',
  templateUrl: './view-candidate-occupation.component.html',
  styleUrls: ['./view-candidate-occupation.component.scss']
})
export class ViewCandidateOccupationComponent implements OnInit, OnChanges {

  @Input() candidate: Candidate;
  @Input() editable: boolean;
  @Input() adminUser: boolean;

  _loading = {
    experience: false,
    occupation: false,
    candidate: false
  };
  error;
  candidateOccupations: CandidateOccupation[];
  experiences: CandidateJobExperience[];
  orderOccupation: boolean = true;
  hasMore: boolean;
  sortDirection: string;

  constructor(private candidateService: CandidateService,
              private candidateOccupationService: CandidateOccupationService,
              private modalService: NgbModal) { }

  ngOnInit() {}

  ngOnChanges(changes: SimpleChanges) {
    this.orderOccupation = true;
    this.experiences = this.candidate?.candidateJobExperiences;
  }

  get loading() {
    const l = this._loading;
    return l.experience || l.occupation || l.candidate;
  }

  editCandidateJobExperience(candidateJobExperience: CandidateJobExperience) {
    const editCandidateJobExperienceModal = this.modalService.open(EditCandidateJobExperienceComponent, {
      centered: true,
      backdrop: 'static'
    });

    editCandidateJobExperienceModal.componentInstance.candidateJobExperience = candidateJobExperience;

    editCandidateJobExperienceModal.result
      .then(() => this.candidateService.updateCandidate())
      .catch(() => { /* Isn't possible */
      });
  }

  createCandidateOccupation() {
    const createCandidateOccupationModal = this.modalService.open(CreateCandidateOccupationComponent, {
      centered: true,
      backdrop: 'static'
    });

    createCandidateOccupationModal.componentInstance.candidateId = this.candidate.id;

    createCandidateOccupationModal.result
      .then(() => this.candidateService.updateCandidate())
      .catch(() => { /* Isn't possible */
      });
  }


  deleteCandidateOccupation(candidateOccupation: CandidateOccupation) {
    this.candidateOccupationService.delete(candidateOccupation.id).subscribe(
      (results) => {
        this.candidateService.updateCandidate()
      },
      (error) => {
        this.error = error;
      })
  }
}


