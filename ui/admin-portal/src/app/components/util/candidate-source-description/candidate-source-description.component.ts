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

import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {AutoSaveComponentBase} from "../autosave/AutoSaveComponentBase";
import {UntypedFormBuilder} from "@angular/forms";
import {Observable} from "rxjs";
import {CandidateSource, UpdateCandidateSourceDescriptionRequest} from "../../../model/base";
import {CandidateService} from "../../../services/candidate.service";
import {CandidateSourceService} from "../../../services/candidate-source.service";
import {AuthorizationService} from "../../../services/authorization.service";
import {isSavedList, isSubmissionList} from "../../../model/saved-list";

@Component({
  selector: 'app-candidate-source-description',
  templateUrl: './candidate-source-description.component.html',
  styleUrls: ['./candidate-source-description.component.scss']
})
export class CandidateSourceDescriptionComponent extends AutoSaveComponentBase
  implements OnInit, OnChanges {

  @Input() candidateSource: CandidateSource;

  constructor(private fb: UntypedFormBuilder,
              private authorizationService: AuthorizationService,
              private candidateSourceService: CandidateSourceService,
              candidateService: CandidateService) {
    super(candidateService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      description: [this.candidateSource.description],
    });
  }

  get description(): string {
    return this.form.value?.description;
  }

  ngOnChanges(changes: SimpleChanges): void {
    //Replace the form value with the new data when changing from one input to the next.
    if (this.form) {
      this.form.controls['description'].patchValue(this.candidateSource.description);
    }
  }

  doSave(formValue: any): Observable<void> {
    const request: UpdateCandidateSourceDescriptionRequest = {
      description: this.description
    }
    return this.candidateSourceService.updateDescription(this.candidateSource, request);
  }

  isEditable(): boolean {
    return this.authorizationService.canEditCandidateSource(this.candidateSource);
  }

  onSuccessfulSave(): void {
    this.candidateSource.description = this.description;
  }

  isSavedList(): boolean {
    return isSavedList(this.candidateSource);
  }

  isSubmissionList(): boolean {
    return isSubmissionList(this.candidateSource);
  }
}
