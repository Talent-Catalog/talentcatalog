import {Component, OnInit} from '@angular/core';
import {FormBuilder} from "@angular/forms";
import {JobIntakeComponentBase} from "../../../util/intake/JobIntakeComponentBase";
import {JobService} from "../../../../services/job.service";

@Component({
  selector: 'app-cost-commit-employer',
  templateUrl: './cost-commit-employer.component.html',
  styleUrls: ['./cost-commit-employer.component.scss']
})
export class CostCommitEmployerComponent extends JobIntakeComponentBase implements OnInit {
  tooltip = "Costs that employer has agreed to pay. Typically visa costs and airfares.";

  constructor(fb: FormBuilder, jobService: JobService) {
    super(fb, jobService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      employerCostCommitment: [{value: this.jobIntakeData?.employerCostCommitment, disabled: !this.editable}],
    });
  }

  onSuccessfulSave() {
    //Update employerCostCommitment in this.jobIntakeData with the form data.
    //Note that we have to this because the save returns void, not the updated intake.
    //This is for performance reasons when doing autosaves - we don't want the server to be
    //continually sending back updated intakes.
    let commitment = this.form.controls.employerCostCommitment.value;
    this.jobIntakeData.employerCostCommitment = commitment;

    //We need to output an event with this new intake data so that the parent job can be updated
    //and also so that the logic which checks whether an intake is complete can be triggered
    //and displayed. See JobPrepJOI in the JobPrepItems of ViewJobComponent.
    //Note that we don't do this for all intake components. We probably should, but currently just
    //employer cost commitment is used in JobPrepJOI to determine intake completeness.
    this.intakeChanged.emit(this.jobIntakeData);
  }
}
