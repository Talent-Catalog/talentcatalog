import { Component, OnInit } from '@angular/core';
import {Candidate} from "../../../../../model/candidate";
import {FormBuilder, FormGroup} from "@angular/forms";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {CandidateService} from "../../../../../services/candidate.service";

@Component({
  selector: 'app-edit-candidate-special-links',
  templateUrl: './edit-candidate-special-links.component.html',
  styleUrls: ['./edit-candidate-special-links.component.scss']
})
export class EditCandidateSpecialLinksComponent implements OnInit {

  candidateId: number;
  candidateForm: FormGroup;

  error;
  loading: boolean;
  saving: boolean;

  constructor(private activeModal: NgbActiveModal,
              private fb: FormBuilder,
              private candidateService: CandidateService) { }

  ngOnInit() {
    this.loading = true;

    this.candidateService.get(this.candidateId).subscribe(candidate => {
      this.candidateForm = this.fb.group({
        sflink: [candidate.sflink],
        folderlink: [candidate.folderlink],
      });
      this.loading = false;
    });
  }

  onSave() {
    this.saving = true;
    this.candidateService.updateLinks(this.candidateId, this.candidateForm.value).subscribe(
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
