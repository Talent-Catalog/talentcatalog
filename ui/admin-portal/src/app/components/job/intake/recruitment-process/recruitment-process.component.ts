import {Component, OnInit} from '@angular/core';
import {JobIntakeComponentBase} from "../../../util/intake/JobIntakeComponentBase";
import {FormBuilder} from "@angular/forms";
import {JobOppIntakeService} from "../../../../services/job-opp-intake.service";

@Component({
  selector: 'app-recruitment-process',
  templateUrl: './recruitment-process.component.html',
  styleUrls: ['./recruitment-process.component.scss']
})
export class RecruitmentProcessComponent extends JobIntakeComponentBase implements OnInit {

  constructor(fb: FormBuilder, jobService: JobOppIntakeService) {
    super(fb, jobService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      recruitmentProcess: [{value: this.jobIntakeData?.recruitmentProcess, disabled: !this.editable}],
    });
  }

}
