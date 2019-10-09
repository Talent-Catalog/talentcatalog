import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {CandidateService} from '../../../../services/candidate.service';
import {FormBuilder, FormGroup} from '@angular/forms';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {LanguageLevel} from "../../../../model/language-level";
import {Candidate} from "../../../../model/candidate";

@Component({
  selector: 'app-edit-candidate',
  templateUrl: './edit-candidate.component.html',
  styleUrls: ['./edit-candidate.component.scss']
})
export class EditCandidateComponent implements OnInit {

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
          firstName: [candidate.user.firstName],
          lastName: [candidate.user.lastName],
          gender: [candidate.gender],
        });
        this.loading = false;
      });
  }

  onSave() {
    this.saving = true;
    this.candidateService.update(this.candidateId, this.candidateForm.value).subscribe(
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
