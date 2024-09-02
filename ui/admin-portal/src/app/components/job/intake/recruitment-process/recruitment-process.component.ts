import {Component, OnInit} from '@angular/core';
import {JobIntakeComponentBase} from "../../../util/intake/JobIntakeComponentBase";
import {FormBuilder} from "@angular/forms";
import {JobService} from "../../../../services/job.service";

@Component({
  selector: 'app-recruitment-process',
  templateUrl: './recruitment-process.component.html',
  styleUrls: ['./recruitment-process.component.scss']
})
export class RecruitmentProcessComponent extends JobIntakeComponentBase implements OnInit {

  tooltip = "Provide a brief description of the recruitment process so that candidates " +
    "and source partners know what is expected.";

  constructor(fb: FormBuilder, jobService: JobService) {
    super(fb, jobService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      recruitmentProcess: [{value: this.jobIntakeData?.recruitmentProcess, disabled: !this.editable}],
    });
  }

}
