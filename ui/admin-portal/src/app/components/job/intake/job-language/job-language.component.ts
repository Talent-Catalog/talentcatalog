import {Component, OnInit} from '@angular/core';
import {FormBuilder} from "@angular/forms";
import {JobIntakeComponentBase} from "../../../util/intake/JobIntakeComponentBase";
import {JobService} from "../../../../services/job.service";

@Component({
  selector: 'app-job-language',
  templateUrl: './job-language.component.html',
  styleUrls: ['./job-language.component.scss']
})
export class JobLanguageComponent extends JobIntakeComponentBase implements OnInit {

  constructor(fb: FormBuilder, jobService: JobService) {
    super(fb, jobService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      languageRequirements: [{value: this.jobIntakeData?.languageRequirements, disabled: !this.editable}],
    });
  }

}
