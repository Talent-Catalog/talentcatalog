import {Component, OnInit} from '@angular/core';
import {FormBuilder} from "@angular/forms";
import {JobService} from "../../../../services/job.service";
import {JobIntakeComponentBase} from "../../../util/intake/JobIntakeComponentBase";

@Component({
  selector: 'app-min-salary',
  templateUrl: './min-salary.component.html',
  styleUrls: ['./min-salary.component.scss']
})
export class MinSalaryComponent extends JobIntakeComponentBase implements OnInit {
  constructor(fb: FormBuilder, jobService: JobService) {
    super(fb, jobService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      minSalary: [{value: this.jobIntakeData?.minSalary, disabled: !this.editable}],
    });
  }

}
