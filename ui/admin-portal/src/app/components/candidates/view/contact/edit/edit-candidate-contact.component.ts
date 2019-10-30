import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {CandidateService} from "../../../../../services/candidate.service";
import {Candidate} from "../../../../../model/candidate";
import {NationalityService} from "../../../../../services/nationality.service";
import {CountryService} from "../../../../../services/country.service";
import {generateYearArray} from "../../../../../util/year-helper";

@Component({
  selector: 'app-edit-country',
  templateUrl: './edit-candidate-contact.component.html',
  styleUrls: ['./edit-candidate-contact.component.scss']
})
export class EditCandidateContactComponent implements OnInit {

  candidateId: number;


  candidateForm: FormGroup;

  nationalities = [];
  countries = [];
  years = [];
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

    this.years = generateYearArray(1950, true);

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

    /*load the nationalities */
    this.nationalityService.listNationalities().subscribe(
      (response) => {
        this.nationalities = response;
      },
      (error) => {
        this.error = error;
        this.loading = false;
      }
    );

    this.candidateService.get(this.candidateId).subscribe(candidate => {
      this.candidateForm = this.fb.group({
        firstName: [candidate.user.firstName],
        lastName: [candidate.user.lastName],
        gender: [candidate.gender],
        address1: [candidate.address1],
        city: [candidate.city],
        countryId: [candidate.country ? candidate.country.id : null, Validators.required],
        yearOfArrival: [candidate.yearOfArrival],
        phone: [candidate.phone],
        whatsapp: [candidate.whatsapp],
        email: [candidate.user.email],
        dob: [candidate.dob],
        nationalityId: [candidate.nationality ? candidate.nationality.id : null, Validators.required],
      });
      this.loading = false;
    });
  }

  onSave() {
    this.saving = true;
    this.candidateService.update(this.candidateId, this.candidateForm.value).subscribe(
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
