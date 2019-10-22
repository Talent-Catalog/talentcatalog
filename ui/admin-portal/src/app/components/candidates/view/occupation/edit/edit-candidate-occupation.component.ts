import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {CandidateOccupation} from "../../../../../model/candidate-occupation";
import {CandidateOccupationService} from "../../../../../services/candidate-occupation.service";

@Component({
  selector: 'app-edit-candidate-occupation',
  templateUrl: './edit-candidate-occupation.component.html',
  styleUrls: ['./edit-candidate-occupation.component.scss']
})
export class EditCandidateOccupationComponent implements OnInit {

  candidateOccupation: CandidateOccupation;

  form: FormGroup;

  countries = [];
  years = [];
  error;
  loading: boolean;
  saving: boolean;

  constructor(private activeModal: NgbActiveModal,
              private fb: FormBuilder,
              private candidateOccupationService: CandidateOccupationService) {
  }

  ngOnInit() {
    this.form = this.fb.group({
      verified: [this.candidateOccupation.verified, Validators.required],
      comment: [null, Validators.required]
    });
  }

  onSave() {
    this.saving = true;
    this.candidateOccupationService.update(this.candidateOccupation.id, this.form.value).subscribe(
      (candidateOccupation) => {
        this.closeModal(candidateOccupation);
        this.saving = false;
      },
      (error) => {
        this.error = error;
        this.saving = false;
      });
  }

  closeModal(candidateOccupation: CandidateOccupation) {
    this.activeModal.close(candidateOccupation);
  }

  dismiss() {
    this.activeModal.dismiss(false);
  }
}
