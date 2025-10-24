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

import {Component, OnInit} from '@angular/core';
import {UntypedFormBuilder, UntypedFormGroup, Validators} from '@angular/forms';
import {Role, UpdateUserRequest, User} from "../../../../model/user";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {UserService} from "../../../../services/user.service";
import {AuthorizationService} from "../../../../services/authorization.service";
import {CountryService} from "../../../../services/country.service";
import {Country} from "../../../../model/country";
import {EnumOption, enumOptions} from "../../../../util/enum";
import {PartnerService} from "../../../../services/partner.service";
import {Partner} from "../../../../model/partner";
import {forkJoin} from "rxjs";
import {EMAIL_REGEX, SearchUserRequest, Status} from "../../../../model/base";
import {AuthenticationService} from "../../../../services/authentication.service";

@Component({
  selector: 'app-create-update-user',
  templateUrl: './create-update-user.component.html',
  styleUrls: ['./create-update-user.component.scss']
})
export class CreateUpdateUserComponent implements OnInit {

  user: User;
  userForm: UntypedFormGroup;
  error;
  working: boolean;

  roleOptions: EnumOption[] = enumOptions(Role);
  countries: Country[];
  partners: Partner[];
  approvers: User[];

  readonly emailRegex: string = EMAIL_REGEX;

  constructor(private activeModal: NgbActiveModal,
              private fb: UntypedFormBuilder,
              private partnerService: PartnerService,
              private userService: UserService,
              private authenticationService: AuthenticationService,
              private authorizationService: AuthorizationService,
              private countryService: CountryService) {
  }

  ngOnInit() {
    let formControlsConfig = {
      email: [this.user?.email, Validators.required],
      username: [this.user?.username, Validators.required],
      firstName: [this.user?.firstName, Validators.required],
      lastName: [this.user?.lastName, Validators.required],
      partnerId: [this.user?.partner.id, Validators.required],
      status: [this.user? this.user.status : Status.active],
      role: [this.user?.role, Validators.required],
      jobCreator: [this.user ? this.user.jobCreator : false],
      approverId: [this.user?.approver?.id],
      purpose: [this.user?.purpose],
      sourceCountries: [this.user?.sourceCountries],
      readOnly: [this.user ? this.user.readOnly : false],
      usingMfa: [this.user ? this.user.usingMfa : true]
    }

    //Password is required field in user creation only
    if (this.create) {
      formControlsConfig["password"] = [null, Validators.required];

      //Need to initialize partnerId to existing user's partner
      formControlsConfig["partnerId"] = [this.authenticationService.getLoggedInUser()?.partner?.id];
    }

    this.userForm = this.fb.group(formControlsConfig);

    this.working = true;
    this.error = null;

    // Populates the 'Approver' dropdown on the add/update user form with eligible admin users
    const userRequest: SearchUserRequest = {
      sortFields: ["firstName", "lastName"],
      sortDirection: "ASC",
      status: "active",
    };

    forkJoin({
      'countries': this.countryService.listCountriesRestricted(),
      'partners': this.partnerService.listPartners(),
      'approvers': this.userService.search(userRequest)
    }).subscribe(
      results => {
        this.working = false;
        this.countries = results['countries'];
        this.partners = results['partners'];
        this.approvers = results['approvers'].map(u => {u.name = u.firstName + " " + u.lastName; return u});
      },
      (error) => {
        this.error = error;
        this.working = false;
      }
    );

    //Filter who can set which roles
    const role = this.authorizationService.getLoggedInRole();
    if (role === Role.admin) {
      this.roleOptions = this.roleOptions.filter(
        r => ![Role.systemadmin].includes(Role[r.key]));
    }

    if (role === Role.partneradmin) {
      this.roleOptions = this.roleOptions.filter(
        r => ![Role.systemadmin, Role.admin].includes(Role[r.key]));
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

    const request: UpdateUserRequest = {
      email: this.userForm.value.email,
      firstName: this.userForm.value.firstName,
      lastName: this.userForm.value.lastName,
      partnerId: this.userForm.value.partnerId,
      readOnly: this.userForm.value.readOnly,
      role: this.userForm.value.role,
      jobCreator: this.userForm.value.jobCreator,
      approverId: this.userForm.value.approverId,
      purpose: this.userForm.value.purpose,
      sourceCountries: this.userForm.value.sourceCountries,
      status: this.userForm.value.status,
      username: this.userForm.value.username,
      usingMfa: this.userForm.value.usingMfa
    }

    if (this.create) {
      request.password = this.userForm.value.password;
      this.userService.create(request).subscribe(
        (user) => {
          this.closeModal(user);
          this.working = false;
        },
        (error) => {
          this.error = error;
          this.working = false;
        });

    } else {
      this.userService.update(this.user.id, request).subscribe(
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

  canAssignPartner(): boolean {
    return this.authorizationService.canAssignPartner();
  }
}
