import {Component, OnInit} from '@angular/core';
import {
  CandidateOpportunity,
  CandidateOpportunityStage,
  isCandidateOpportunity
} from "../../../model/candidate-opportunity";
import {FormBuilder, FormGroup} from "@angular/forms";
import {EnumOption, enumOptions} from "../../../util/enum";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {Opportunity, OpportunityProgressParams} from "../../../model/opportunity";
import {isJob, JobOpportunityStage} from "../../../model/job";
import {SavedListService} from "../../../services/saved-list.service";

@Component({
  selector: 'app-edit-opp',
  templateUrl: './edit-opp.component.html',
  styleUrls: ['./edit-opp.component.scss']
})
export class EditOppComponent implements OnInit {

  opp: Opportunity;

  salesforceStageForm: FormGroup;
  opportunityStageOptions: EnumOption[] = [];

  closing = false;

  isOnlyChild: boolean;

  constructor(
    private activeModal: NgbActiveModal,
    private savedListService: SavedListService,
    private fb: FormBuilder) { }

  ngOnInit(): void {
    let stage = null;
    if (isCandidateOpportunity(this.opp) || isJob(this.opp)) {
      stage = this.opp.stage;
      if (isCandidateOpportunity(this.opp)) {
        this.opportunityStageOptions = enumOptions(CandidateOpportunityStage);
        this.setIsOnlyChild(this.opp);
      }
      if (isJob(this.opp)) {
        this.opportunityStageOptions = enumOptions(JobOpportunityStage);
      }
    }

    this.salesforceStageForm = this.fb.group({
      stage: [stage],
      nextStep: [this.opp ? this.opp.nextStep : null],
      nextStepDueDate: [this.opp ? this.opp.nextStepDueDate : null],
    });

    if (this.closing) {
      this.opportunityStageOptions = this.opportunityStageOptions
      .filter(en=>en.stringValue.startsWith('Closed') )
    }
  }

  get nextStep(): string { return this.salesforceStageForm.value?.nextStep; }
  get nextStepDueDate(): string { return this.salesforceStageForm.value?.nextStepDueDate; }
  get stage(): string { return this.salesforceStageForm.value?.stage; }

  cancel() {
    this.activeModal.dismiss(false);
  }

  onSave() {
    const info: OpportunityProgressParams = {
      stage: this.stage,
      nextStep: this.nextStep,
      nextStepDueDate: this.nextStepDueDate,
    }
    this.activeModal.close(info)
  }

  /**
   * Sets this.isOnlyChild to 'true' if there is only one candidate remaining on the parent job submission list — i.e., only one remaining child candidate opportunity (case) belonging to its parent job opportunity — in turn causing 'Copy 'next step' info to parent job opportunity?' checkbox to be displayed in the 'Update Opportunity Progress' modal.
   * @param opp candidate opportunity
   * TODO: set error message for safety here?
   */
  async setIsOnlyChild(opp: CandidateOpportunity) {
    const candidateCount = await this.savedListService.getCandidateCount(opp.jobOpp.submissionList.id).toPromise().then((candidateCount) => {
      this.isOnlyChild = candidateCount === 1;
    })
  }
}
