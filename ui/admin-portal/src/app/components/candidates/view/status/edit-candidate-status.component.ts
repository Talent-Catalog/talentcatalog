import {Component, OnInit} from '@angular/core';
import {CandidateService} from '../../../../services/candidate.service';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {Candidate} from "../../../../model/candidate";

@Component({
  selector: 'app-edit-candidate',
  templateUrl: './edit-candidate-status.component.html',
  styleUrls: ['./edit-candidate-status.component.scss']
})
export class EditCandidateStatusComponent implements OnInit {

  candidateId: number;
  candidateForm: FormGroup;
  error;
  loading: boolean;
  saving: boolean;

  constructor(private activeModal: NgbActiveModal,
              private fb: FormBuilder,
              private candidateService: CandidateService) {
  }

  ngOnInit() {
      this.loading = true;
      this.candidateService.get(this.candidateId).subscribe(candidate => {
        this.candidateForm = this.fb.group({
          status: [candidate.status, Validators.required],
          comment: [null, Validators.required],
          candidateMessage: [candidate.candidateMessage || ''],
        });
        this.loading = false;
      });
  }

   onSave() {
    this.error = null;

    const val = this.candidateForm.value;
    if (this.showCandidateMessage && (!val.candidateMessage || val.candidateMessage.length < 1)) {
      this.error = 'Please enter a message for the candidate about what they need to complete';
      return;
    }

    this.saving = true;
    this.candidateService.updateStatus(this.candidateId, this.candidateForm.value).subscribe(
      (candidate) => {
        this.saving = false;
        this.closeModal(candidate);
      },
      (error) => {
        this.error = error;
        this.saving = false;
      });
  }

  closeModal(candidate: Candidate) {
    this.activeModal.close(candidate);
  }

  cancel() {
    this.activeModal.dismiss();
  }

  get showCandidateMessage() {
    if (this.loading || !this.candidateForm) {
      return false;
    }
    return this.candidateForm.controls.status.value === 'incomplete';
  }
}
