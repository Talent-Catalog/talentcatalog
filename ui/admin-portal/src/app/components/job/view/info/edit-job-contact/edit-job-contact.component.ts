import {Component, OnInit} from '@angular/core';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {FormBuilder, FormGroup} from "@angular/forms";
import {JobService} from "../../../../../services/job.service";
import {Job} from "../../../../../model/job";

@Component({
  selector: 'app-edit-job-contact',
  templateUrl: './edit-job-contact.component.html',
  styleUrls: ['./edit-job-contact.component.scss']
})
export class EditJobContactComponent implements OnInit {

  jobId: number;

  jobForm: FormGroup;

  error;
  loading: boolean;
  saving: boolean;

  constructor(private activeModal: NgbActiveModal,
              private fb: FormBuilder,
              private jobService: JobService) { }

  ngOnInit(): void {
    this.loading = true;
    this.jobService.get(this.jobId).subscribe(job => {
        this.jobForm = this.fb.group({
          submissionDueDate: [job.submissionDueDate],
        });
        this.loading = false;
      },
      (error) => {
        this.error = error;
        this.loading = false;
      }
    );
  }

  onSave() {
    this.error = null;
    this.saving = true;
    this.jobService.update(this.jobId, this.jobForm.value).subscribe(
      (job) => {
        this.closeModal(job);
        this.saving = false;
      },
      (error) => {
        this.error = error;
        this.saving = false;
      });
  }

  closeModal(job: Job) {
    this.activeModal.close(job);
  }

  dismiss() {
    this.activeModal.dismiss(false);
  }

}
