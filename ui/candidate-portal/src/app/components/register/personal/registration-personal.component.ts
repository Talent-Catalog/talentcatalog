import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
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

  /* A flag to indicate if the component is being used on the profile component */
  @Input() edit: boolean = false;

  @Output() onSave = new EventEmitter();

  form: FormGroup;
  error: any;
  // Component states
  _loading = {
    candidate: true,
    countries: true,
    nationalities: true
  };
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
        this._loading.countries = false;
      },
      (error) => {
        this.error = error;
        this._loading.countries = false;
      }
    );

    this.nationalityService.listNationalities().subscribe(
      (response) => {
        this.nationalities = response;
        this._loading.nationalities = false;
      },
      (error) => {
        this.error = error;
        this._loading.nationalities = false;
      }
    );

    this.candidateService.getCandidatePersonal().subscribe(
      (response) => {
        this.form.patchValue({
          /* PERSONAL */
          firstName: response.user ? response.user.firstName : null,
          lastName: response.user ? response.user.lastName : null,
          gender: response.gender || null ,
          dob: response.dob || null,
          /* LOCATION */
          countryId: response.country ? response.country.id : null,
          city: response.city,
          yearOfArrival: response.yearOfArrival,
          /* NATIONALITY */
          nationality: response.nationality ? response.nationality.id : null,
          // registeredWithUN: response.registeredWithUN,
          // registrationId: response.registrationId

        });
        this._loading.candidate = false;
      },
      (error) => {
        this.error = error;
        this._loading.candidate = false;
      }
    );
  }

  save(dir: string) {
    this.saving = true;
    if (this.form.invalid) {
      return;
    }

    // If the candidate hasn't changed anything, skip the update service call
    if (this.form.pristine) {
      if (dir === 'next') {
        this.onSave.emit();
        this.registrationService.next();
      } else {
        this.registrationService.back();
      }
      return;
    }

    // Save changes
    this.candidateService.updateCandidatePersonal(this.form.value).subscribe(
      () => {
        this.saving = false;
        if (dir === 'next') {
          this.onSave.emit();
          this.registrationService.next();
        } else {
          this.registrationService.back();
        }
      },
      (error) => {
        this.error = error;
        this.saving = false;
      }
    );
  }

  back() {
    if (this.form.invalid || this.form.pristine) {
      // Candidate data shouldn't be updated
      this.registrationService.back();
    } else {
      this.save('back');
    }
  }

  next() {
    this.save('next');
  }

  get loading() {
    const l = this._loading;
    return l.candidate || l.countries || l.nationalities;
  }

}
