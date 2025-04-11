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
import {UntypedFormBuilder, UntypedFormGroup} from "@angular/forms";
import {EnumOption, enumOptions} from "../../../util/enum";
import {CandidateOpportunityParams} from "../../../model/candidate";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {
  CandidateOpportunity,
  CandidateOpportunityStage
} from "../../../model/candidate-opportunity";
import {SearchHelpLinkRequest} from "../../../model/help-link";

@Component({
  selector: 'app-edit-candidate-opp',
  templateUrl: './edit-candidate-opp.component.html',
  styleUrls: ['./edit-candidate-opp.component.scss']
})
export class EditCandidateOppComponent implements OnInit {

  //Allow for optional supply of CandidateOpportunity which can be used to prefill
  //form fields with existing values
  opp: CandidateOpportunity;

  /**
   * Determines whether progress params - stage, nextStep and nextStepDue - are captured by the
   * component.
   * <p/>
   * Sometimes those params are captured by the separate special purpose
   * OpportunityStageNextStepComponent - in which case, this can be set false.
   */
  showProgressParams = true;

  salesforceStageForm: UntypedFormGroup;
  candidateOpportunityStageOptions: EnumOption[] = enumOptions(CandidateOpportunityStage);

  closing = false;

  stageHelpRequest: SearchHelpLinkRequest;

  constructor(
    private activeModal: NgbActiveModal,
    private fb: UntypedFormBuilder) { }

  ngOnInit(): void {
    this.salesforceStageForm = this.fb.group({
      stage: [this.opp ? this.opp.stage : null],
      nextStep: [this.opp ? this.opp.nextStep : null],
      nextStepDueDate: [this.opp ? this.opp.nextStepDueDate : null],
      closingComments: [this.opp ? this.opp.closingComments : null],
      closingCommentsForCandidate: [this.opp ? this.opp.closingCommentsForCandidate : null],
      employerFeedback: [this.opp ? this.opp.employerFeedback : null]
    });

    if (this.closing) {
      this.candidateOpportunityStageOptions = this.candidateOpportunityStageOptions
         .filter(en=>en.stringValue.startsWith('Closed') )
    }

    if (this.opp) {
      this.stageHelpRequest = {caseStage: this.opp.stage}
    }
  }

  get closingComments(): string { return this.salesforceStageForm.value?.closingComments; }
  get closingCommentsForCandidate(): string { return this.salesforceStageForm.value?.closingCommentsForCandidate; }
  get employerFeedback(): string { return this.salesforceStageForm.value?.employerFeedback; }
  get nextStep(): string { return this.salesforceStageForm.value?.nextStep; }
  get nextStepDueDate(): string { return this.salesforceStageForm.value?.nextStepDueDate; }
  get stage(): string { return this.salesforceStageForm.value?.stage; }

  cancel() {
    this.activeModal.dismiss(false);
  }

  onSave() {
    const info: CandidateOpportunityParams = {
      stage: this.stage,
      nextStep: this.nextStep,
      nextStepDueDate: this.nextStepDueDate,
      closingComments: this.closingComments,
      closingCommentsForCandidate: this.closingCommentsForCandidate,
      employerFeedback: this.employerFeedback
    }
    this.activeModal.close(info)
  }

  onStageSelectionChange(stage: any) {
    this.stageHelpRequest = {caseStage: stage.key}
  }
}
