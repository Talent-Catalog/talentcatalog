import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {Router} from '@angular/router';
import {CandidateService} from '../../../services/candidate.service';
import {CandidateCertificationService} from '../../../services/candidate-certification.service';
import {RegistrationService} from '../../../services/registration.service';
import {CandidateCertification} from '../../../model/candidate-certification';
import {generateYearArray} from '../../../util/year-helper';


@Component({
  selector: 'app-candidate-certification-form',
  templateUrl: './candidate-certification-form.component.html',
  styleUrls: ['./candidate-certification-form.component.scss']
})
export class CandidateCertificationFormComponent implements OnInit {

  @Input() certificate: CandidateCertification;

  @Output() saved = new EventEmitter<CandidateCertification>();

  error: any;
  saving: boolean;

  form: FormGroup;

  constructor(private fb: FormBuilder,
              private router: Router,
              private candidateService: CandidateService,
              private candidateCertificationService: CandidateCertificationService,
              public registrationService: RegistrationService) { }

  ngOnInit() {
    this.saving = false;
    /* Intialise the form */
    const cert = this.certificate;
    this.form = this.fb.group({
      id: [cert ? cert.id : null],
      name: [cert ? cert.name : null , Validators.required],
      institution: [cert ? cert.institution : null , Validators.required],
      dateCompleted: [cert ? cert.dateCompleted : null , Validators.required]
    });
  };

  save() {
    this.error = null;
    this.saving = true;

    // If the candidate hasn't changed anything, skip the update service call
    if (this.form.pristine) {
      this.saved.emit(this.certificate);
      return;
    }

    if (!this.form.value.id) {
      this.candidateCertificationService.createCandidateCertification(this.form.value).subscribe(
        (response) => {
          this.saved.emit(response);
        },
        (error) => {
          this.error = error;
          this.saving = false;
        },
      );
    } else {
      this.candidateCertificationService.update(this.certificate.id, this.form.value).subscribe(
        (response) => {
          this.saved.emit(response);
        },
        (error) => {
          this.error = error;
          this.saving = false;
        }
      );
    }
  }

}
