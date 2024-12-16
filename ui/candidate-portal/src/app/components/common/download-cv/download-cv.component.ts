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

import {Component, OnInit} from '@angular/core';
import {CandidateService} from '../../../services/candidate.service';

@Component({
  selector: 'app-download-cv',
  templateUrl: './download-cv.component.html',
  styleUrls: ['./download-cv.component.scss']
})
export class DownloadCvComponent implements OnInit {

  error;
  loading: boolean = false;

  constructor(public candidateService: CandidateService) { }

  ngOnInit() {
  }

  downloadCV() {
    this.loading = true;
    this.candidateService.downloadCv().subscribe(
      result => {
        const tab = window.open();
        tab.location.href = URL.createObjectURL(result);
        this.loading = false
      },
      error => {
        this.error = error;
        this.loading = false;
      }
    );
  }

}
