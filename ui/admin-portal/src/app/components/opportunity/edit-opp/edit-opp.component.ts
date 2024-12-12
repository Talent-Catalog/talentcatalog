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
import {
  CandidateOpportunity,
  CandidateOpportunityStage,
  isCandidateOpportunity
} from "../../../model/candidate-opportunity";
import {UntypedFormBuilder, UntypedFormGroup} from "@angular/forms";
import {EnumOption, enumOptions} from "../../../util/enum";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {Opportunity, OpportunityProgressParams} from "../../../model/opportunity";
import {isJob, JobOpportunityStage} from "../../../model/job";
import {CandidateSourceCandidateService} from "../../../services/candidate-source-candidate.service";
import {SavedListService} from "../../../services/saved-list.service";
import {switchMap} from "rxjs/operators";
import {SearchCandidateSourcesRequest} from "../../../model/base";
import {JobService} from "../../../services/job.service";
import {SearchHelpLinkRequest} from "../../../model/help-link";

@Component({
  selector: 'app-edit-opp',
  templateUrl: './edit-opp.component.html',
  styleUrls: ['./edit-opp.component.scss']
})
export class EditOppComponent implements OnInit {

  opp: Opportunity;

  salesforceStageForm: UntypedFormGroup;
  opportunityStageOptions: EnumOption[] = [];

  closing = false;

  oppType: string;

  /**
   * When set to true, this will indicate that the given case is the only remaining child of its job parent
   */
  isOnlyOpenCaseOfParentJob: boolean;

  error;

  stageHelpRequest: SearchHelpLinkRequest;

  constructor(
    private activeModal: NgbActiveModal,
    private savedListService: SavedListService,
    private candidateSourceCandidateService: CandidateSourceCandidateService,
    private jobService: JobService,
    private fb: UntypedFormBuilder) { }

  ngOnInit(): void {
    let stage = null;
    if (isCandidateOpportunity(this.opp) || isJob(this.opp)) {
      stage = this.opp.stage;
      if (isCandidateOpportunity(this.opp)) {
        this.opportunityStageOptions = enumOptions(CandidateOpportunityStage);
        this.checkOpenCases(this.opp);
        this.oppType = 'Candidate Opportunity';
        this.stageHelpRequest = {caseOppId: this.opp.id, caseStage: this.opp.stage}
      }
      if (isJob(this.opp)) {
        this.opportunityStageOptions = enumOptions(JobOpportunityStage);
        this.oppType = 'Job'
        this.stageHelpRequest = {jobOppId: this.opp.id, jobStage: this.opp.stage}
      }
    }

    this.salesforceStageForm = this.fb.group({
      stage: [stage],
      nextStep: [this.opp ? this.opp.nextStep : null],
      nextStepDueDate: [this.opp ? this.opp.nextStepDueDate : null],
      copyToParentJob: [false]
    });

    if (this.closing) {
      this.opportunityStageOptions = this.opportunityStageOptions
      .filter(en=>en.stringValue.startsWith('Closed') )
    }
  }

  get nextStep(): string { return this.salesforceStageForm.value?.nextStep; }
  get nextStepDueDate(): string { return this.salesforceStageForm.value?.nextStepDueDate; }
  get stage(): string { return this.salesforceStageForm.value?.stage; }
  get copyToParentJob(): boolean { return this.salesforceStageForm.value?.copyToParentJob; }

  cancel() {
    this.activeModal.dismiss(false);
  }

  onSave() {
    const info: OpportunityProgressParams = {
      stage: this.stage,
      nextStep: this.nextStep,
      nextStepDueDate: this.nextStepDueDate,
    }
    const parentJobInfo: OpportunityProgressParams = {
      nextStep: this.nextStep,
      nextStepDueDate: this.nextStepDueDate,
    }
    if (this.copyToParentJob && isCandidateOpportunity(this.opp)) {
      this.jobService.update(this.opp.jobOpp.id, parentJobInfo).subscribe(() => {
          this.activeModal.close(info);
        },
        (error) => this.error = error);
      } else {
        this.activeModal.close(info)
    }
  }

  /**
   * If opp is the only open case of its parent job, sets isOnlyOpenCaseOfParentJob to true — the job has a ShortSavedList property but checking for open cases requires the full SavedList, so this is retrieved before proceeding.
   * @param opp CandidateOpportunity/case whose progress is being updated
   *
   */
  checkOpenCases(opp: CandidateOpportunity) {
    if (!this.opp.closed) {
      const request: SearchCandidateSourcesRequest = {
        keyword: null, global: null, owned: null, shared: null
      }
      this.savedListService
        .get(opp.jobOpp.submissionList.id)
        .pipe(
          switchMap((fullList) =>
            this.candidateSourceCandidateService.search(fullList, request)
          )
        )
        .subscribe((result) => {
          this.isOnlyOpenCaseOfParentJob = (result.length === 1);
        },
          (error) => this.error = error);
      }
    }

  isEvergreenJob(): boolean {
    return isJob(this.opp) && this.opp.evergreen;
  }

  onStageSelectionChange(stage: any) {
    if (isCandidateOpportunity(this.opp)) {
      this.stageHelpRequest = {caseOppId: this.opp.id, caseStage: stage.key}
    }
    if (isJob(this.opp)) {
      this.stageHelpRequest = {jobOppId: this.opp.id, jobStage: stage.key}
    }

  }
}
