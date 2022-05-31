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
import {PartnerService} from "../../../../services/partner.service";
import {Partner} from "../../../../model/partner";
import {forkJoin} from "rxjs";

@Component({
  selector: 'app-create-update-user',
  templateUrl: './create-update-user.component.html',
  styleUrls: ['./create-update-user.component.scss']
})
export class CreateUpdateUserComponent implements OnInit {

  user: User;
  userForm: FormGroup;
  error;
  working: boolean;

  roleOptions: EnumOption[] = enumOptions(AdminRole);
  countries: Country[];
  partners: Partner[];

  constructor(private activeModal: NgbActiveModal,
              private fb: FormBuilder,
              private partnerService: PartnerService,
              private userService: UserService,
              private authService: AuthService,
              private countryService: CountryService) {
  }

  ngOnInit() {
    let formControlsConfig = {
      email: [this.user?.email, [Validators.required, Validators.email]],
      username: [this.user?.username, Validators.required],
      firstName: [this.user?.firstName, Validators.required],
      lastName: [this.user?.lastName, Validators.required],
      partnerId: [this.user?.sourcePartner.id],
      status: [this.user?.status],
      role: [this.user?.role, Validators.required],
      sourceCountries: [this.user?.sourceCountries],
      readOnly: [this.user ? this.user.readOnly : false],
      usingMfa: [this.user ? this.user.usingMfa : true]
    }

    //Password is required field in user creation only
    if (this.create) {
      formControlsConfig["password"] = [null, Validators.required];
    }

    this.userForm = this.fb.group(formControlsConfig);

    this.working = true;
    this.error = null;

    forkJoin({
      'countries': this.countryService.listCountriesRestricted(),
      'partners': this.partnerService.listPartners()
    }).subscribe(
      results => {
        this.working = false;
        this.countries = results['countries'];
        this.partners = results['partners'];
      },
      (error) => {
        this.error = error;
        this.working = false;
      }
    );

    //todo move to this  this.roleOptions = this.authService.assignableUserRoles().map(r => AdminRole[r]);
    //Filter who can set which roles
    if (this.authService.getLoggedInUser().role === "admin") {
      this.roleOptions = this.roleOptions.filter(
        r => !["systemadmin", "admin"].includes(r.key));
    }

    if (this.authService.getLoggedInUser().role === "sourcepartneradmin") {
      this.roleOptions = this.roleOptions.filter(
        r => !["systemadmin", "admin", "sourcepartneradmin"].includes(r.key));
    }

  }

  get create(): boolean {
    return !this.user;
  }

  get title(): string {
    return this.create ? "Add New User"
      : "Update User";
  }

  onSave() {
    this.working = true;

    //todo populate UpdateUserRequest from form. - see create-update-partner

    if (this.create) {
      this.userService.create(this.userForm.value).subscribe(
        (user) => {
          this.closeModal(user);
          this.working = false;
        },
        (error) => {
          this.error = error;
          this.working = false;
        });

    } else {
      this.userService.update(this.user.id, this.userForm.value).subscribe(
        (user) => {
          this.closeModal(user);
          this.working = false;
        },
        (error) => {
          this.error = error;
          this.working = false;
        });
    }
  }

  closeModal(user: User) {
    this.activeModal.close(user);
  }

  dismiss() {
    this.activeModal.dismiss(false);
  }

  canAssignPartner(): boolean {
    return this.authService.canAssignPartner();
  }
}
