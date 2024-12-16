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

import {
  Component,
  EventEmitter,
  Input,
  OnChanges,
  OnInit,
  Output,
  SimpleChanges
} from '@angular/core';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {CandidateService} from "../../../services/candidate.service";
import {EditCandidateReviewStatusItemComponent} from "./edit/edit-candidate-review-status-item.component";
import {SavedSearch} from "../../../model/saved-search";
import {CandidateReviewStatusItem} from "../../../model/candidate-review-status-item";
import {CandidateSource} from "../../../model/base";

@Component({
  selector: 'app-candidate-review-status-item',
  templateUrl: './candidate-review-status-item.component.html',
  styleUrls: ['./candidate-review-status-item.component.scss']
})
export class CandidateReviewStatusItemComponent implements OnInit, OnChanges {

  @Input() candidateId: number;
  @Input() candidateReviewStatusItems: CandidateReviewStatusItem[];
  @Input() savedSearch: CandidateSource;

  @Output() reviewStatusChange = new EventEmitter();

  loading: boolean;
  error;
  debug: boolean = false;
  candidateReviewStatusItem;

  constructor(private candidateService: CandidateService,
              private modalService: NgbModal) { }

  ngOnInit() {
    this.selectReviewStatus();
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.selectReviewStatus();
  }

  private selectReviewStatus() {
    if (this.candidateReviewStatusItems && this.savedSearch) {
      this.candidateReviewStatusItem = this.candidateReviewStatusItems.find(
        s => s.savedSearch.id === this.savedSearch.id);
    }
  }

  editReviewStatusItem() {
    const editModal = this.modalService.open(EditCandidateReviewStatusItemComponent, {
      centered: true,
      backdrop: 'static'
    });

    editModal.componentInstance.candidateReviewStatusItemId = this.candidateReviewStatusItem ? this.candidateReviewStatusItem.id : null;
    editModal.componentInstance.candidateId = this.candidateId;
    editModal.componentInstance.savedSearch = this.savedSearch as SavedSearch;

    editModal.result
      .then((candidateReviewStatusItem) => this.reviewStatusChange.emit(candidateReviewStatusItem))
      .catch(() => { /* Isn't possible */ });
  }

}
