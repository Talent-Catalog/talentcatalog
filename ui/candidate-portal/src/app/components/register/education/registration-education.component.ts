import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {Router} from "@angular/router";
import {CandidateService} from "../../../services/candidate.service";
import {EducationLevelService} from "../../../services/education-level.service";
import {EducationLevel} from "../../../model/education-level";
import {RegistrationService} from "../../../services/registration.service";
import {CandidateEducation} from "../../../model/candidate-education";

@Component({
  selector: 'app-registration-education',
  templateUrl: './registration-education.component.html',
  styleUrls: ['./registration-education.component.scss']
})
export class RegistrationEducationComponent implements OnInit {

  error: any;
  // Component states
  _loading = {
    levels: true,
    candidate: true
  };
  saving: boolean;

  educationLevelForm: FormGroup;
  educationLevels: EducationLevel[];
  candidateEducationItems: CandidateEducation[];


  constructor(private fb: FormBuilder,
              private router: Router,
              private educationLevelService: EducationLevelService,
              private candidateService: CandidateService,
              public registrationService: RegistrationService) {
  }

  ngOnInit() {
    this.saving = false;
    this.educationLevelForm = this.fb.group({
      maxEducationLevelId: ['', Validators.required]
    });

    this._loading.levels = true;
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

    this._loading.candidate = true;
    this.candidateService.getCandidateEducation().subscribe(
      (response) => {
        /* DEBUG */
        console.log('response', response);
        this.educationLevelForm.patchValue({
          maxEducationLevelId: response.maxEducationLevel ? response.maxEducationLevel.id : null,
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
    this.candidateService.updateCandidateEducationLevel(this.educationLevelForm.value).subscribe(
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
    return this._loading.levels || this._loading.candidate;
  }
}
