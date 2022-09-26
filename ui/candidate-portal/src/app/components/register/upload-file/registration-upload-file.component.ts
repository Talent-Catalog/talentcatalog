import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {UntypedFormGroup} from "@angular/forms";
import {RegistrationService} from "../../../services/registration.service";
import {CandidateService} from "../../../services/candidate.service";

@Component({
  selector: 'app-registration-upload-file',
  templateUrl: './registration-upload-file.component.html',
  styleUrls: ['./registration-upload-file.component.scss']
})
export class RegistrationUploadFileComponent implements OnInit {

  /* A flag to indicate if the component is being used on the profile component */
  @Input() edit: boolean = false;

  @Output() onSave = new EventEmitter();

  form: UntypedFormGroup;
  error: any;
  // Component states
  saving: boolean;
  activeIds: string;


  constructor(public registrationService: RegistrationService,
              private candidateService: CandidateService) {
  }

  ngOnInit() {
    // Make the accordions closed if editing (to allow better view of footer navigation)
    if (!this.edit) {
      this.activeIds = 'upload-cv'
    } else {
      this.activeIds = ''
    }
  }

  //Final registration step method
  submit() {
    this.saving = true;
    this.candidateService.submitRegistration().subscribe(
      (response) => {
        this.saving = false;
        this.next();
      },
      (error) => {
        this.error = error;
        this.saving = false;
      }
    );
  }

  // Methods during registration process.
  next() {
    this.registrationService.next();
  }
  back() {
    this.registrationService.back();
  }

  // Methods during edit process.
  update() {
    this.onSave.emit();
  }
  cancel() {
    this.onSave.emit();
  }


}
