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
import {Candidate} from "../../../../../model/candidate";
import {User} from "../../../../../model/user";

@Component({
  selector: 'app-candidate-general-tab',
  templateUrl: './candidate-general-tab.component.html',
  styleUrls: ['./candidate-general-tab.component.scss']
})
export class CandidateGeneralTabComponent implements OnInit, OnChanges {

  @Input() candidate: Candidate;
  @Input() editable: boolean;
  @Input() loggedInUser: User;

  loading: boolean;
  error;
  result: Candidate;

  constructor() { }

  ngOnInit() {
    this.error = null;
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes && changes.candidate && changes.candidate.previousValue !== changes.candidate.currentValue) {
      this.error = null;
      this.loading = true;
      this.result = this.candidate;
      this.loading = false;
    }
  }

}
