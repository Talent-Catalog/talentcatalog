import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {Router} from "@angular/router";
import {countries} from "../../../model/countries";
import {years} from "../../../model/years";
import {Education} from "../../../model/education";
import {CandidateService} from "../../../services/candidate.service";
import {EducationService} from "../../../services/education.service";

@Component({
  selector: 'app-registration-university',
  templateUrl: './registration-university.component.html',
  styleUrls: ['./registration-university.component.scss']
})
export class RegistrationUniversityComponent implements OnInit {

  form: FormGroup;
  countries: string[];
  years: number[];
  error: any;
  loading: boolean;
  saving: boolean;
  educations: Education[];

  constructor(private fb: FormBuilder,
              private router: Router,
              private candidateService: CandidateService,
              private educationService: EducationService) { }

  ngOnInit() {
    this.educations = [];
    this.countries = countries;
    this.years = years;
    this.saving = false;
    this.loading = true;
    this.form = this.fb.group({
      educationType: ['University'],
      courseName: ['', Validators.required],
      countryId: ['', Validators.required],
      institution: ['', Validators.required],
      lengthOfCourseYears: ['', Validators.required],
      dateCompleted: ['', Validators.required]
     });

    this.candidateService.getCandidateEducations().subscribe(
      (response) => {
        /* TO DO remember the filled out form */
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
    this.educationService.createEducation(this.form.value).subscribe(
      (response) => {
         this.saving = false;
         this.router.navigate(['register', 'education', 'school']);
      },
      (error) => {
         this.error = error;
         this.saving = false;
      }
    );
  }

}
