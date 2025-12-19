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
import {UntypedFormBuilder} from "@angular/forms";
import {CandidateDependant,} from "../../../../../model/candidate";
import {AutoSaveComponentBase} from "../../../../util/autosave/AutoSaveComponentBase";
import {CandidateOpportunity} from "../../../../../model/candidate-opportunity";
import {Observable} from "rxjs";
import {
  CandidateOpportunityService,
  UpdateRelocatingDependantIds
} from "../../../../../services/candidate-opportunity.service";
import {CandidateDependantService} from "../../../../../services/candidate-dependant.service";
import {AuthorizationService} from "../../../../../services/authorization.service";

@Component({
  selector: 'app-relocating-dependants',
  templateUrl: './relocating-dependants.component.html',
  styleUrls: ['./relocating-dependants.component.scss']
})
export class RelocatingDependantsComponent extends AutoSaveComponentBase implements OnInit {

  @Input() candidateOpp: CandidateOpportunity;
  @Input() candidateId: number;
  dependants: CandidateDependant[];
  loading: boolean;
  updatingSf: boolean;

  constructor(private fb: UntypedFormBuilder,
              private candidateOpportunityService: CandidateOpportunityService,
              private candidateDependantService: CandidateDependantService,
              private authorizationService: AuthorizationService) {
    super(null);
  }

  ngOnInit(): void {
    this.fetchDependants()
    this.form = this.fb.group({
      relocatingDependantIds: [this.candidateOpp?.relocatingDependantIds],
    });
  }

  fetchDependants() {
    this.loading = true;
    this.candidateDependantService.list(this.candidateId).subscribe(
      (results) => {
        this.dependants = results;
        this.loading = false;
      }, (error) => {
        this.error = error;
        this.loading = false;
      }
    )
  }

  isReadOnly(): boolean {
    return this.authorizationService.isReadOnly();
  }

  doSave(formValue: any): Observable<void> {
    const request: UpdateRelocatingDependantIds = {
      id: this.candidateOpp.id,
      relocatingDependantIds: this.form.value.relocatingDependantIds
    }
    return this.candidateOpportunityService.updateRelocatingDependants(this.candidateOpp.id, request);
  }

  onSuccessfulSave() {
    this.candidateOpp.relocatingDependantIds = this.form.value.relocatingDependantIds;
  }

  requestSfCaseRelocationInfoUpdate() {
    this.error = null;
    this.updatingSf = true;
    this.candidateOpportunityService.updateSfCaseRelocationInfo(
      this.candidateOpp.id).subscribe(
      boolean => {
        this.updatingSf = false;
      },
      error => {
        this.error = error;
        this.updatingSf = false;
      });
  }
}
