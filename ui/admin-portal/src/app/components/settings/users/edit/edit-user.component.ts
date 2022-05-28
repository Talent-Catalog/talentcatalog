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

import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {AdminRole, User} from "../../../../model/user";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {UserService} from "../../../../services/user.service";
import {AuthService} from "../../../../services/auth.service";
import {CountryService} from "../../../../services/country.service";
import {Country} from "../../../../model/country";
import {EnumOption, enumOptions} from "../../../../util/enum";

@Component({
  selector: 'app-edit-user',
  templateUrl: './edit-user.component.html',
  styleUrls: ['./edit-user.component.scss']
})
export class EditUserComponent implements OnInit {

  userId: number;
  userForm: FormGroup;
  error;
  loading: boolean;
  saving: boolean;

  roleOptions: EnumOption[] = enumOptions(AdminRole);
  countries: Country[];

  constructor(private activeModal: NgbActiveModal,
              private fb: FormBuilder,
              private userService: UserService,
              private authService: AuthService,
              private countryService: CountryService) {
  }

  ngOnInit() {
    this.loading = true;
    this.userService.get(this.userId).subscribe(user => {
      this.userForm = this.fb.group({
        email: [user.email, [Validators.required, Validators.email]],
        username: [user.username, Validators.required],
        firstName: [user.firstName, Validators.required],
        lastName: [user.lastName, Validators.required],
        status: [user.status, Validators.required],
        role: [user.role, Validators.required],
        sourceCountries: [user.sourceCountries],
        readOnly: [user.readOnly],
        usingMfa: [user.usingMfa]
      });
      this.loading = false;
    });

    this.countryService.listCountriesRestricted().subscribe(
      (response) => {
        this.countries = response;
      },
      (error) => {
        this.error = error;
        this.loading = false;
      }
    );

    if (this.authService.getLoggedInUser().role === "sourcepartneradmin") {
      this.roleOptions = this.roleOptions.filter(r => r.key !== "admin" && r.key !== "sourcepartneradmin" );
    }

  }

  onSave() {
    this.saving = true;
    this.userService.update(this.userId, this.userForm.value).subscribe(
      (user) => {
        this.closeModal(user);
        this.saving = false;
      },
      (error) => {
        this.error = error;
        this.saving = false;
      });
  }

  closeModal(user: User) {
    this.activeModal.close(user);
  }

  dismiss() {
    this.activeModal.dismiss(false);
  }

}
