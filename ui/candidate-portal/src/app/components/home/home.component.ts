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
import {CandidateService} from "../../services/candidate.service";
import {Candidate, CandidateStatus} from "../../model/candidate";
import {User} from "../../model/user";
import {LanguageService} from "../../services/language.service";

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
})
export class HomeComponent implements OnInit {

  loading: boolean;
  error: any;

  candidate: Candidate;
  user: User;
  lang: string;

  constructor(private candidateService: CandidateService,
              private languageService: LanguageService) {
  }

  ngOnInit() {
    this.loading = true;
    this.lang = this.languageService.getSelectedLanguage();
    this.candidateService.getStatus().subscribe(
      (candidate) => {
        this.candidate = candidate || ({status: CandidateStatus.draft} as Candidate);
        this.user = this.candidate.user;
        this.loading = false;
      },
      (error) => {
        this.error = error;
        this.loading = false;
      }
    );
  }
}

