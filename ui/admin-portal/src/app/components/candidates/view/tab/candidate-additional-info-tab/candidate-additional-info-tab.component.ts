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
import {Candidate} from "../../../../../model/candidate";
import {AuthorizationService} from "../../../../../services/authorization.service";
import {CandidateService} from "../../../../../services/candidate.service";

@Component({
  selector: 'app-candidate-additional-info-tab',
  templateUrl: './candidate-additional-info-tab.component.html',
  styleUrls: ['./candidate-additional-info-tab.component.scss']
})
export class CandidateAdditionalInfoTabComponent implements OnInit, OnChanges {

  @Input() candidate: Candidate;
  @Input() editable: boolean = false;
  @Input() canViewPrivateInfo: boolean = false;
  @Output() candidateChanged = new EventEmitter();

  loading: boolean;
  error: boolean;

  constructor(private authService: AuthorizationService,
              private candidateService: CandidateService) { }

  ngOnInit() {
  }

  // todo update the candidate object when changing the tabs to refresh any changes in data from the tab components.
  ngOnChanges(changes: SimpleChanges): void {

  }

}
