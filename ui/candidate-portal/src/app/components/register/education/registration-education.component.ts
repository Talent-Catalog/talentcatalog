import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {FormBuilder, FormGroup} from "@angular/forms";
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

  /* A flag to indicate if the component is being used on the profile component */
  @Input() edit: boolean = false;

  @Output() onSave = new EventEmitter();

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
      maxEducationLevelId: ['']
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
          this.candidateEducationItems = candidate.candidateEducations
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

    this.candidateService.updateCandidateEducation(this.form.value).subscribe(
      (response) => {
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
    this.addingEducation = true;
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
    this.addingEducation = false;
  }

  deleteCandidateEducation(index: number) {
    this.saving = true;
    const education = this.candidateEducationItems[index];
    this.candidateEducationService.deleteCandidateEducation(education.id).subscribe(
      () => {
        this.candidateEducationItems.splice(index, 1);
        this.saving = false;
      },
      (error) => {
        this.error = error;
        this.saving = false;
      });
  }
}
