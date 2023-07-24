import {Component, OnInit} from '@angular/core';
import {CandidateOpportunityStage, isCandidateOpportunity} from "../../../model/candidate-opportunity";
import {FormBuilder, FormGroup} from "@angular/forms";
import {EnumOption, enumOptions} from "../../../util/enum";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {Opportunity, OpportunityProgressParams} from "../../../model/opportunity";
import {isJob, JobOpportunityStage} from "../../../model/job";
import {AuthService} from "../../../services/auth.service";

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

  constructor(
    private activeModal: NgbActiveModal,
    private authService: AuthService,
    private fb: FormBuilder) { }

  ngOnInit(): void {
    let stage = null;
    if (isCandidateOpportunity(this.opp) || isJob(this.opp)) {
      stage = this.opp.stage;
      if (isCandidateOpportunity(this.opp)) {
        this.opportunityStageOptions = enumOptions(CandidateOpportunityStage);
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

  showCopyToParentOption(): boolean {
    return this.authService.isOnlyChild(this.opp);
  }

}
