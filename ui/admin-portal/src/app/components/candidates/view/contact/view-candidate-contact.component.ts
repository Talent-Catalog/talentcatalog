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
import { ActivatedRoute, Router } from '@angular/router';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import {Candidate} from "../../../../model/candidate";
import {CandidateService} from "../../../../services/candidate.service";
import {EditCountryComponent} from "../../../settings/countries/edit/edit-country.component";
import {EditCandidateContactComponent} from "./edit/edit-candidate-contact.component";

@Component({
  selector: 'app-view-candidate-contact',
  templateUrl: './view-candidate-contact.component.html',
  styleUrls: ['./view-candidate-contact.component.scss']
})
export class ViewCandidateContactComponent implements OnInit, OnChanges {

  @Input() candidate: Candidate;
  @Input() editable: boolean;

  loading: boolean;
  error;

  constructor(private candidateService: CandidateService,
              private modalService: NgbModal) { }

  ngOnInit() {

  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes && changes.candidate && changes.candidate.previousValue !== changes.candidate.currentValue) {
      this.loading = true;
      this.candidateService.get(this.candidate.id).subscribe(
        candidate => {
            this.candidate = candidate;
            this.loading = false;
          },
        error => {
          this.error = error;
          this.loading = false;
        });
    }
  }

  editContactDetails() {
    const editCandidateModal = this.modalService.open(EditCandidateContactComponent, {
      centered: true,
      backdrop: 'static'
    });

    editCandidateModal.componentInstance.candidateId = this.candidate.id;

    editCandidateModal.result
      .then((candidate) => this.candidate = candidate)
      .catch(() => { /* Isn't possible */ });

  }


}
