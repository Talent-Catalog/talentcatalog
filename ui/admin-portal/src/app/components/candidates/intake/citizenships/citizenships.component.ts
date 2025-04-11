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

import {Component, Input, OnInit} from '@angular/core';
import {Candidate, CandidateIntakeData} from "../../../../model/candidate";
import {
  CandidateCitizenshipService
} from "../../../../services/candidate-citizenship.service";
import {Country} from "../../../../model/country";

@Component({
  selector: 'app-citizenships',
  templateUrl: './citizenships.component.html',
  styleUrls: ['./citizenships.component.scss']
})
export class CitizenshipsComponent implements OnInit {
  @Input() candidate: Candidate;
  @Input() candidateIntakeData: CandidateIntakeData;
  error: boolean;
  @Input() nationalities: Country[];
  @Input() editable: boolean;
  open: boolean;
  saving: boolean;


  constructor(
    private candidateCitizenshipService: CandidateCitizenshipService
  ) {}

  ngOnInit(): void {
  }

  deleteRecord(i: number) {
    this.candidateIntakeData.candidateCitizenships.splice(i, 1);
  }
}
