import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {FormBuilder, FormGroup} from "@angular/forms";
import {Router} from "@angular/router";
import {CandidateService} from "../../../services/candidate.service";
import {RegistrationService} from "../../../services/registration.service";


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
  // Component states
  loading: boolean;
  saving: boolean;

  constructor(private fb: FormBuilder,
              private router: Router,
              private candidateService: CandidateService,
              public registrationService: RegistrationService) {
  }

  ngOnInit() {
    this.loading = true;
    this.saving = false;
    this.form = this.fb.group({
      additionalInfo: [''],
      submit: this.submitApplication
    });
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

  save(dir: string) {
    this.saving = true;

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
}
