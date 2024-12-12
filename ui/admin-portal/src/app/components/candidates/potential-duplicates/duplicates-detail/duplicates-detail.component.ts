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

import {Component} from '@angular/core';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {CandidateService} from "../../../../services/candidate.service";
import {Candidate} from "../../../../model/candidate";

/**
 * Provides a modal for reviewing potential duplicates of selected candidate.
 */
@Component({
  selector: 'app-duplicates-detail',
  templateUrl: './duplicates-detail.component.html',
  styleUrls: ['./duplicates-detail.component.scss']
})
export class DuplicatesDetailComponent {
  error = null;
  loading = null;
  selectedCandidate: Candidate;
  potentialDuplicates: Candidate[];

  constructor(
    private activeModal: NgbActiveModal,
    private candidateService: CandidateService
  ) { }

  ngOnInit(): void {
    this.fetchPotentialDuplicates(this.selectedCandidate.id);
  }

  private fetchPotentialDuplicates(candidateId: number) {
    this.loading = true;
    this.candidateService.fetchPotentialDuplicates(candidateId).subscribe(
      result => {
        this.potentialDuplicates = result;
        this.loading = false;
      },
      error => {
        this.error = error;
        this.loading = false;
      }
    );
  }

  /**
   * User can display new results — changes help text if there are no longer any potential duplicates.
   */
  public refresh(): void {
    this.fetchPotentialDuplicates(this.selectedCandidate.id);
  }

  closeModal() {
    this.activeModal.close()
  }

}
