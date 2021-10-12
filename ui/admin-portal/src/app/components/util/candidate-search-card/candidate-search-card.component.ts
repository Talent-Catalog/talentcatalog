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

import {Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges} from '@angular/core';
import {Candidate} from '../../../model/candidate';
import {User} from '../../../model/user';
import {CandidateSource} from '../../../model/base';
import {CandidateAttachment} from "../../../model/candidate-attachment";

@Component({
  selector: 'app-candidate-search-card',
  templateUrl: './candidate-search-card.component.html',
  styleUrls: ['./candidate-search-card.component.scss']
})
export class CandidateSearchCardComponent implements OnInit, OnChanges {

  @Input() candidate: Candidate;
  @Input() loggedInUser: User;
  @Input() candidateSource: CandidateSource;
  @Input() sourceType: String;
  @Input() defaultSearch: boolean;
  @Input() savedSearchSelectionChange: boolean;

  @Output() closeEvent = new EventEmitter();

  cvs: CandidateAttachment[];
  other: CandidateAttachment[];

  showAttachments: boolean = false;
  showNotes: boolean = false;

  constructor() { }

  ngOnInit() {
  }

  ngOnChanges(changes: SimpleChanges) {
    this.cvs = this.candidate.candidateAttachments.filter(a => a.cv === true);
    this.other = this.candidate.candidateAttachments.filter(a => a.cv === false);
  }

  close() {
    this.closeEvent.emit();
  }

  toggleAttachments() {
    this.showAttachments = !this.showAttachments;
  }

  toggleNotes() {
    this.showNotes = !this.showNotes;
  }

}
