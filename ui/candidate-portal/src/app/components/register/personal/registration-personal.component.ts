import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {Router} from "@angular/router";
import {Candidate} from "../../../model/candidate";
import {CandidateService} from "../../../services/candidate.service";
import {NationalityService} from "../../../services/nationality.service";
import {Nationality} from "../../../model/nationality";
import {CountryService} from "../../../services/country.service";
import {Country} from "../../../model/country";
import {years} from "../../../model/years";
import {RegistrationService} from "../../../services/registration.service";

@Component({
  selector: 'app-registration-personal',
  templateUrl: './registration-personal.component.html',
  styleUrls: ['./registration-personal.component.scss']
})
export class RegistrationPersonalComponent implements OnInit {

  form: FormGroup;
  error: any;
  // Component states
  loading: boolean;
  saving: boolean;
  candidate: Candidate;
  countries: Country[];
  nationalities: Nationality[];
  years: number[];

  constructor(private fb: FormBuilder,
              private router: Router,
              private candidateService: CandidateService,
              private countryService: CountryService,
              private nationalityService: NationalityService,
              public registrationService: RegistrationService) { }

  ngOnInit() {
    this.loading = true;
    this.saving = false;
    this.years = years;
    this.form = this.fb.group({
      /* PERSONAL */
      firstName: [null, Validators.required],
      lastName: [null, Validators.required],
      gender: [null, Validators.required],
      dob: [null, Validators.required],
      /* LOCATION */
      countryId: ['', Validators.required],
      city: ['', Validators.required],
      yearOfArrival: ['', Validators.required],
      /* NATIONALITY */
      nationality: ['', Validators.required],
      // registeredWithUN: ['', Validators.required],
      // registrationId: ['', Validators.required]
    });

    /* Load the countries */
    this.countryService.listCountries().subscribe(
      (response) => {
        this.countries = response;
        this.loading = false;
      },
      (error) => {
        this.error = error;
        this.loading = false;
      }
    );

    this.nationalityService.listNationalities().subscribe(
      (response) => {
        this.nationalities = response;
        this.loading = false;
      },
      (error) => {
        this.error = error;
        this.loading = false;
      }
    );

    this.candidateService.getCandidatePersonal().subscribe(
      (response) => {
        this.form.patchValue({
          /* PERSONAL */
          firstName: response.user.firstName,
          lastName: response.user.lastName,
          gender: response.gender,
          dob: response.dob,
          /* LOCATION */
          countryId: response.country.id,
          city: response.city,
          yearOfArrival: response.yearOfArrival,
          /* NATIONALITY */
          nationality: response.nationality.id,
          // registeredWithUN: response.registeredWithUN,
          // registrationId: response.registrationId

        });
        this.loading = false;
      },
      (error) => {
        this.error = error;
        this.loading = false;
      }
    );
  }

  save() {
    this.candidateService.updateCandidatePersonal(this.form.value).subscribe(
      (response) => {
        this.router.navigate(['register', 'location']);
      },
      (error) => {
        this.error = error;
      }
    );
  }

}
