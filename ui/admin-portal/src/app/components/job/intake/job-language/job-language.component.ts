import { Component, OnInit } from '@angular/core';
import {FormBuilder} from "@angular/forms";
import {JobOppIntakeService} from "../../../../services/job-opp-intake.service";
import {JobIntakeComponentBase} from "../../../util/intake/JobIntakeComponentBase";

@Component({
  selector: 'app-job-language',
  templateUrl: './job-language.component.html',
  styleUrls: ['./job-language.component.scss']
})
export class JobLanguageComponent extends JobIntakeComponentBase implements OnInit {

  constructor(fb: FormBuilder, jobService: JobOppIntakeService) {
    super(fb, jobService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      languageRequirements: [{value: this.jobIntakeData?.languageRequirements, disabled: !this.editable}],
    });
  }

}
