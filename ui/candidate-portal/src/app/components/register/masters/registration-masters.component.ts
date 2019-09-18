import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {Router} from "@angular/router";
import {years} from "../../../model/years";
import {Education} from "../../../model/education";
import {CandidateService} from "../../../services/candidate.service";
import {EducationService} from "../../../services/education.service";
import {Country} from "../../../model/country";
import {CountryService} from "../../../services/country.service";


@Component({
  selector: 'app-registration-masters',
  templateUrl: './registration-masters.component.html',
  styleUrls: ['./registration-masters.component.scss']
})
export class RegistrationMastersComponent implements OnInit {

  form: FormGroup;
  error: any;
  loading: boolean;
  saving: boolean;
  countries: Country[];
  years: number[];
  educations: Education[];

  constructor(private fb: FormBuilder,
              private router: Router,
              private candidateService: CandidateService,
              private educationService: EducationService,
              private countryService: CountryService) { }

  ngOnInit() {
    this.educations = [];
    this.countries = [];
    this.years = years;
    this.saving = false;
    this.loading = true;

    this.form = this.fb.group({
      educationType: ['Masters'],
      courseName: ['', Validators.required],
      country: ['', Validators.required],
      institution: ['', Validators.required],
      lengthOfCourseYears: ['', Validators.required],
      dateCompleted: ['', Validators.required]
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
  };

  save() {
    this.saving = true;
    console.log(this.form.value);
    this.educationService.createEducation(this.form.value).subscribe(
      (response) => {
         console.log(response);
         this.educations.push(response);
         this.saving = false;
         this.router.navigate(['register', 'education', 'university']);
      },
      (error) => {
         this.error = error;
         this.saving = false;
      }
    );
  }

}
