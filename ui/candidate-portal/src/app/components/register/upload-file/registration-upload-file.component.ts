import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {FormGroup} from "@angular/forms";
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

  form: FormGroup;
  error: any;
  // Component states
  saving: boolean;


  constructor(public registrationService: RegistrationService,
              private candidateService: CandidateService) {
  }

  ngOnInit() {
  }

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
