import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {Router} from "@angular/router";
import {years} from "../../../model/years";
import {CandidateService} from "../../../services/candidate.service";
import {Country} from "../../../model/country";
import {CountryService} from "../../../services/country.service";

@Component({
  selector: 'app-registration-location',
  templateUrl: './registration-location.component.html',
  styleUrls: ['./registration-location.component.scss']
})
export class RegistrationLocationComponent implements OnInit {

  form: FormGroup;
  years: number[];
  error: any;
  countries: Country[];
  // Component states
  loading: boolean;
  saving: boolean;

  constructor(private fb: FormBuilder,
              private router: Router,
              private candidateService: CandidateService,
              private countryService: CountryService) { }

  ngOnInit() {
    this.loading = true;
    this.saving = false;
    this.countries = [];
    this.years = years;
    this.form = this.fb.group({
      country: ['', Validators.required],
      city: ['', Validators.required],
      yearOfArrival: ['', Validators.required]
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

    this.candidateService.getCandidateLocation().subscribe(
      (response) => {
        this.form.patchValue({
          country: response.country,
          city: response.city,
          yearOfArrival: response.yearOfArrival
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
    this.candidateService.updateCandidateLocation(this.form.value).subscribe(
      (response) => {
        this.router.navigate(['register', 'nationality']);
      },
      (error) => {
        this.error = error;
      }
    );
  }

}
