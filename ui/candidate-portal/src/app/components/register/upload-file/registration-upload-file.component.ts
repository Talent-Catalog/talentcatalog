import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {FormGroup} from "@angular/forms";
import {RegistrationService} from "../../../services/registration.service";

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


  constructor(public registrationService: RegistrationService) {
  }

  ngOnInit() {
  }



  cancel() {
    this.onSave.emit();
  }

  next() {
    this.onSave.emit();
    this.registrationService.next();
  }



  back() {
    // Candidate data shouldn't be updated
    this.registrationService.back();
  }


}
