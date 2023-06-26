import {Component, OnInit} from '@angular/core';
import {FormBuilder} from "@angular/forms";
import {JobIntakeComponentBase} from "../../../util/intake/JobIntakeComponentBase";
import {JobService} from "../../../../services/job.service";

@Component({
  selector: 'app-job-salary',
  templateUrl: './job-salary.component.html',
  styleUrls: ['./job-salary.component.scss']
})
export class JobSalaryComponent extends JobIntakeComponentBase implements OnInit {
  constructor(fb: FormBuilder, jobService: JobService) {
    super(fb, jobService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      salaryRange: [{value: this.jobIntakeData?.salaryRange, disabled: !this.editable}],
    });
  }

}
