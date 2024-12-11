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
import {ActivatedRoute} from "@angular/router";
import {CvService} from "../../services/cv.service";
import {Candidate} from "../../model/candidate";

@Component({
  selector: 'app-cv-landing',
  templateUrl: './cv-landing.component.html',
  styleUrls: ['./cv-landing.component.scss']
})
export class CvLandingComponent implements OnInit {

  candidate: Candidate;
  error;
  loading: boolean;

  constructor(private route: ActivatedRoute, private cvService: CvService) { }

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      const token = params.get('token');
      if (token) {
        this.fetchCv(token);
      }
    });
  }

  private fetchCv(token: string) {
    this.error = null;
    this.loading = true;
    this.cvService.decodeCvRequest(token).subscribe(
      (candidate: Candidate) => {
        this.candidate = candidate;
        this.loading = false;
      },
      error => {
        this.error = error;
        this.loading = false;
      }
    )
  }
}
