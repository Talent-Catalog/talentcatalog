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
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {
  EditCandidateAdditionalInfoComponent
} from "./edit/edit-candidate-additional-info.component";
import {CandidateService} from "../../../../services/candidate.service";

@Component({
  selector: 'app-view-candidate-additional-info',
  templateUrl: './view-candidate-additional-info.component.html',
  styleUrls: ['./view-candidate-additional-info.component.scss']
})
export class ViewCandidateAdditionalInfoComponent implements OnInit {

  @Input() candidate: Candidate;
  @Input() editable: boolean;

  constructor(private modalService: NgbModal,
              private candidateService: CandidateService) { }

  ngOnInit() {
  }

  editAdditionalInfo() {
    const editAdditionalInfoModal = this.modalService.open(EditCandidateAdditionalInfoComponent, {
      centered: true,
      backdrop: 'static'
    });

    editAdditionalInfoModal.componentInstance.candidateId = this.candidate.id;

    editAdditionalInfoModal.result
      .then((candidate) => this.candidateService.updateCandidate())
      .catch(() => { /* Isn't possible */ });

  }

}
