import {Component, OnInit} from '@angular/core';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {CandidateService} from "../../../../../services/candidate.service";
import {Candidate} from "../../../../../model/candidate";
import {UntypedFormBuilder, UntypedFormGroup} from "@angular/forms";

@Component({
  selector: 'app-edit-candidate-aspirations',
  templateUrl: './edit-candidate-aspirations.component.html',
  styleUrl: './edit-candidate-aspirations.component.scss'
})
export class EditCandidateAspirationsComponent implements OnInit{

  candidateId: number;
  candidateForm: UntypedFormGroup;

  error;
  loading: boolean;
  saving: boolean;

  constructor(private activeModal: NgbActiveModal,
              private fb: UntypedFormBuilder,
              private candidateService: CandidateService) {
  }

  ngOnInit() {
    this.loading = true;

    this.candidateService.get(this.candidateId).subscribe(candidate => {
      this.candidateForm = this.fb.group({
        aspirations: [candidate.aspirations]
      });
      this.loading = false;
    });
  }

  onSave() {
    this.saving = true;
    this.candidateService.updateAspirations(this.candidateId, this.candidateForm.value).subscribe(
      (candidate) => {
        this.closeModal(candidate);
        this.saving = false;
      },
      (error) => {
        this.error = error;
        this.saving = false;
      });
  }

  closeModal(candidate: Candidate) {
    this.activeModal.close(candidate);
  }

  dismiss() {
    this.activeModal.dismiss(false);
  }
}
