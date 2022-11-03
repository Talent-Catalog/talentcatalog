import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {Job} from "../../../../../model/job";
import {AbstractControl, FormBuilder, FormGroup} from "@angular/forms";
import {JobService} from "../../../../../services/job.service";

@Component({
  selector: 'app-view-job-summary',
  templateUrl: './view-job-summary.component.html',
  styleUrls: ['./view-job-summary.component.scss']
})
export class ViewJobSummaryComponent implements OnInit, OnChanges {
  @Input() job: Job;

  form: FormGroup;
  error: any;
  saving: boolean;

  constructor(private fb: FormBuilder, private jobService: JobService) { }

  ngOnInit(): void {
    this.form = this.fb.group({
      jobSummary: [this.job.jobSummary],
    });
  }

  ngOnChanges(changes: SimpleChanges): void {
    const change = changes.job;
    if (change && change.previousValue !== change.currentValue) {
      //The very first change is fired before ngOnInt has been called.
      //So don't update form if it is not there yet.
      if (this.form) {
        this.jobSummaryControl.patchValue(this.job.jobSummary)
      }
    }
  }

  get jobSummaryControl(): AbstractControl {
    return this.form.get('jobSummary');
  }

  cancelChanges() {
    this.jobSummaryControl.patchValue(this.job.jobSummary);
    this.jobSummaryControl.markAsPristine();
  }

  saveChanges() {
    //todo
  }
}
