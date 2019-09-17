import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {Router} from "@angular/router";
import {countries} from "../../../model/countries";
import {years} from "../../../model/years";
import {Education} from "../../../model/education";
import {CandidateService} from "../../../services/candidate.service";
import {EducationService} from "../../../services/education.service";

@Component({
  selector: 'app-registration-school',
  templateUrl: './registration-school.component.html',
  styleUrls: ['./registration-school.component.scss']
})
export class RegistrationSchoolComponent implements OnInit {

  form: FormGroup;
  // TODO create list of years
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
    this.saving = false;
    this.loading = true;
    this.countries = countries;
    this.years = years;
    this.form = this.fb.group({
      educationTypeId: ['School'],
      courseName: [''],
      countryId: ['', Validators.required],
      institution: ['', Validators.required],
      lengthOfCourseYears: [''],
      dateCompleted: [''],
      completedSchool: ['', Validators.required]
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
  }

  save() {
  this.saving = true;
    this.educationService.createEducation(this.form.value).subscribe(
      (response) => {
         this.saving = false;
         this.router.navigate(['register', 'language']);
      },
      (error) => {
         this.error = error;
         this.saving = false;
      }
    );
  }

}
