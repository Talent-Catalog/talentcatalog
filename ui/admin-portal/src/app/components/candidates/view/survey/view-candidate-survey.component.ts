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
import {EditCandidateSurveyComponent} from "./edit/edit-candidate-survey.component";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {CandidateService} from "../../../../services/candidate.service";

@Component({
  selector: 'app-view-candidate-survey',
  templateUrl: './view-candidate-survey.component.html',
  styleUrls: ['./view-candidate-survey.component.scss']
})
export class ViewCandidateSurveyComponent implements OnInit {

  @Input() candidate: Candidate;
  @Input() editable: boolean;

  constructor(private modalService: NgbModal,
              private candidateService: CandidateService) { }

  ngOnInit() {
  }

  editSurvey() {
    const editSurveyModal = this.modalService.open(EditCandidateSurveyComponent, {
      centered: true,
      backdrop: 'static'
    });

    editSurveyModal.componentInstance.candidateId = this.candidate.id;

    editSurveyModal.result
      .then((candidate) => this.candidateService.updateCandidate())
      .catch(() => { /* Isn't possible */ });

  }

}
