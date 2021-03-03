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
import {CandidateService} from "../../../services/candidate.service";
import {Candidate, linkedInUrl} from "../../../model/candidate";

@Component({
  selector: 'app-candidate-profile',
  templateUrl: './candidate-profile.component.html',
  styleUrls: ['./candidate-profile.component.scss']
})
export class CandidateProfileComponent implements OnInit {

  error: any;
  loading: boolean;

  candidate: Candidate;
  linkedInUrl = linkedInUrl;

  constructor(private candidateService: CandidateService) { }

  ngOnInit() {
    this.loading = true;
    this.candidateService.getProfile().subscribe(
      (response) => {
        this.candidate = response;
        this.loading = false;
      },
      (error) => {
        this.error = error;
        this.loading = false;
      });
  }

}
