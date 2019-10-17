import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {Router} from "@angular/router";
import {CandidateService} from "../../../services/candidate.service";
import {EducationLevelService} from "../../../services/education-level.service";
import {EducationLevel} from "../../../model/education-level";
import {RegistrationService} from "../../../services/registration.service";
import {CandidateEducation} from "../../../model/candidate-education";
import {CandidateEducationService} from "../../../services/candidate-education.service";
import {EducationMajorService} from "../../../services/education-major.service";
import {EducationMajor} from "../../../model/education-major";
import {Country} from "../../../model/country";
import {CountryService} from "../../../services/country.service";

@Component({
  selector: 'app-registration-education',
  templateUrl: './registration-education.component.html',
  styleUrls: ['./registration-education.component.scss']
})
export class RegistrationEducationComponent implements OnInit {

  error: any;
  saving: boolean;
  _loading = {
    levels: true,
    candidate: true,
    educationMajors: true,
    countries: true
  };

  form: FormGroup;
  majors: EducationMajor[];
  countries: Country[];
  educationLevels: EducationLevel[];
  candidateEducationItems: CandidateEducation[];
  addingEducation: boolean;

  constructor(private fb: FormBuilder,
              private router: Router,
              private candidateEducationService: CandidateEducationService,
              private candidateService: CandidateService,
              private countryService: CountryService,
              private educationLevelService: EducationLevelService,
              private educationMajorService: EducationMajorService,
              public registrationService: RegistrationService) {
  }

  ngOnInit() {
    this.saving = false;
    this.candidateEducationItems = [];
    this.form = this.fb.group({
      maxEducationLevelId: ['', Validators.required]
    });

    /* Load data */
    this.educationMajorService.listMajors().subscribe(
      (response) => {
        this.majors = response;
        this._loading.educationMajors = false;
      },
      (error) => {
        this.error = error;
        this._loading.educationMajors = false;
      });

    this.countryService.listCountries().subscribe(
      (response) => {
        this.countries = response;
        this._loading.countries = false;
      },
      (error) => {
        this.error = error;
        this._loading.countries = false;
      });

    this.educationLevelService.listEducationLevels().subscribe(
      (response) => {
        this.educationLevels = response;
        this._loading.levels = false;
      },
      (error) => {
        this.error = error;
        this._loading.levels = false;
      }
    );

    this.candidateService.getCandidateEducation().subscribe(
      (candidate) => {
        this.form.patchValue({
          maxEducationLevelId: candidate.maxEducationLevel ? candidate.maxEducationLevel.id : null,
        });
        if (candidate.candidateEducations) {
          this.candidateEducationItems = candidate.candidateEducations.map(edu => {
            return {
              id: edu ? edu.id : null,
            educationType: edu ? edu.educationType : null,
            lengthOfCourseYears: edu ? edu.lengthOfCourseYears : null,
            institution: edu ? edu.institution : null,
            courseName: edu ? edu.courseName : null,
            yearCompleted: edu ? edu.yearCompleted : null,
            countryId: edu && edu.country ? edu.country.id : null,
            educationMajorId: edu && edu.educationMajor ? edu.educationMajor.id : null,
            }
          });
        }
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
    this.candidateService.updateCandidateEducation(this.form.value).subscribe(
      (response) => {
        this.saving = false;
        if (dir === 'next') {
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
  };

  back() {
    this.save('back');
  }

  next() {
    this.save('next');
  }

  get loading() {
    const l = this._loading;
    return l.levels || l.candidate || l.educationMajors || l.countries;
  }

  addEducation() {
    if (this.addingEducation) {
      this.addingEducation = false;
    } else {
      this.addingEducation = true;
    }
  }

  handleCandidateEducationSaved(education: CandidateEducation) {
    let index = -1;
    if (this.candidateEducationItems.length) {
      index = this.candidateEducationItems.findIndex(edu => edu.id === education.id);
    }
    /* Replace the old education item with the updated item */
    if (index >= 0) {
      this.candidateEducationItems.splice(index, 1, education);
    } else {
      this.candidateEducationItems.push(education);
    }
  }
}
