import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {Router} from "@angular/router";
import {CandidateEducation} from "../../../model/candidate-education";
import {CandidateEducationService} from "../../../services/candidate-education.service";
import {Country} from "../../../model/country";
import {CountryService} from "../../../services/country.service";
import {EducationMajor} from "../../../model/education-major";
import {EducationMajorService} from "../../../services/education-major.service";
import {generateYearArray} from "../../../util/year-helper";


@Component({
  selector: 'app-candidate-education-form',
  templateUrl: './candidate-education-form.component.html',
  styleUrls: ['./candidate-education-form.component.scss']
})
export class CandidateEducationFormComponent implements OnInit {

  @Input() educationType: string;
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
    this.years = generateYearArray();
    /* Intialise the form */
    const edu = this.candidateEducation;
    this.form = this.fb.group({
      id: [edu ? edu.id : null],
      educationType: [edu ? edu.educationType : this.educationType, Validators.required],
      courseName: [edu ? edu.courseName : null, Validators.required],
      countryId: [edu && edu.country ? edu.country.id : null, Validators.required],
      institution: [edu ? edu.institution : null, Validators.required],
      lengthOfCourseYears: [edu ? edu.lengthOfCourseYears : null, Validators.required],
      dateCompleted: [edu ? edu.yearCompleted : null],
      incomplete: [edu ? edu.incomplete : null],
      educationMajorId: [edu && edu.educationMajor ? edu.educationMajor.id : null, Validators.required]
    });
    /* Observe form educationType control and add required validator for university (and above) education types */
    this.form.controls['educationType'].valueChanges.subscribe(
      (value) => {
          this.form.controls['educationMajorId'].setValidators(Validators.required);
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

  save() {
    this.error = null;
    this.saving = true;

    // If the candidate hasn't changed anything, skip the update service call
    if (this.form.pristine) {
      this.saved.emit(this.candidateEducation);
      return;
    }

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
          this.saved.emit(response);
        },
        (error) => {
          this.error = error;
          this.saving = false;
        }
      );
    }
  }
}
