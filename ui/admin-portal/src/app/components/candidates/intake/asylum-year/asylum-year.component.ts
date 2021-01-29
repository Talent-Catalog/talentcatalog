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

import {Component, OnInit} from '@angular/core';
import {NgbDateStruct} from '@ng-bootstrap/ng-bootstrap';
import {FormBuilder} from '@angular/forms';
import {CandidateService} from '../../../../services/candidate.service';
import {IntakeComponentBase} from '../../../util/intake/IntakeComponentBase';

@Component({
  selector: 'app-asylum-year',
  templateUrl: './asylum-year.component.html',
  styleUrls: ['./asylum-year.component.scss']
})
export class AsylumYearComponent extends IntakeComponentBase implements OnInit {

  public maxDate: NgbDateStruct;
  public today: Date;

  constructor(fb: FormBuilder, candidateService: CandidateService) {
    super(fb, candidateService);
  }

  // Year is converted to string using the ngb-date-adapter file in the util folder (see app module providers)
  ngOnInit(): void {
    this.today = new Date();
    this.maxDate = {year: this.today.getFullYear(), month: this.today.getMonth() + 1, day: this.today.getDate()};
    this.form = this.fb.group({
      asylumYear: [this.candidateIntakeData?.asylumYear],
    });
  }

}
