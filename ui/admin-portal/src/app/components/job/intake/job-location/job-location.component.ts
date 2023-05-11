import {Component, OnInit} from '@angular/core';
import {FormBuilder} from "@angular/forms";
import {JobOppIntakeService} from "../../../../services/job-opp-intake.service";
import {JobIntakeComponentBase} from "../../../util/intake/JobIntakeComponentBase";

@Component({
  selector: 'app-job-location',
  templateUrl: './job-location.component.html',
  styleUrls: ['./job-location.component.scss']
})
export class JobLocationComponent extends JobIntakeComponentBase implements OnInit {

  constructor(fb: FormBuilder, jobService: JobOppIntakeService) {
    super(fb, jobService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      location: [{value: this.jobIntakeData?.location, disabled: !this.editable}],
    });
  }

}
