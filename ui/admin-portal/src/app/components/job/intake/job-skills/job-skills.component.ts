import {Component, OnInit} from '@angular/core';
import {FormBuilder} from "@angular/forms";
import {JobOppIntakeService} from "../../../../services/job-opp-intake.service";
import {JobIntakeComponentBase} from "../../../util/intake/JobIntakeComponentBase";

@Component({
  selector: 'app-job-skills',
  templateUrl: './job-skills.component.html',
  styleUrls: ['./job-skills.component.scss']
})
export class JobSkillsComponent extends JobIntakeComponentBase implements OnInit {

  constructor(fb: FormBuilder, jobService: JobOppIntakeService) {
    super(fb, jobService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      skillRequirements: [{value: this.jobIntakeData?.skillRequirements, disabled: !this.editable}],
    });
  }

}
