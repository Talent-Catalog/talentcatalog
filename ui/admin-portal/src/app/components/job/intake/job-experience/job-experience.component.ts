import {Component, OnInit} from '@angular/core';
import {FormBuilder} from "@angular/forms";
import {JobIntakeComponentBase} from "../../../util/intake/JobIntakeComponentBase";
import {JobService} from "../../../../services/job.service";

@Component({
  selector: 'app-job-experience',
  templateUrl: './job-experience.component.html',
  styleUrls: ['./job-experience.component.scss']
})
export class JobExperienceComponent extends JobIntakeComponentBase implements OnInit {

  constructor(fb: FormBuilder, jobService: JobService) {
    super(fb, jobService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      employmentExperience: [{value: this.jobIntakeData?.employmentExperience, disabled: !this.editable}],
    });
  }

}
