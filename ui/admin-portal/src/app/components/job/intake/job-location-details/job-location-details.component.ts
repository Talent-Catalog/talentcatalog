import {Component, OnInit} from '@angular/core';
import {FormBuilder} from "@angular/forms";
import {JobIntakeComponentBase} from "../../../util/intake/JobIntakeComponentBase";
import {JobService} from "../../../../services/job.service";

@Component({
  selector: 'app-job-location-details',
  templateUrl: './job-location-details.component.html',
  styleUrls: ['./job-location-details.component.scss']
})
export class JobLocationDetailsComponent extends JobIntakeComponentBase implements OnInit {

  constructor(fb: FormBuilder, jobService: JobService) {
    super(fb, jobService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      locationDetails: [{value: this.jobIntakeData?.locationDetails, disabled: !this.editable}],
    });
  }

}
