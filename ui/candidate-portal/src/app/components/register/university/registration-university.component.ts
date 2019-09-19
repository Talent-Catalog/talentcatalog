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
  selector: 'app-registration-university',
  templateUrl: './registration-university.component.html',
  styleUrls: ['./registration-university.component.scss']
})
export class RegistrationUniversityComponent implements OnInit {

  form: FormGroup;
  error: any;
  loading: boolean;
  saving: boolean;
  countries: Country[];
  years: number[];
  educations: Education[];
  university: Education[];

  constructor(private fb: FormBuilder,
              private router: Router,
              private candidateService: CandidateService,
              private educationService: EducationService,
              private countryService: CountryService) { }

  ngOnInit() {
    this.countries = [];
    this.years = years;
    this.saving = false;
    this.loading = true;

    this.form = this.fb.group({
      educationType: ['University'],
      courseName: ['', Validators.required],
      country: ['', Validators.required],
      institution: ['', Validators.required],
      lengthOfCourseYears: ['', Validators.required],
      dateCompleted: ['', Validators.required]
     });

    /* Load & update the candidate data */
    this.candidateService.getCandidateEducations().subscribe(
      (candidate) => {
        this.educations = candidate.educations || [];

        /* filter for the correct education type for the component */
        this.university = this.educations.filter(e => e.educationType == "University");
        if(this.university.length !== 0){
          this.form.patchValue({
            educationType: this.university[0].educationType,
            courseName: this.university[0].courseName,
            country: this.university[0].country.id,
            institution: this.university[0].institution,
            lengthOfCourseYears: this.university[0].lengthOfCourseYears,
            dateCompleted: this.university[0].dateCompleted,
          });
        }

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
      },
      (error) => {
        this.error = error;
        this.loading = false;
      }
    );
  };

  save() {
    this.saving = true;
    /* CREATE if no education type exists in education table*/
    if(this.university.length == 0){
      this.educationService.createEducation(this.form.value).subscribe(
        (response) => {
           this.educations.push(response);
           this.saving = false;
           this.router.navigate(['register', 'education', 'school']);
        },
        (error) => {
           this.error = error;
           this.saving = false;
        },
      );
    } else {
      this.educationService.updateEducation(this.form.value).subscribe(
        (response) => {
           this.router.navigate(['register', 'education', 'school']);
        },
        (error) => {
           this.error = error;
           this.saving = false;
        }
      );
    }
  }
}
