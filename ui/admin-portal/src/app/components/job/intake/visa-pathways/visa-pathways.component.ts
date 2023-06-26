import {Component, OnInit} from '@angular/core';
import {JobIntakeComponentBase} from "../../../util/intake/JobIntakeComponentBase";
import {FormBuilder} from "@angular/forms";
import {JobService} from "../../../../services/job.service";

@Component({
  selector: 'app-visa-pathways',
  templateUrl: './visa-pathways.component.html',
  styleUrls: ['./visa-pathways.component.scss']
})
export class VisaPathwaysComponent extends JobIntakeComponentBase implements OnInit {

  constructor(fb: FormBuilder,
              jobService: JobService) {
    super(fb, jobService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      visaPathways: [{value: this.jobIntakeData?.visaPathways, disabled: !this.editable}],
    });
  }

}
