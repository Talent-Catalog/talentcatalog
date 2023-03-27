import {Component, OnInit} from '@angular/core';
import {JobIntakeComponentBase} from "../../../util/intake/JobIntakeComponentBase";
import {FormBuilder} from "@angular/forms";
import {JobService} from "../../../../services/job.service";

@Component({
  selector: 'app-occupation-code',
  templateUrl: './occupation-code.component.html',
  styleUrls: ['./occupation-code.component.scss']
})
export class OccupationCodeComponent extends JobIntakeComponentBase implements OnInit {
  constructor(fb: FormBuilder, jobService: JobService) {
    super(fb, jobService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      occupationCode: [{value: this.jobIntakeData?.occupationCode, disabled: !this.editable}],
    });
  }

}
