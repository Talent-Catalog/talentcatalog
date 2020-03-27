import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {Router} from "@angular/router";
import {CandidateService} from "../../../services/candidate.service";
import {RegistrationService} from "../../../services/registration.service";
import {SurveyTypeService} from "../../../services/survey-type.service";
import {SurveyType} from "../../../model/survey-type";

@Component({
  selector: 'app-registration-additional-info',
  templateUrl: './registration-additional-info.component.html',
  styleUrls: ['./registration-additional-info.component.scss']
})
export class RegistrationAdditionalInfoComponent implements OnInit {

  @Input() submitApplication: boolean = false;
  /* A flag to indicate if the component is being used on the profile component */
  @Input() edit: boolean = false;

  @Output() onSave = new EventEmitter();

  form: FormGroup;
  error: any;
  _loading = {
    surveyTypes: true,
  };
  // Component states
  loading: boolean;
  saving: boolean;

  surveyTypes: SurveyType[];

  constructor(private fb: FormBuilder,
              private router: Router,
              private candidateService: CandidateService,
              public registrationService: RegistrationService,
              private surveyTypeService: SurveyTypeService) {
  }

  ngOnInit() {
    this.loading = true;
    this.saving = false;
    this.form = this.fb.group({
      additionalInfo: [''],
      surveyTypeId: ['', Validators.required],
      submit: this.submitApplication
    });

    this.loadDropDownData();

    this.candidateService.getCandidateAdditionalInfo().subscribe(
      (response) => {
        this.form.patchValue({
          additionalInfo: response.additionalInfo
        });
        this.loading = false;
      },
      (error) => {
        this.error = error;
        this.loading = false;
      }
    );
  }

  loadDropDownData() {
    this._loading.surveyTypes = true;

    /* Load the language levels */
    this.surveyTypeService.listSurveyTypes().subscribe(
      (response) => {
        this.surveyTypes = response;
        this._loading.surveyTypes = false;
      },
      (error) => {
        this.error = error;
        this._loading.surveyTypes = false;
      }
    );
  }

  save(dir: string) {
    this.saving = true;

    /* Don't submit the registration application if the user is going back */
    if (dir === 'back') {
      this.form.controls.submit.patchValue(false);
    }

    this.candidateService.updateCandidateAdditionalInfo(this.form.value).subscribe(
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
  }

  cancel() {
    this.onSave.emit();
  }
}
