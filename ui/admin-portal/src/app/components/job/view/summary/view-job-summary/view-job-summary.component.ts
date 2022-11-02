import {Component, Input, OnInit} from '@angular/core';
import {Job} from "../../../../../model/job";
import {AbstractControl, FormBuilder, FormGroup} from "@angular/forms";
import {CandidateService} from "../../../../../services/candidate.service";

@Component({
  selector: 'app-view-job-summary',
  templateUrl: './view-job-summary.component.html',
  styleUrls: ['./view-job-summary.component.scss']
})
export class ViewJobSummaryComponent implements OnInit {
  @Input() job: Job;

  form: FormGroup;
  error: any;

  constructor(private fb: FormBuilder, private candidateService: CandidateService) { }

  ngOnInit(): void {
    this.form = this.fb.group({
      jobSummary: [this.job.jobSummary],
    });
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
