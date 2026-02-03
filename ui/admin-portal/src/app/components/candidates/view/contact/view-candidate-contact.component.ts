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
import {CandidateService} from "../../../../services/candidate.service";
import {EditCandidateContactComponent} from "./edit/edit-candidate-contact.component";

@Component({
  selector: 'app-view-candidate-contact',
  templateUrl: './view-candidate-contact.component.html',
  styleUrls: ['./view-candidate-contact.component.scss']
})
export class ViewCandidateContactComponent implements OnInit {

  @Input() candidate: Candidate;
  @Input() editable: boolean;
  /** Passed to tc-description-list instances when narrower column spacing is required */
  @Input() compact: boolean;

  loading: boolean;
  error;

  constructor(private candidateService: CandidateService,
              private modalService: NgbModal) { }

  ngOnInit() {

  }

  editContactDetails() {
    const editCandidateModal = this.modalService.open(EditCandidateContactComponent, {
      centered: true,
      backdrop: 'static',
      size: "xl"
    });

    editCandidateModal.componentInstance.candidate = this.candidate;

    editCandidateModal.result
      .then((candidate) => this.candidateService.updateCandidate(candidate))
      .catch(() => { /* Isn't possible */ });

  }


}
