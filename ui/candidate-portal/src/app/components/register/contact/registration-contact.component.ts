/*
 * Copyright (c) 2021 Talent Beyond Boundaries.
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
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {CandidateService} from "../../../services/candidate.service";
import {Candidate} from "../../../model/candidate";
import {RegistrationService} from "../../../services/registration.service";
import {AuthenticationService} from "../../../services/authentication.service";
import {EMAIL_REGEX} from "../../../model/base";

@Component({
  selector: 'app-registration-contact',
  templateUrl: './registration-contact.component.html',
  styleUrls: ['./registration-contact.component.scss']
})
export class RegistrationContactComponent implements OnInit {

  /* A flag to indicate if the component is being used on the profile component */
  @Input() edit: boolean = false;

  @Output() onSave = new EventEmitter();

  form: FormGroup;
  error: any;
  // Form states
  loading: boolean;
  saving: boolean;
  // Candidate data
  authenticated: boolean;
  candidate: Candidate;

  usAfghan: boolean;

  readonly emailRegex: string = EMAIL_REGEX;

  constructor(private fb: FormBuilder,
              private candidateService: CandidateService,
              private authenticationService: AuthenticationService,
              private registrationService: RegistrationService) { }

  ngOnInit() {
    this.authenticated = false;
    this.loading = true;
    this.candidate = null;
    this.form = this.fb.group({
      email: ['', Validators.required],
      phone: [''],
      whatsapp: [''],
    });

    if (this.authenticationService.isAuthenticated()) {
      this.authenticated = true;
      this.candidateService.getCandidateContact().subscribe(
        (candidate) => {
          this.candidate = candidate;
          this.form.patchValue({
            email: candidate.user ? candidate.user.email : '',
            phone: candidate.phone,
            whatsapp: candidate.whatsapp,
          });
          this.loading = false;
        },
        (error) => {
          this.error = error;
          this.loading = false;
        }
      );
    } else {

      this.loading = false;
    }
  }

  get email(): string {
    return this.form.value.email;
  }

  get phone(): string {
    return this.form.value.phone;
  }

  get whatsapp(): string {
    return this.form.value.whatsapp;
  }

  cancel() {
    this.onSave.emit();
  }

  save() {
    this.saving = true;
    this.error = null;
    if (this.authenticationService.isAuthenticated()) {

      // If the candidate hasn't changed anything, skip the update service call
      if (this.form.pristine) {
        this.registrationService.next();
        this.onSave.emit();
        return;
      }

      // The user has already registered and is either revisiting this page or updating it for the
      // first time
      this.candidateService.updateCandidateContact(this.form.value).subscribe(
        (response) => {
          this.registrationService.next();
          this.onSave.emit();
        },
        (error) => {
          // console.log(error);
          this.error = error;
          this.saving = false;
        }
      );
    } else {
      // No special processing. We expect the user is logged on or has registered before reaching
      // this step
      this.saving = false;
    }
  }

}
