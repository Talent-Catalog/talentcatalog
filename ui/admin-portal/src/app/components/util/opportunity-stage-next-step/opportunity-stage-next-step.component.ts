import {Component, Input, OnInit} from '@angular/core';
import {isCandidateOpportunity} from "../../../model/candidate-opportunity";
import {getOpportunityStageName, Opportunity} from "../../../model/opportunity";
import {AuthService} from "../../../services/auth.service";
import {isJob} from "../../../model/job";

@Component({
  selector: 'app-opportunity-stage-next-step',
  templateUrl: './opportunity-stage-next-step.component.html',
  styleUrls: ['./opportunity-stage-next-step.component.scss']
})
export class OpportunityStageNextStepComponent implements OnInit {
  @Input() opp: Opportunity;

  error: string;
  updating: boolean;

  constructor(
    private authService: AuthService,

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
    return canEdit;
  }

  editOppProgress() {
    //todo  const editQuery = this.modalService.open(EditCandidateOppComponent, {size: 'lg'});
    // editQuery.componentInstance.opp = this.opp;
    //
    // editQuery.result
    // .then((info: CandidateOpportunityParams) => {this.doUpdate(info);})
    // .catch(() => { });
  }

  getOpportunityStageName(opp: Opportunity): string {
    return getOpportunityStageName(opp)
  }
}
