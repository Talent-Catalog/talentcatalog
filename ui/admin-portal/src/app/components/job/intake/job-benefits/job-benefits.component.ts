import {Component, OnInit} from '@angular/core';
import {FormBuilder} from "@angular/forms";
import {JobIntakeComponentBase} from "../../../util/intake/JobIntakeComponentBase";
import {JobOppIntakeService} from "../../../../services/job-opp-intake.service";

@Component({
  selector: 'app-job-benefits',
  templateUrl: './job-benefits.component.html',
  styleUrls: ['./job-benefits.component.scss']
})
export class JobBenefitsComponent extends JobIntakeComponentBase implements OnInit {

  constructor(fb: FormBuilder, joiService: JobOppIntakeService) {
    super(fb, joiService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      benefits: [{value: this.jobIntakeData?.benefits, disabled: !this.editable}],
    });
  }

}
