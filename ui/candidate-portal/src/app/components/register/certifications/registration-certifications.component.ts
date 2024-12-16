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

import {Component, EventEmitter, Input, OnDestroy, OnInit, Output} from '@angular/core';
import {UntypedFormBuilder, UntypedFormGroup, Validators} from "@angular/forms";
import {Router} from "@angular/router";
import {CandidateCertification} from "../../../model/candidate-certification";
import {CandidateService} from "../../../services/candidate.service";
import {CandidateCertificationService} from "../../../services/candidate-certification.service";
import {RegistrationService} from "../../../services/registration.service";
import {CandidateEducation} from '../../../model/candidate-education';

@Component({
  selector: 'app-registration-certifications',
  templateUrl: './registration-certifications.component.html',
  styleUrls: ['./registration-certifications.component.scss']
})
export class RegistrationCertificationsComponent implements OnInit {

  /* A flag to indicate if the component is being used on the profile component */
  @Input() edit: boolean = false;

  @Output() onSave = new EventEmitter();

  error: any;
  loading: boolean;
  saving: boolean;

  form: UntypedFormGroup;
  candidateCertifications: CandidateCertification[];
  addingCertification: boolean;


  editTarget: CandidateCertification;
  subscription;

  constructor(private fb: UntypedFormBuilder,
              private router: Router,
              private candidateService: CandidateService,
              private candidateCertificationService: CandidateCertificationService,
              public registrationService: RegistrationService) { }

  ngOnInit() {
    this.candidateCertifications = [];
    this.saving = false;
    this.loading = false;
    this.clearForm();

   /* Load the candidate data */
    this.candidateService.getCandidateCertifications().subscribe(
      (candidate) => {
        this.candidateCertifications = candidate.candidateCertifications || [];
        this.addingCertification = !this.candidateCertifications.length;
      },
      (error) => {
        this.error = error;
        this.loading = false;
      }
    );
  }

  clearForm() {
    this.form = this.fb.group({
      name: ['', Validators.required],
      institution: ['', Validators.required],
      dateCompleted: ['', Validators.required]
    })
  }

  deleteCertificate(certification) {
    this.saving = true;
    this.candidateCertificationService.deleteCandidateCertification(certification.id).subscribe(
      () => {
        this.candidateCertifications = this.candidateCertifications.filter(c => c !== certification);
        this.addingCertification = !this.candidateCertifications.length;
        this.saving = false;
      },
      (error) => {
        this.error = error;
        this.saving = false;
      }
    );
  }

  next() {
    this.onSave.emit();
    this.registrationService.next();
  }

  back() {
    this.registrationService.back();
  }

  finishEditing() {
    this.onSave.emit();
  }

  handleCandidateCertificationCreated(certification: CandidateCertification) {
    let index = -1;
    if (this.candidateCertifications.length) {
      index = this.candidateCertifications.findIndex(cert => cert.id === certification.id);
    }
    /* Replace the old education item with the updated item */
    if (index >= 0) {
      this.candidateCertifications.splice(index, 1, certification);
    } else {
      this.candidateCertifications.push(certification);
    }
    this.addingCertification = false;
  }

  cancel() {
    this.onSave.emit();
  }

  editCandidateCertification(certification: CandidateCertification) {
    this.editTarget = certification;
  }

  handleCertificationSaved(certification: CandidateCertification, i) {
    this.candidateCertifications[i] = certification;
    this.editTarget = null;
  }

}
