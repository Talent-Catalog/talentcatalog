import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup} from '@angular/forms';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {CandidateService} from "../../../../../services/candidate.service";
import {Candidate} from "../../../../../model/candidate";
import {NationalityService} from "../../../../../services/nationality.service";
import {CountryService} from "../../../../../services/country.service";

@Component({
  selector: 'app-edit-candidate-additional-info',
  templateUrl: './edit-candidate-additional-info.component.html',
  styleUrls: ['./edit-candidate-additional-info.component.scss']
})
export class EditCandidateAdditionalInfoComponent implements OnInit {

  candidateId: number;

  candidateForm: FormGroup;

  error;
  loading: boolean;
  saving: boolean;

  constructor(private activeModal: NgbActiveModal,
              private fb: FormBuilder,
              private candidateService: CandidateService,
              private nationalityService: NationalityService,
              private countryService: CountryService ) {
  }

  ngOnInit() {
    this.loading = true;

    this.candidateService.get(this.candidateId).subscribe(candidate => {
      this.candidateForm = this.fb.group({
        additionalInfo: [candidate.additionalInfo],
      });
      this.loading = false;
    });
  }

  onSave() {
    this.saving = true;
    this.candidateService.updateInfo(this.candidateId, this.candidateForm.value).subscribe(
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
