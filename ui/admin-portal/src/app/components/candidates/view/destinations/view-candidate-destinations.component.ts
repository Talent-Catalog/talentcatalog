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

import {Component, Input, OnInit, SimpleChanges} from '@angular/core';
import {Candidate} from "../../../../model/candidate";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {
  EditCandidateDestinationsComponent
} from "./edit/edit-candidate-destinations/edit-candidate-destinations.component";
import {CandidateDestinationService} from "../../../../services/candidate-destination.service";
import {CandidateDestination} from "../../../../model/candidate-destination";
import {CandidateService} from "../../../../services/candidate.service";

@Component({
  selector: 'app-view-candidate-destinations',
  templateUrl: './view-candidate-destinations.component.html',
  styleUrls: ['./view-candidate-destinations.component.scss']
})
export class ViewCandidateDestinationsComponent implements OnInit {

  @Input() candidate: Candidate;
  @Input() editable: boolean;

  loading: boolean;
  error;
  candidateDestinations: CandidateDestination[];
  emptyDestinations: boolean;

  constructor(private candidateDestinationService: CandidateDestinationService,
              private candidateService: CandidateService,
              private modalService: NgbModal) { }

  ngOnInit() {

  }

  // todo what happens when the candidate object isn't changing, but we are changing tabs (candidate profile)
  ngOnChanges(changes: SimpleChanges) {
    if (changes && changes.candidate && changes.candidate.previousValue !== changes.candidate.currentValue) {
      if (changes.candidate.currentValue.candidateDestinations != null) {
        this.checkForEmptyDestinations(changes.candidate.currentValue.candidateDestinations);
      }
    }
  }

  doSearch() {
    this.loading = true;
    this.candidateDestinationService.list(this.candidate.id).subscribe(
      candidateDestinations => {
        this.candidateDestinations = candidateDestinations;
        this.loading = false;
        this.emptyDestinations = this.checkForEmptyDestinations(candidateDestinations);
      },
      error => {
        this.error = error;
        this.loading = false;
      })
    ;
  }

  editDestinationsDetails(destination: CandidateDestination) {
    const editCandidateDestinationsModal = this.modalService.open(EditCandidateDestinationsComponent, {
      centered: true,
      backdrop: 'static'
    });

    editCandidateDestinationsModal.componentInstance.candidateDestination = destination;

    editCandidateDestinationsModal.result
      .then((candidateDestination) => {this.candidateService.updateCandidate()} )
      .catch(() => { /* Isn't possible */ });

  }
  // Some candidates have 'empty' candidate destinations so run a check to only display them if they have data.
  // These were created automatically in old code, having a country ID & candidate ID but no useful data inside.
  // Now we have moved candidate destinations to the registration so all candidates will have to have these filled out.
  checkForEmptyDestinations(candidateDestinations: CandidateDestination[]) {
    if (candidateDestinations.length === 0) {
      this.emptyDestinations = true;
    } else if (candidateDestinations.length > 0) {
      this.emptyDestinations = candidateDestinations.every(cd => cd.interest == null && cd.notes == null);
    }
    return this.emptyDestinations;
  }
}
