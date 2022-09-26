import {Component, OnInit} from '@angular/core';
import {UntypedFormBuilder, UntypedFormGroup} from "@angular/forms";
import {EnumOption, enumOptions} from "../../../util/enum";
import {CandidateOpportunityStage, SalesforceOppParams} from "../../../model/candidate";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";

export interface SalesforceStageInfo {
  stageName?: string;
  nextStep?: string;
}

@Component({
  selector: 'app-salesforce-stage',
  templateUrl: './salesforce-stage.component.html',
  styleUrls: ['./salesforce-stage.component.scss']
})
export class SalesforceStageComponent implements OnInit {

  salesforceStageForm: UntypedFormGroup;
  candidateOpportunityStageOptions: EnumOption[] = enumOptions(CandidateOpportunityStage);

  constructor(
    private activeModal: NgbActiveModal,
    private fb: UntypedFormBuilder) { }

  ngOnInit(): void {
    this.salesforceStageForm = this.fb.group({
      stage: [null],
      nextStep: [null],
      closingComments: [null],
      employerFeedback: [null]
    });
  }

  get closingComments(): string { return this.salesforceStageForm.value?.closingComments; }
  get employerFeedback(): string { return this.salesforceStageForm.value?.employerFeedback; }
  get nextStep(): string { return this.salesforceStageForm.value?.nextStep; }
  get stage(): string { return this.salesforceStageForm.value?.stage; }

  cancel() {
    this.activeModal.dismiss(false);
  }

  onSave() {
    const info: SalesforceOppParams = {
      stage: this.stage,
      nextStep: this.nextStep,
      closingComments: this.closingComments,
      employerFeedback: this.employerFeedback
    }
    this.activeModal.close(info)
  }
}
