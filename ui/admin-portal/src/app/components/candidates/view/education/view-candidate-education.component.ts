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

import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {Candidate} from "../../../../model/candidate";
import {CandidateEducation} from "../../../../model/candidate-education";
import {CandidateEducationService} from "../../../../services/candidate-education.service";
import {EditCandidateEducationComponent} from "./edit/edit-candidate-education.component";
import {CreateCandidateEducationComponent} from "./create/create-candidate-education.component";

@Component({
  selector: 'app-view-candidate-education',
  templateUrl: './view-candidate-education.component.html',
  styleUrls: ['./view-candidate-education.component.scss']
})
export class ViewCandidateEducationComponent implements OnInit, OnChanges {

  @Input() candidate: Candidate;
  @Input() editable: boolean;

  candidateEducations: CandidateEducation[];
  loading: boolean;
  error;

  constructor(private candidateEducationService: CandidateEducationService,
              private modalService: NgbModal ) {
  }

  ngOnInit() {
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes && changes.candidate && changes.candidate.previousValue !== changes.candidate.currentValue) {
       this.search();
    }
  }

  search(){
    this.loading = true;
    this.candidateEducationService.list(this.candidate.id).subscribe(
      candidateEducations => {
        this.candidateEducations = candidateEducations;
        this.loading = false;
      },
      error => {
        this.error = error;
        this.loading = false;
      })
    ;
  }

  editCandidateEducation(candidateEducation: CandidateEducation) {
    const editCandidateEducationModal = this.modalService.open(EditCandidateEducationComponent, {
      centered: true,
      backdrop: 'static'
    });

    editCandidateEducationModal.componentInstance.candidateEducation = candidateEducation;

    editCandidateEducationModal.result
      .then((candidateEducation) => this.search())
      .catch(() => { /* Isn't possible */ });

  }

  createCandidateEducation() {
    const createCandidateEducationModal = this.modalService.open(CreateCandidateEducationComponent, {
      centered: true,
      backdrop: 'static'
    });

    createCandidateEducationModal.componentInstance.candidateId = this.candidate.id;

    createCandidateEducationModal.result
      .then((candidateEducation) => this.search())
      .catch(() => { /* Isn't possible */ });

  }


}
