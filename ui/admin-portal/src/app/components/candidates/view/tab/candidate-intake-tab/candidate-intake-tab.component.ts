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

import {Component, EventEmitter, Output} from '@angular/core';
import {IntakeComponentTabBase} from "../../../../util/intake/IntakeComponentTabBase";

@Component({
  selector: 'app-candidate-intake-tab',
  templateUrl: './candidate-intake-tab.component.html',
  styleUrls: ['./candidate-intake-tab.component.scss']
})
export class CandidateIntakeTabComponent extends IntakeComponentTabBase {
  @Output() closeAcc = new EventEmitter();
  collapse: boolean = false;
  activeIds: string[] = ['intake-confirm', 'intake-int-recruit', 'intake-english-assessment', 'intake-residency',
    'intake-host-country', 'intake-registration', 'intake-partner-info', 'intake-additional-eligibility']

  closeAll() {
    this.activeIds = [];
    this.collapse = true;
  }

  openAll() {
    this.activeIds = ['intake-confirm', 'intake-int-recruit', 'intake-english-assessment', 'intake-residency',
      'intake-host-country', 'intake-registration', 'intake-partner-info', 'intake-additional-eligibility']
    this.collapse = false;
  }

}
