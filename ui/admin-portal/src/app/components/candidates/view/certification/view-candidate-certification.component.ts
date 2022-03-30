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
import {CandidateCertification} from "../../../../model/candidate-certification";
import {CandidateCertificationService} from "../../../../services/candidate-certification.service";
import {EditCandidateCertificationComponent} from "./edit/edit-candidate-certification.component";
import {CreateCandidateCertificationComponent} from "./create/create-candidate-certification.component";
import {ConfirmationComponent} from "../../../util/confirm/confirmation.component";

@Component({
  selector: 'app-view-candidate-certification',
  templateUrl: './view-candidate-certification.component.html',
  styleUrls: ['./view-candidate-certification.component.scss']
})
export class ViewCandidateCertificationComponent implements OnInit, OnChanges {

  @Input() candidate: Candidate;
  @Input() editable: boolean;
  @Input() adminUser: boolean;

  candidateCertifications: CandidateCertification[];
  candidateCertification: CandidateCertification;
  loading: boolean;
  error;

  constructor(private candidateCertificationService: CandidateCertificationService,
              private modalService: NgbModal) {
  }

  ngOnInit() {

  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes && changes.candidate && changes.candidate.previousValue !== changes.candidate.currentValue) {
      this.doSearch();
    }
  }

  doSearch() {
    this.loading = true;
    this.candidateCertificationService.list(this.candidate.id).subscribe(
      candidateCertifications => {
        this.candidateCertifications = candidateCertifications;
        this.loading = false;
      },
      error => {
        this.error = error;
        this.loading = false;
      })
    ;
  }

  editCandidateCertification(candidateCertification: CandidateCertification) {
    const editCandidateCertificationModal = this.modalService.open(EditCandidateCertificationComponent, {
      centered: true,
      backdrop: 'static'
    });

    editCandidateCertificationModal.componentInstance.candidateCertification = candidateCertification;

    editCandidateCertificationModal.result
      .then(() => this.doSearch())
      .catch(() => { /* Isn't possible */ });

  }

  createCandidateCertification() {
    const createCandidateCertificationModal = this.modalService.open(CreateCandidateCertificationComponent, {
      centered: true,
      backdrop: 'static'
    });

    createCandidateCertificationModal.componentInstance.candidateId = this.candidate.id;

    createCandidateCertificationModal.result
      .then((candidateCertification) => this.doSearch())
      .catch(() => { /* Isn't possible */ });

  }

  deleteCandidateCertification(candidateCertification: CandidateCertification) {
    const deleteCandidateCertificationModal = this.modalService.open(ConfirmationComponent, {
      centered: true,
      backdrop: 'static'
    });

    deleteCandidateCertificationModal.componentInstance.message = 'Are you sure you want to delete this certification?';

    deleteCandidateCertificationModal.result
      .then((result) => {
        if (result === true) {
          this.candidateCertificationService.delete(candidateCertification.id).subscribe(
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
