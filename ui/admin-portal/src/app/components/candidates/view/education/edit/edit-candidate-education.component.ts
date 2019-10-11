import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {CandidateEducationService} from "../../../../../services/candidate-education.service";
import {CandidateEducation} from "../../../../../model/candidate-education";
import {CountryService} from "../../../../../services/country.service";

@Component({
  selector: 'app-edit-candidate-education',
  templateUrl: './edit-candidate-education.component.html',
  styleUrls: ['./edit-candidate-education.component.scss']
})
export class EditCandidateEducationComponent implements OnInit {

  candidateEducation: CandidateEducation;

  candidateForm: FormGroup;

  countries = [];
  years = [];
  error;
  loading: boolean;
  saving: boolean;

  constructor(private activeModal: NgbActiveModal,
              private fb: FormBuilder,
              private candidateEducationService: CandidateEducationService,
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
      courseName: [this.candidateEducation.courseName],
      institution: [this.candidateEducation.institution],
      countryId: [this.candidateEducation.country ? this.candidateEducation.country.id : null, Validators.required],
      dateCompleted: [this.candidateEducation.dateCompleted],
    });
    this.loading = false;
  }

  onSave() {
    this.saving = true;
    this.candidateEducationService.update(this.candidateEducation.id, this.candidateForm.value).subscribe(
      (candidateEducation) => {
        this.closeModal(candidateEducation);
        this.saving = false;
      },
      (error) => {
        this.error = error;
        this.saving = false;
      });
  }

  closeModal(candidateEducation: CandidateEducation) {
    this.activeModal.close(candidateEducation);
  }

  dismiss() {
    this.activeModal.dismiss(false);
  }
}
