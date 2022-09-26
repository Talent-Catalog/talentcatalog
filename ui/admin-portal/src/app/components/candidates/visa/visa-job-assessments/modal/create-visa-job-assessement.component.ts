import {Component, OnInit} from '@angular/core';
import {UntypedFormBuilder, UntypedFormGroup, Validators} from '@angular/forms';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {JoblinkValidationEvent} from '../../../../util/joblink/joblink.component';
import {CreateCandidateVisaJobRequest} from "../../../../../services/candidate-visa-job.service";

@Component({
  selector: 'app-create-visa-job-assessement',
  templateUrl: './create-visa-job-assessement.component.html',
  styleUrls: ['./create-visa-job-assessement.component.scss']
})
export class CreateVisaJobAssessementComponent implements OnInit {

  error = null;
  form: UntypedFormGroup;
  saving: boolean;

  jobName: string;
  sfJoblink: string;

  constructor(private activeModal: NgbActiveModal,
              private fb: UntypedFormBuilder) {
  }

  ngOnInit() {
    this.form = this.fb.group({
      name: ['', Validators.required],
      link: ['', Validators.required],
    });
  }

  get nameControl() { return this.form.get('name'); }

  get name() { return this.form.value.name; }

  get link() { return this.form.value.link; }

  onJoblinkValidation(jobOpportunity: JoblinkValidationEvent) {
    if (jobOpportunity.valid) {
      this.sfJoblink = jobOpportunity.sfJoblink;
      this.jobName = jobOpportunity.jobname;

      //If existing name is empty, auto copy into them
      if (!this.nameControl.value) {
        this.nameControl.patchValue(this.jobName);
      }
    } else {
      this.sfJoblink = null;
      this.jobName = null;
    }
  }

  onCancel() {
    this.activeModal.dismiss();
  }

  onSelect() {
    const request: CreateCandidateVisaJobRequest = {
        name: this.name,
        sfJobLink: this.sfJoblink };
    this.activeModal.close(request);
  }

  dismiss() {
    this.activeModal.dismiss(false);
  }

}
