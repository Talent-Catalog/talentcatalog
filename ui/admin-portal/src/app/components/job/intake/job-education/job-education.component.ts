import {Component, OnInit} from '@angular/core';
import {FormBuilder} from "@angular/forms";
import {JobService} from "../../../../services/job.service";
import {JobIntakeComponentBase} from "../../../util/intake/JobIntakeComponentBase";

@Component({
  selector: 'app-job-education',
  templateUrl: './job-education.component.html',
  styleUrls: ['./job-education.component.scss']
})
export class JobEducationComponent extends JobIntakeComponentBase implements OnInit {

  constructor(fb: FormBuilder, jobService: JobService) {
    super(fb, jobService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      education: [{value: this.jobIntakeData?.educationRequirements, disabled: !this.editable}],
    });
  }

}
