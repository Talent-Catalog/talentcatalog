import { Component, OnInit } from '@angular/core';
import {FormBuilder} from "@angular/forms";
import {JobIntakeComponentBase} from "../../../util/intake/JobIntakeComponentBase";
import {JobOppIntakeService} from "../../../../services/job-opp-intake.service";

@Component({
  selector: 'app-cost-commit-employer',
  templateUrl: './cost-commit-employer.component.html',
  styleUrls: ['./cost-commit-employer.component.scss']
})
export class CostCommitEmployerComponent extends JobIntakeComponentBase implements OnInit {

  constructor(fb: FormBuilder, joiService: JobOppIntakeService) {
    super(fb, joiService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      employerCostCommitment: [{value: this.jobIntakeData?.employerCostCommitment, disabled: !this.editable}],
    });
  }

}
