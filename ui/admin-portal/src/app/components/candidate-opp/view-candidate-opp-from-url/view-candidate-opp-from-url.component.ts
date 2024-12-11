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
import {CandidateOpportunity} from "../../../model/candidate-opportunity";
import {ActivatedRoute} from "@angular/router";
import {CandidateOpportunityService} from "../../../services/candidate-opportunity.service";

@Component({
  selector: 'app-view-candidate-opp-from-url',
  templateUrl: './view-candidate-opp-from-url.component.html',
  styleUrls: ['./view-candidate-opp-from-url.component.scss']
})
export class ViewCandidateOppFromUrlComponent implements OnInit {
  loading: boolean;
  error: string;
  opp: CandidateOpportunity;

  constructor(
    private oppService: CandidateOpportunityService,
    private route: ActivatedRoute,
  ) { }

  ngOnInit(): void {
    this.refreshOppInfo();
  }

  private refreshOppInfo() {
    this.route.paramMap.subscribe(params => {
      const id = +params.get('id');
      if (id) {
        this.loadOpp(id);
      }
    });
  }

  private loadOpp(id: number) {

    this.loading = true;
    this.error = null;
    this.oppService.get(id).subscribe(
      opp => {
        this.setOpp(opp);
        this.loading = false;
      },
      error => {
        this.error = error;
        this.setOpp(null);
        this.loading = false;
      });
  }

  private setOpp(opp: CandidateOpportunity) {
    this.opp = opp;
  }

  onCandidateOppUpdated(opp: CandidateOpportunity) {
    this.setOpp(opp)
  }
}
