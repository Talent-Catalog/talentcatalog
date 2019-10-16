import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {CandidateJobExperienceService} from "../../../../../../services/candidate-job-experience.service";
import {CandidateJobExperience} from "../../../../../../model/candidate-job-experience";
import {CountryService} from "../../../../../../services/country.service";

@Component({
  selector: 'app-edit-candidate-job-experience',
  templateUrl: './edit-candidate-job-experience.component.html',
  styleUrls: ['./edit-candidate-job-experience.component.scss']
})
export class EditCandidateJobExperienceComponent implements OnInit {

  candidateJobExperience: CandidateJobExperience;

  candidateForm: FormGroup;

  countries = [];
  years = [];
  error;
  loading: boolean;
  saving: boolean;

  constructor(private activeModal: NgbActiveModal,
              private fb: FormBuilder,
              private candidateJobExperienceService: CandidateJobExperienceService,
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
      countryId: [this.candidateJobExperience.country ? this.candidateJobExperience.country.id : null, Validators.required],
      companyName: [this.candidateJobExperience.companyName],
      role: [this.candidateJobExperience.role],
      startDate: [this.candidateJobExperience.startDate],
      endDate: [this.candidateJobExperience.endDate],
      fullTime: [this.candidateJobExperience.fullTime],
      paid: [this.candidateJobExperience.paid],
      description: [this.candidateJobExperience.description],
    });
    this.loading = false;
  }

  onSave() {
    this.saving = true;
    this.candidateJobExperienceService.update(this.candidateJobExperience.id, this.candidateForm.value).subscribe(
      (candidateJobExperience) => {
        this.closeModal(candidateJobExperience);
        this.saving = false;
      },
      (error) => {
        this.error = error;
        this.saving = false;
      });
  }

  closeModal(candidateJobExperience: CandidateJobExperience) {
    this.activeModal.close(candidateJobExperience);
  }

  dismiss() {
    this.activeModal.dismiss(false);
  }
}
