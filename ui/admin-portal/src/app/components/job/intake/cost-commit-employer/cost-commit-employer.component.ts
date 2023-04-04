import { Component, OnInit } from '@angular/core';
import {FormBuilder} from "@angular/forms";
import {JobService} from "../../../../services/job.service";
import {JobIntakeComponentBase} from "../../../util/intake/JobIntakeComponentBase";

@Component({
  selector: 'app-cost-commit-employer',
  templateUrl: './cost-commit-employer.component.html',
  styleUrls: ['./cost-commit-employer.component.scss']
})
export class CostCommitEmployerComponent extends JobIntakeComponentBase implements OnInit {

  constructor(fb: FormBuilder, jobService: JobService) {
    super(fb, jobService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      costCommitEmployer: [{value: this.jobIntakeData?.costCommitEmployer, disabled: !this.editable}],
    });
  }

}
