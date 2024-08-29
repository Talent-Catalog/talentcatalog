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
  @Input() editable: boolean;
  @Input() nRows: number = 3;
  @Input() highlight: boolean;
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
    this.error = null;
    this.saving = true;

    const summary = this.form.value.jobSummary;
    //Update the local job object.
    this.job.jobSummary = summary;
    //And save the change on the server as well.
    this.jobService.updateSummary(this.job.id, summary).subscribe(
      (job) => {
        this.jobSummaryControl.markAsPristine();
        this.saving = false;
      },
      (error) => {
        this.error = error;
        this.saving = false;
      });
  }
}
