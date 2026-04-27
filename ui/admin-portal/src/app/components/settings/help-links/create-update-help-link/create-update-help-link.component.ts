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
import {Country} from "../../../../model/country";
import {UntypedFormBuilder, UntypedFormGroup, Validators} from "@angular/forms";
import {enumOptions} from "../../../../util/enum";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {FormComponentBase} from "../../../util/form/FormComponentBase";
import {HelpFocus, HelpLink, UpdateHelpLinkRequest} from "../../../../model/help-link";
import {HelpLinkService} from "../../../../services/help-link.service";
import {CandidateOpportunityStage} from "../../../../model/candidate-opportunity";
import {JobOpportunityStage} from "../../../../model/job";

@Component({
  selector: 'app-create-update-help-link',
  templateUrl: './create-update-help-link.component.html',
  styleUrls: ['./create-update-help-link.component.scss']
})
export class CreateUpdateHelpLinkComponent extends FormComponentBase implements OnInit {

  destinationCountries: Country[];
  error = null;
  form: UntypedFormGroup;
  helpLink: HelpLink;
  caseStages = enumOptions(CandidateOpportunityStage);
  jobStages = enumOptions(JobOpportunityStage);
  focuses = enumOptions(HelpFocus);
  working: boolean;


  constructor(fb: UntypedFormBuilder,
              private activeModal: NgbActiveModal,
              private helpLinkService: HelpLinkService,
  ) {
    super(fb);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      label: [this.helpLink?.label, Validators.required],
      link: [this.helpLink?.link, Validators.required],
      countryId: [this.helpLink?.country?.id],
      caseStage: [this.helpLink?.caseStage],
      jobStage: [this.helpLink?.jobStage],
      focus: [this.helpLink?.focus],
      nextStepName: [this.helpLink?.nextStepInfo?.nextStepName],
      nextStepText: [this.helpLink?.nextStepInfo?.nextStepText],
      nextStepDays: [this.helpLink?.nextStepInfo?.nextStepDays],
    });
  }

  get create(): boolean {
    return !this.helpLink;
  }

  get title(): string {
    return this.create ? "Add New Help Link"
      : "Update Help Link";
  }

  save() {
    this.error = null;
    this.working = true;

    const request: UpdateHelpLinkRequest = {
      label: this.form.value.label,
      link: this.form.value.link,
      countryId: this.form.value.countryId,
      caseStage: this.form.value.caseStage,
      jobStage: this.form.value.jobStage,
      focus: this.form.value.focus,
      nextStepInfo: {
        nextStepName: this.form.value.nextStepName,
        nextStepText: this.form.value.nextStepText,
        nextStepDays: this.form.value.nextStepDays
      }
    };

    if (this.create) {
      this.helpLinkService.create(request).subscribe(
        (helpLink: HelpLink) => {
          this.closeModal(helpLink);
          this.working = false;
        },
        (error) => {
          this.error = error;
          this.working = false;
        });
    } else {
      this.helpLinkService.update(this.helpLink.id, request).subscribe(
        (helpLink: HelpLink) => {
          this.closeModal(helpLink);
          this.working = false;
        },
        (error) => {
          this.error = error;
          this.working = false;
        });
    }
  }

  closeModal(helpLink: HelpLink) {
    this.activeModal.close(helpLink);
  }

  dismiss() {
    this.activeModal.dismiss(false);
  }

  isCreate(): boolean {
    return this.helpLink == null;
  }
}
