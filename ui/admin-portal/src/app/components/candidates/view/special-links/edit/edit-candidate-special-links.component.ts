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
import {Candidate} from "../../../../../model/candidate";
import {UntypedFormBuilder, UntypedFormGroup, Validators} from "@angular/forms";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {CandidateService} from "../../../../../services/candidate.service";
import {EnvService} from "../../../../../services/env.service";

@Component({
  selector: 'app-edit-candidate-special-links',
  templateUrl: './edit-candidate-special-links.component.html',
  styleUrls: ['./edit-candidate-special-links.component.scss']
})
export class EditCandidateSpecialLinksComponent implements OnInit {

  candidateId: number;
  candidateForm: UntypedFormGroup;

  error;
  loading: boolean;
  saving: boolean;

  sfUrlPlaceholder: string;

  constructor(private activeModal: NgbActiveModal,
              private fb: UntypedFormBuilder,
              private candidateService: CandidateService,
              private envService: EnvService) { }

  ngOnInit() {
    this.sfUrlPlaceholder = this.envService.sfLightningUrl;

    const linkedInRegex = /^http(s)?:\/\/([\w]+\.)?linkedin\.com\/in\/[A-z0-9_-]+\/?/
    this.loading = true;

    this.candidateService.get(this.candidateId).subscribe(candidate => {
      this.candidateForm = this.fb.group({
        sflink: [candidate.sflink],
        folderlink: [candidate.folderlink],
        videolink: [candidate.videolink],
        linkedInLink: [candidate.linkedInLink, Validators.pattern(linkedInRegex)],
      });
      this.loading = false;
    });
  }

  onSave() {
    this.saving = true;
    this.candidateService.updateLinks(this.candidateId, this.candidateForm.value).subscribe(
      (candidate) => {
        this.closeModal(candidate);
        this.saving = false;
      },
      (error) => {
        this.error = error;
        this.saving = false;
      });
  }

  closeModal(candidate: Candidate) {
    this.activeModal.close(candidate);
  }

  dismiss() {
    this.activeModal.dismiss(false);
  }

}
