import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {Router} from "@angular/router";
import {CandidateSurveyService} from "../../../services/candidate-survey.service";
import {RegistrationService} from "../../../services/registration.service";
import {SurveyTypeService} from "../../../services/survey-type.service";
import {SurveyType} from "../../../model/survey-type";

@Component({
  selector: 'app-registration-survey',
  templateUrl: './registration-survey.component.html',
  styleUrls: ['./registration-survey.component.scss']
})
export class RegistrationSurveyComponent implements OnInit {


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
              private candidateSurveyService: CandidateSurveyService,
              public registrationService: RegistrationService,
              private surveyTypeService: SurveyTypeService) { }

  ngOnInit() {
    this.loading = true;
    this.saving = false;
    this.form = this.fb.group({
      surveyTypeId: ['', Validators.required],
      surveyComment: [''],
    });

    this.loadDropDownData();

    // this.surveyType.getCandidateAdditionalInfo().subscribe(
    //   (response) => {
    //     this.form.patchValue({
    //       additionalInfo: response.additionalInfo
    //     });
    //     this.loading = false;
    //   },
    //   (error) => {
    //     this.error = error;
    //     this.loading = false;
    //   }
    // );
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

  save(){
    console.log("changed");
    this.saving = true;
    this.candidateSurveyService.createCandidateSurvey(this.form.value).subscribe(
      (response) => {
        console.log(response);
        this.saving = false;
      },
      (error) => {
        this.error = error;
        this.saving = false;
      });
  }

}

