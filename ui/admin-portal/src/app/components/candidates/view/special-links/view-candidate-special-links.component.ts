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
import {Candidate} from "../../../../model/candidate";
import {CandidateService} from "../../../../services/candidate.service";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {EditCandidateSpecialLinksComponent} from "./edit/edit-candidate-special-links.component";
import {AuthorizationService} from "../../../../services/authorization.service";

@Component({
  selector: 'app-view-candidate-special-links',
  templateUrl: './view-candidate-special-links.component.html',
  styleUrls: ['./view-candidate-special-links.component.scss']
})
export class ViewCandidateSpecialLinksComponent implements OnInit {
  @Input() candidate: Candidate;
  @Input() editable: boolean;
  /** Passed to tc-description-list instances to define column spacing */
  @Input() compact: boolean = false;

  loading: boolean;
  error;

  constructor(
    private authService: AuthorizationService,
    private candidateService: CandidateService,
              private modalService: NgbModal) { }

  ngOnInit() {
  }

  editSpecialLinks() {
    const editCandidateModal = this.modalService.open(EditCandidateSpecialLinksComponent, {
      centered: true,
      backdrop: 'static'
    });

    editCandidateModal.componentInstance.candidateId = this.candidate.id;

    editCandidateModal.result
      .then((candidate) => this.candidateService.updateCandidate())
      .catch(() => { /* Isn't possible */ });

  }

  createCandidateFolder() {
    this.error = null;
    this.loading = true;
    this.candidateService.createCandidateFolder(this.candidate.id).subscribe(
      candidate => {
        this.candidateService.updateCandidate();
        this.loading = false;
      },
      error => {
        this.error = error;
        this.loading = false;
      });
  }

  createUpdateSalesforce() {
    this.error = null;
    this.loading = true;
    this.candidateService.createUpdateLiveCandidate(this.candidate.id).subscribe(
      candidate => {
        this.candidateService.updateCandidate();
        this.loading = false;
      },
      error => {
        this.error = error;
        this.loading = false;
      });
  }

  canAccessSalesforce(): boolean {
    return this.authService.canAccessSalesforce();
  }

  canAccessGoogleDrive(): boolean {
    return this.authService.canAccessGoogleDrive();
  }

}
