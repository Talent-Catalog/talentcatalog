import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup} from "@angular/forms";
import {EnumOption, enumOptions} from "../../../util/enum";
import {CandidateOpportunityParams} from "../../../model/candidate";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {
  CandidateOpportunity,
  CandidateOpportunityStage
} from "../../../model/candidate-opportunity";

@Component({
  selector: 'app-edit-candidate-opp',
  templateUrl: './edit-candidate-opp.component.html',
  styleUrls: ['./edit-candidate-opp.component.scss']
})
export class EditCandidateOppComponent implements OnInit {

  //Allow for optional supply of CandidateOpportunity which can be used to prefill
  //form fields with existing values
  opp: CandidateOpportunity;

  salesforceStageForm: FormGroup;
  candidateOpportunityStageOptions: EnumOption[] = enumOptions(CandidateOpportunityStage);

  constructor(
    private activeModal: NgbActiveModal,
    private fb: FormBuilder) { }

  ngOnInit(): void {
    this.salesforceStageForm = this.fb.group({
      stage: [this.opp ? this.opp.stage : null],
      nextStep: [this.opp ? this.opp.nextStep : null],
      nextStepDueDate: [this.opp ? this.opp.nextStepDueDate : null],
      closingComments: [this.opp ? this.opp.closingComments : null],
      employerFeedback: [this.opp ? this.opp.employerFeedback : null]
    });
  }

  get closingComments(): string { return this.salesforceStageForm.value?.closingComments; }
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
      employerFeedback: this.employerFeedback
    }
    this.activeModal.close(info)
  }
}
