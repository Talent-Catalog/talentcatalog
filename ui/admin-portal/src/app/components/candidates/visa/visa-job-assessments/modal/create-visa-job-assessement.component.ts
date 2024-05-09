import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {JobNameAndId} from "../../../../../model/job";

@Component({
  selector: 'app-create-visa-job-assessement',
  templateUrl: './create-visa-job-assessement.component.html',
  styleUrls: ['./create-visa-job-assessement.component.scss']
})
export class CreateVisaJobAssessementComponent implements OnInit {

  error = null;
  form: FormGroup;
  saving: boolean;

  jobName: string;
  jobId: number;

  constructor(private activeModal: NgbActiveModal,
              private fb: FormBuilder) {
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

  onJobSelection(job: JobNameAndId) {
    this.jobName = job?.name;
    this.jobId = job?.id;

    //If existing name is empty, auto copy into them
    if (!this.nameControl.value) {
      this.nameControl.patchValue(this.jobName);
    }
  }

  onCancel() {
    this.activeModal.dismiss();
  }

  onSelect() {
    // const request: CreateCandidateVisaJobRequest = {
    //     name: this.name,
    //     sfJobLink: this.sfJoblink };
    // this.activeModal.close(request);
  }

  dismiss() {
    this.activeModal.dismiss(false);
  }

}
