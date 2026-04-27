/*
 * Copyright (c) 2025 Talent Catalog.
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

import {Component, EventEmitter, Input, Output} from '@angular/core';
import {Candidate} from 'src/app/model/candidate';
import {CandidateService} from 'src/app/services/candidate.service';
import {DuolingoCouponService} from 'src/app/services/duolingo-coupon.service';

@Component({
  selector: 'app-duolingo-assignment',
  templateUrl: './duolingo-assignment.component.html',
  styleUrls: ['./duolingo-assignment.component.scss']
})
export class DuolingoAssignmentComponent {
  @Input() candidate: Candidate;
  @Output() errorOccurred = new EventEmitter<any>(); // Define the event emitter for error

  constructor(
    private candidateService: CandidateService,
    private duolingoCouponService: DuolingoCouponService,
  ) {
  }

  assignDuolingoCouponTask() {
    this.duolingoCouponService.assignCouponToCandidate(this.candidate.id).subscribe(
      () => {
        this.candidateService.updateCandidate();
      },
      error => {
        this.errorOccurred.emit(error);
      }
    );
  }
}
