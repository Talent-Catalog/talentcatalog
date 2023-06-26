import {Component, OnInit} from '@angular/core';
import {FormBuilder} from "@angular/forms";
import {JobIntakeComponentBase} from "../../../util/intake/JobIntakeComponentBase";
import {JobService} from "../../../../services/job.service";

@Component({
  selector: 'app-job-skills',
  templateUrl: './job-skills.component.html',
  styleUrls: ['./job-skills.component.scss']
})
export class JobSkillsComponent extends JobIntakeComponentBase implements OnInit {

  constructor(fb: FormBuilder, jobService: JobService) {
    super(fb, jobService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      skillRequirements: [{value: this.jobIntakeData?.skillRequirements, disabled: !this.editable}],
    });
  }

}
