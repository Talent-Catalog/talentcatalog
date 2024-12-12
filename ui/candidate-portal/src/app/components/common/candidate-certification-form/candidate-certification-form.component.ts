/*
 * Copyright (c) 2024 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {UntypedFormBuilder, UntypedFormGroup, Validators} from '@angular/forms';
import {Router} from '@angular/router';
import {CandidateService} from '../../../services/candidate.service';
import {CandidateCertificationService} from '../../../services/candidate-certification.service';
import {RegistrationService} from '../../../services/registration.service';
import {CandidateCertification} from '../../../model/candidate-certification';


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

  form: UntypedFormGroup;

  constructor(private fb: UntypedFormBuilder,
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
      this.candidateCertificationService.update(this.form.value).subscribe(
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
