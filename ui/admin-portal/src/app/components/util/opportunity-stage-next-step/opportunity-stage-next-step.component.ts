import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {isCandidateOpportunity} from "../../../model/candidate-opportunity";
import {getOpportunityStageName, Opportunity, OpportunityProgressParams} from "../../../model/opportunity";
import {AuthService} from "../../../services/auth.service";
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
    private authService: AuthService,
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
