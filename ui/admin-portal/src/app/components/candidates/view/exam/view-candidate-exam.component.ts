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

import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {Candidate, CandidateExam} from "../../../../model/candidate";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {CandidateExamService} from "../../../../services/candidate-exam.service";
import {CreateCandidateExamComponent} from "./create/create-candidate-exam.component";
import {ConfirmationComponent} from "../../../util/confirm/confirmation.component";
import {EditCandidateExamComponent} from "./edit/edit-candidate-exam.component";

@Component({
  selector: 'app-view-candidate-exam',
  templateUrl: './view-candidate-exam.component.html',
  styleUrls: ['./view-candidate-exam.component.scss']
})
export class ViewCandidateExamComponent implements OnChanges {

  @Input() candidate: Candidate;
  @Input() editable: boolean;
  @Input() adminUser: boolean;

  candidateExams: CandidateExam[];
  candidateExam: CandidateExam;
  loading: boolean;
  error;

  constructor(private candidateExamService: CandidateExamService,
              private modalService: NgbModal) {
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes && changes.candidate && changes.candidate.previousValue !== changes.candidate.currentValue) {
      this.doSearch();
    }
  }

  doSearch() {
    this.loading = true;
    this.candidateExamService.list(this.candidate.id).subscribe(
      candidateExams => {
        this.candidateExams = candidateExams;
        this.loading = false;
      },
      error => {
        this.error = error;
        this.loading = false;
      })
    ;
  }

  createCandidateExam() {
    const createCandidateExamModal = this.modalService.open(CreateCandidateExamComponent, {
      centered: true,
      backdrop: 'static'
    });

    createCandidateExamModal.componentInstance.candidateId = this.candidate.id;

    createCandidateExamModal.result
    .then((candidateExam) => this.doSearch())
    .catch(() => { /* Isn't possible */ });

  }

  editCandidateExam(candidateExam: CandidateExam) {
    const editCandidateExamModal = this.modalService.open(EditCandidateExamComponent, {
      centered: true,
      backdrop: 'static'
    });

    editCandidateExamModal.componentInstance.candidateExam = candidateExam;

    editCandidateExamModal.result
    .then(() => this.doSearch())
    .catch(() => { /* Isn't possible */ });

  }

  deleteCandidateExam(candidateExam: CandidateExam) {
    const deleteCandidateExamModal = this.modalService.open(ConfirmationComponent, {
      centered: true,
      backdrop: 'static'
    });
    deleteCandidateExamModal.componentInstance.message = 'Are you sure you want to delete this exam?';

    deleteCandidateExamModal.result
    .then((result) => {
      if (result === true) {
        this.candidateExamService.delete(candidateExam.id).subscribe(
          (user) => {
            this.loading = false;
            this.doSearch();
          },
          (error) => {
            this.error = error;
            this.loading = false;
          });
        this.doSearch();
      }
    })
    .catch(() => { /* Isn't possible */ });
  }

}
