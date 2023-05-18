import {Component, OnInit} from '@angular/core';
import {FormBuilder} from "@angular/forms";
import {JobIntakeComponentBase} from "../../../util/intake/JobIntakeComponentBase";
import {JobService} from "../../../../services/job.service";

@Component({
  selector: 'app-job-location',
  templateUrl: './job-location.component.html',
  styleUrls: ['./job-location.component.scss']
})
export class JobLocationComponent extends JobIntakeComponentBase implements OnInit {

  constructor(fb: FormBuilder, jobService: JobService) {
    super(fb, jobService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      location: [{value: this.jobIntakeData?.location, disabled: !this.editable}],
    });
  }

}
