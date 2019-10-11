import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {CandidateCertificationService} from "../../../../../services/candidate-certification.service";
import {CandidateCertification} from "../../../../../model/candidate-certification";
import {CountryService} from "../../../../../services/country.service";

@Component({
  selector: 'app-edit-candidate-certification',
  templateUrl: './edit-candidate-certification.component.html',
  styleUrls: ['./edit-candidate-certification.component.scss']
})
export class EditCandidateCertificationComponent implements OnInit {

  candidateCertification: CandidateCertification;

  candidateForm: FormGroup;

  countries = [];
  years = [];
  error;
  loading: boolean;
  saving: boolean;

  constructor(private activeModal: NgbActiveModal,
              private fb: FormBuilder,
              private candidateCertificationService: CandidateCertificationService,
              private countryService: CountryService ) {
  }

  ngOnInit() {
    this.loading = true;

    /*load the countries */
    this.countryService.listCountries().subscribe(
      (response) => {
        this.countries = response;
      },
      (error) => {
        this.error = error;
        this.loading = false;
      }
    );

    this.candidateForm = this.fb.group({
      name: [this.candidateCertification.name],
      institution: [this.candidateCertification.institution],
      dateCompleted: [this.candidateCertification.dateCompleted],
    });
    this.loading = false;
  }

  onSave() {
    this.saving = true;
    this.candidateCertificationService.update(this.candidateCertification.id, this.candidateForm.value).subscribe(
      (candidateCertification) => {
        this.closeModal(candidateCertification);
        this.saving = false;
      },
      (error) => {
        this.error = error;
        this.saving = false;
      });
  }

  closeModal(candidateCertification: CandidateCertification) {
    this.activeModal.close(candidateCertification);
  }

  dismiss() {
    this.activeModal.dismiss(false);
  }
}
