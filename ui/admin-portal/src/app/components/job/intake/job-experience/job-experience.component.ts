import {Component, OnInit} from '@angular/core';
import {FormBuilder} from "@angular/forms";
import {JobIntakeComponentBase} from "../../../util/intake/JobIntakeComponentBase";
import {JobOppIntakeService} from "../../../../services/job-opp-intake.service";

@Component({
  selector: 'app-job-experience',
  templateUrl: './job-experience.component.html',
  styleUrls: ['./job-experience.component.scss']
})
export class JobExperienceComponent extends JobIntakeComponentBase implements OnInit {

  constructor(fb: FormBuilder, joiService: JobOppIntakeService) {
    super(fb, joiService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      employmentExperience: [{value: this.jobIntakeData?.employmentExperience, disabled: !this.editable}],
    });
  }

}
