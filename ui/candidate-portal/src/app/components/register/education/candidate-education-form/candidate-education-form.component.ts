import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {Router} from "@angular/router";
import {years} from "../../../../model/years";
import {CandidateEducation} from "../../../../model/candidate-education";
import {CandidateEducationService} from "../../../../services/candidate-education.service";
import {Country} from "../../../../model/country";
import {CountryService} from "../../../../services/country.service";
import {EducationMajor} from "../../../../model/education-major";
import {EducationMajorService} from "../../../../services/education-major.service";


@Component({
  selector: 'app-candidate-education-form',
  templateUrl: './candidate-education-form.component.html',
  styleUrls: ['./candidate-education-form.component.scss']
})
export class CandidateEducationFormComponent implements OnInit {

  @Input() candidateEducation: CandidateEducation;
  @Input() majors: EducationMajor[];
  @Input() countries: Country[];

  @Output() saved = new EventEmitter<CandidateEducation>();

  error: any;
  _loading = {
    countries: false,
    majors: false
  };
  saving: boolean;

  form: FormGroup;
  years: number[];

  constructor(private fb: FormBuilder,
              private router: Router,
              private candidateEducationService: CandidateEducationService,
              private countryService: CountryService,
              private majorService: EducationMajorService) {
  }

  ngOnInit() {
    this.saving = false;
    this.years = years;
    /* Intialise the form */
    const edu = this.candidateEducation;
    this.form = this.fb.group({
      id: [edu ? edu.id : null],
      educationType: [edu ? edu.educationType : null],
      courseName: [edu ? edu.courseName : null, Validators.required],
      countryId: [edu && edu.country ? edu.country.id : null, Validators.required],
      institution: [edu ? edu.institution : null, Validators.required],
      lengthOfCourseYears: [edu ? edu.lengthOfCourseYears : null, Validators.required],
      dateCompleted: [edu ? edu.yearCompleted : null, Validators.required],
      educationMajorId: [edu && edu.educationMajor ? edu.educationMajor.id : null, Validators.required]
    });

    /* Load countries if absent */
    if (!this.countries) {
      this._loading.countries = true;
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
    } else {
      this._loading.countries = false;
    }

    /* Load majors if absent */
    if (!this.majors) {
      this._loading.majors = true;
      /* Load the majors */
      this.majorService.listMajors().subscribe(
        (response) => {
          this.majors = response;
          this._loading.majors = false;
        },
        (error) => {
          this.error = error;
          this._loading.majors = false;
        }
      );
    } else {
      this._loading.majors = false;
    }
  };

  get loading() {
    return this._loading.majors || this._loading.countries;
  }

  get requiresEducationMajor() {
    if (!this.form.value.educationType) {return false;}
    return ['masters', 'university'].includes(this.form.value.educationType.toLowerCase());
  }

  save() {
    this.error = null;
    this.saving = true;


    if (!this.form.value.id) {
      this.candidateEducationService.createCandidateEducation(this.form.value).subscribe(
        (response) => {
          this.saved.emit(response);
        },
        (error) => {
          this.error = error;
          this.saving = false;
        },
      );
    } else {
      this.candidateEducationService.updateCandidateEducation(this.form.value).subscribe(
        (response) => {
          this.router.navigate(['register', 'education', 'university']);
        },
        (error) => {
          this.error = error;
          this.saving = false;
        }
      );
    }
  }
}
