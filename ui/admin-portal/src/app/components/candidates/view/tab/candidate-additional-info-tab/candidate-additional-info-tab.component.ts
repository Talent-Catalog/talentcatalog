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

import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {Candidate} from "../../../../../model/candidate";
import {AuthService} from "../../../../../services/auth.service";

@Component({
  selector: 'app-candidate-additional-info-tab',
  templateUrl: './candidate-additional-info-tab.component.html',
  styleUrls: ['./candidate-additional-info-tab.component.scss']
})
export class CandidateAdditionalInfoTabComponent implements OnInit {

  @Input() candidate: Candidate;
  @Input() editable: boolean = false;
  @Input() canViewPrivateInfo: boolean = false;
  @Output() candidateChanged = new EventEmitter();

  constructor(private authService: AuthService) { }

  ngOnInit() {
  }

  onCandidateChanged() {
    this.candidateChanged.emit();
  }
}
