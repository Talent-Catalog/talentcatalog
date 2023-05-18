import {Component, OnInit} from '@angular/core';
import {FormBuilder} from "@angular/forms";
import {JobIntakeComponentBase} from "../../../util/intake/JobIntakeComponentBase";
import {JobService} from "../../../../services/job.service";

@Component({
  selector: 'app-job-benefits',
  templateUrl: './job-benefits.component.html',
  styleUrls: ['./job-benefits.component.scss']
})
export class JobBenefitsComponent extends JobIntakeComponentBase implements OnInit {

  constructor(fb: FormBuilder, jobService: JobService) {
    super(fb, jobService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      benefits: [{value: this.jobIntakeData?.benefits, disabled: !this.editable}],
    });
  }

}
