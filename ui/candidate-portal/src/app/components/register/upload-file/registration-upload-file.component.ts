import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {FormBuilder, FormGroup} from "@angular/forms";
import {SurveyType} from "../../../model/survey-type";
import {Router} from "@angular/router";
import {CandidateService} from "../../../services/candidate.service";
import {RegistrationService} from "../../../services/registration.service";
import {SurveyTypeService} from "../../../services/survey-type.service";

@Component({
  selector: 'app-registration-upload-file',
  templateUrl: './registration-upload-file.component.html',
  styleUrls: ['./registration-upload-file.component.scss']
})
export class RegistrationUploadFileComponent implements OnInit {

  @Input() submitApplication: boolean = false;
  /* A flag to indicate if the component is being used on the profile component */
  @Input() edit: boolean = false;

  @Output() onSave = new EventEmitter();

  form: FormGroup;
  error: any;
  // Component states
  saving: boolean;

  surveyTypes: SurveyType[];

  constructor(private fb: FormBuilder,
              private router: Router,
              private candidateService: CandidateService,
              public registrationService: RegistrationService,
              private surveyTypeService: SurveyTypeService) {
  }

  ngOnInit() {
  }



  cancel() {
    this.onSave.emit();
  }

  next() {
    this.save('next');
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

    this.candidateService.updateCandidateEducationLevel(this.form.value).subscribe(
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
    // Candidate data shouldn't be updated
    this.registrationService.back();
  }


}
