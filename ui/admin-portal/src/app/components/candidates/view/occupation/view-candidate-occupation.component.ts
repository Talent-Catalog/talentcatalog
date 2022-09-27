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
import {FormBuilder, FormGroup} from "@angular/forms";
import {Candidate} from "../../../../model/candidate";
import {CandidateOccupation} from "../../../../model/candidate-occupation";
import {CandidateService} from "../../../../services/candidate.service";
import {CandidateOccupationService} from "../../../../services/candidate-occupation.service";
import {CandidateJobExperience} from "../../../../model/candidate-job-experience";
import {CandidateJobExperienceService} from "../../../../services/candidate-job-experience.service";
import {EditCandidateJobExperienceComponent} from "./experience/edit/edit-candidate-job-experience.component";
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

  candidateJobExperienceForm: FormGroup;
  _loading = {
    experience: true,
    occupation: true,
    candidate: true
  };
  error;
  candidateOccupations: CandidateOccupation[];
  experiences: CandidateJobExperience[];
  orderOccupation: boolean;
  hasMore: boolean;
  sortDirection: string;

  constructor(private candidateService: CandidateService,
              private candidateOccupationService: CandidateOccupationService,
              private candidateJobExperienceService: CandidateJobExperienceService,
              private modalService: NgbModal,
              private fb: FormBuilder) { }

  ngOnInit() {}

  ngOnChanges(changes: SimpleChanges) {
    this.experiences = [];
    this.orderOccupation = true;

    this.candidateJobExperienceForm = this.fb.group({
      candidateId: [this.candidate.id],
      pageSize: 10,
      pageNumber: 0,
      sortDirection: 'DESC',
      sortFields: [['startDate']]
    });
    if (changes && changes.candidate && changes.candidate.previousValue !== changes.candidate.currentValue) {
      this.doSearch();
    }
  }

  get loading() {
    const l = this._loading;
    return l.experience || l.occupation || l.candidate;
  }

  doSearch() {
    /* GET CANDIDATE */
    this.candidateService.get(this.candidate.id).subscribe(
      candidate => {
          this.candidate = candidate;
          this._loading.candidate = false;
        },
      error => {
          this.error = error;
          this._loading.candidate = false;
        });

    /* GET CANDIDATE OCCUPATIONS */
    this.candidateOccupationService.get(this.candidate.id).subscribe(
      results => {
         this.candidateOccupations = results;
         this._loading.occupation = false;
         },
      error => {
         this.error = error;
         this._loading.occupation = false;
       }
    );

    this.loadJobExperiences();
  }

  editCandidateJobExperience(candidateJobExperience: CandidateJobExperience) {
    const editCandidateJobExperienceModal = this.modalService.open(EditCandidateJobExperienceComponent, {
      centered: true,
      backdrop: 'static'
    });

    editCandidateJobExperienceModal.componentInstance.candidateJobExperience = candidateJobExperience;

    editCandidateJobExperienceModal.result
      .then(() => this.doSearch())
      .catch(() => { /* Isn't possible */
      });
  }

  loadJobExperiences(more: boolean = false) {
    if (more) {
      // Load the next page
      const page = this.candidateJobExperienceForm.value.pageNumber;
      this.candidateJobExperienceForm.patchValue({pageNumber: page + 1});
    } else {
      // Load the first page
      this.candidateJobExperienceForm.patchValue({pageNumber: 0});
    }

    /* GET CANDIDATE EXPERIENCE */
    this.candidateJobExperienceService.search(this.candidateJobExperienceForm.value).subscribe(
      results => {
        if (more) {
          this.experiences = this.experiences.concat(results.content);
        } else {
          this.experiences = results.content;
        }
        this.hasMore = results.totalPages > results.number + 1;
        this._loading.experience = false;
      },
      error => {
        this.error = error;
        this._loading.experience = false;
      });
  }

  createCandidateOccupation() {
    const createCandidateOccupationModal = this.modalService.open(CreateCandidateOccupationComponent, {
      centered: true,
      backdrop: 'static'
    });

    createCandidateOccupationModal.componentInstance.candidateId = this.candidate.id;

    createCandidateOccupationModal.result
      .then(() => this.doSearch())
      .catch(() => { /* Isn't possible */
      });
  }


  deleteCandidateOccupation(candidateOccupation: CandidateOccupation) {
    this.candidateOccupationService.delete(candidateOccupation.id).subscribe(
      (results) => {
        this.doSearch();
      },
      (error) => {
        this.error = error;
      })
  }
}


