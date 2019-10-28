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
  showCandidateMessage: boolean;

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
          candidateMessage: [candidate.candidateMessage],
        });

        this.showCandidateMessage = candidate.status == 'incomplete';

        this.candidateForm.get('status').valueChanges.subscribe(value => {
          this.showCandidateMessage = value == 'incomplete'
        });
        this.loading = false;
      });

  }

   onSave() {
    this.error = null;
    if (this.candidateForm.value.status == 'incomplete' && !this.candidateForm.value.candidateMessage || this.candidateForm.value.candidateMessage.length < 1){
      this.error = 'Please enter a message for the candidate about what they need to complete';
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
}
