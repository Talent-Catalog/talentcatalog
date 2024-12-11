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

import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {isCandidateOpportunity} from "../../../model/candidate-opportunity";
import {
  getOpportunityStageName,
  Opportunity,
  OpportunityProgressParams
} from "../../../model/opportunity";
import {AuthorizationService} from "../../../services/authorization.service";
import {isJob} from "../../../model/job";
import {EditOppComponent} from "../../opportunity/edit-opp/edit-opp.component";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {CandidateOpportunityService} from "../../../services/candidate-opportunity.service";
import {JobService} from "../../../services/job.service";

@Component({
  selector: 'app-opportunity-stage-next-step',
  templateUrl: './opportunity-stage-next-step.component.html',
  styleUrls: ['./opportunity-stage-next-step.component.scss']
})
export class OpportunityStageNextStepComponent implements OnInit {
  @Input() opp: Opportunity;
  @Output() oppProgressUpdated = new EventEmitter<Opportunity>();
  @Input() notEditable: boolean;

  error: string;
  updating: boolean;

  constructor(
    private authService: AuthorizationService,
    private candidateOpportunityService: CandidateOpportunityService,
    private jobService: JobService,
    private modalService: NgbModal,

  ) { }

  ngOnInit(): void {
  }

  get editable(): boolean {
    let canEdit = false;
    if (isCandidateOpportunity(this.opp)) {
      canEdit = this.authService.canEditCandidateOpp(this.opp);
    }
    if (isJob(this.opp)) {
      canEdit = this.authService.canChangeJobStage(this.opp);
    }
    if (this.notEditable) {
      canEdit = false;
    }
    return canEdit;
  }

  editOppProgress() {
    const editQuery = this.modalService.open(EditOppComponent, {size: 'lg'});
    editQuery.componentInstance.opp = this.opp;

    editQuery.result
    .then((info: OpportunityProgressParams) => {this.doUpdate(info);})
    .catch(() => { });
  }

  private doUpdate(info: OpportunityProgressParams) {
    this.updating = true;
    this.error = null;

    let updateObservable;
    if (isCandidateOpportunity(this.opp)) {
      updateObservable = this.candidateOpportunityService.updateCandidateOpportunity(this.opp.id, info);
    }
    if (isJob(this.opp)) {
      updateObservable = this.jobService.update(this.opp.id, info);
    }

    if (updateObservable) {
      updateObservable.subscribe(opp => {
          //Emit an opp updated which will refresh the display
          this.oppProgressUpdated.emit(opp);
          this.updating = false;
        },
        err => {this.error = err; this.updating = false; }
      );
    }
  }

  getOpportunityStageName(opp: Opportunity): string {
    return getOpportunityStageName(opp)
  }
}
