import { Component, OnInit } from '@angular/core';
import {JoblinkValidationEvent} from "../../util/joblink/joblink.component";

@Component({
  selector: 'app-new-job',
  templateUrl: './new-job.component.html',
  styleUrls: ['./new-job.component.scss']
})
export class NewJobComponent implements OnInit {
  error = null;
  jobName: string;
  saving: boolean;
  sfJoblink: string;

  constructor() { }

  ngOnInit(): void {
    this.error = null;
  }

  onJoblinkValidation(jobOpportunity: JoblinkValidationEvent) {
    if (jobOpportunity.valid) {
      this.sfJoblink = jobOpportunity.sfJoblink;
      this.jobName = jobOpportunity.jobname;
    } else {
      this.sfJoblink = null;
      this.jobName = null;
    }
  }

  create() {
    //todo Workflow goes here to register new job
  }
}
