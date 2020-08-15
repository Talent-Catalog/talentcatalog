import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {CandidateJobExperienceService} from "../../../../../../services/candidate-job-experience.service";
import {CandidateJobExperience} from "../../../../../../model/candidate-job-experience";
import {CountryService} from "../../../../../../services/country.service";
import {CandidateOccupation} from "../../../../../../model/candidate-occupation";

@Component({
  selector: 'app-create-candidate-job-experience',
  templateUrl: './create-candidate-job-experience.component.html',
  styleUrls: ['./create-candidate-job-experience.component.scss']
})
export class CreateCandidateJobExperienceComponent implements OnInit {

  candidateJobExperience: CandidateJobExperience;
  candidateOccupation: CandidateOccupation;

  candidateForm: FormGroup;

  candidateOccupationId: number;
  candidateId: number;
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
      countryId: ['', [Validators.required]],
      companyName: ['', [Validators.required]],
      candidateOccupationId: [this.candidateOccupationId],
      role: ['', [Validators.required]],
      startDate: ['', [Validators.required]],
      endDate: [''],
      fullTime: ['', [Validators.required]],
      paid: ['', [Validators.required]],
      description: ['', [Validators.required]],
    });
    this.loading = false;
  }

  onSave() {
    this.saving = true;
    this.candidateJobExperienceService.create(this.candidateId, this.candidateForm.value).subscribe(
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
