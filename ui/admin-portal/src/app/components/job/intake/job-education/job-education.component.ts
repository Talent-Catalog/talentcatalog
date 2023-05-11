import {Component, OnInit} from '@angular/core';
import {FormBuilder} from "@angular/forms";
import {JobIntakeComponentBase} from "../../../util/intake/JobIntakeComponentBase";
import {JobOppIntakeService} from "../../../../services/job-opp-intake.service";

@Component({
  selector: 'app-job-education',
  templateUrl: './job-education.component.html',
  styleUrls: ['./job-education.component.scss']
})
export class JobEducationComponent extends JobIntakeComponentBase implements OnInit {

  constructor(fb: FormBuilder, joiService: JobOppIntakeService) {
    super(fb, joiService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      educationRequirements: [{value: this.jobIntakeData?.educationRequirements, disabled: !this.editable}],
    });
  }

}
