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

import {Component, Input, OnInit} from '@angular/core';


import {SearchResults} from '../../../model/search-results';

import {UntypedFormBuilder, UntypedFormGroup} from "@angular/forms";
import {debounceTime, distinctUntilChanged} from "rxjs/operators";
import {Role, roleGreaterThan, User} from "../../../model/user";
import {UserService} from "../../../services/user.service";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {CreateUpdateUserComponent} from "./create-update-user/create-update-user.component";
import {ConfirmationComponent} from "../../util/confirm/confirmation.component";
import {AuthorizationService} from '../../../services/authorization.service';
import {ChangePasswordComponent} from "../../account/change-password/change-password.component";
import {EnumOption, enumOptions} from "../../../util/enum";
import {SearchUserRequest} from "../../../model/base";
import {AuthenticationService} from "../../../services/authentication.service";


@Component({
  selector: 'app-search-users',
  templateUrl: './search-users.component.html',
  styleUrls: ['./search-users.component.scss']
})
export class SearchUsersComponent implements OnInit {

  @Input() loggedInUser: User;

  searchForm: UntypedFormGroup;
  loading: boolean;
  error: any;
  pageNumber: number;
  pageSize: number;
  results: SearchResults<User>;
  roleOptions: EnumOption[] = enumOptions(Role);

  constructor(private fb: UntypedFormBuilder,
              private userService: UserService,
              private modalService: NgbModal,
              private authService: AuthorizationService,
              private authenticationService: AuthenticationService
              ) { }

  ngOnInit() {

  /* SET UP FORM */
    this.searchForm = this.fb.group({
      keyword: [''],
      role: [],
      status: ['active']
    });
    this.pageNumber = 1;
    this.pageSize = 50;

    this.onChanges();
    if (this.authService.getLoggedInRole() === Role.partneradmin) {
      this.roleOptions = this.roleOptions.filter(r => r.key !== "admin" && r.key !== "partneradmin" );
    }
  }

  onChanges(): void {
    /* SEARCH ON CHANGE*/
    this.searchForm.valueChanges
      .pipe(
        debounceTime(400),
        distinctUntilChanged()
      )
      .subscribe(res => {
        this.search();
      });
    this.search();
  }

  getLoggedInUser(){
    /* GET LOGGED IN USER ROLE FROM LOCAL STORAGE */
    this.loggedInUser = this.authenticationService.getLoggedInUser();
    this.search();
  }

/* SEARCH FORM */
  search() {
    this.loading = true;
    const request: SearchUserRequest = this.searchForm.value;
    request.pageNumber = this.pageNumber - 1;
    request.pageSize =  this.pageSize;

    //Partners other than the default  partner only see users for their partner.
    if (!this.authService.isDefaultPartner()) {
      request.partnerId = this.loggedInUser.partner.id;
    }
    this.userService.searchPaged(request).subscribe(results => {
      this.results = results;
      this.loading = false;
    });
  }

  addUser() {
    const addUserModal = this.modalService.open(CreateUpdateUserComponent, {
      centered: true,
      backdrop: 'static'
    });

    addUserModal.result
      .then((user) => this.search())
      .catch(() => { /* Isn't possible */ });
  }

  editUser(user) {
    const editUserModal = this.modalService.open(CreateUpdateUserComponent, {
      centered: true,
      backdrop: 'static'
    });

    editUserModal.componentInstance.user = user;

    editUserModal.result
      .then((updatedUser) => {
        this.search()
        // UPDATES VIEW IF LOGGED IN ADMIN USER CHANGES ROLE THEMSELVES
        if (this.loggedInUser.id === updatedUser.id){
          this.authenticationService.setLoggedInUser(updatedUser);
          this.getLoggedInUser();
        }
      })
      .catch(() => { /* Isn't possible */ });
  }

  deleteUser(user) {
    const deleteUserModal = this.modalService.open(ConfirmationComponent, {
      centered: true,
      backdrop: 'static'
    });

    deleteUserModal.componentInstance.message = 'Are you sure you want to delete ' + user.username;

    deleteUserModal.result
      .then((result) => {
        if (result === true) {
          this.userService.delete(user.id).subscribe(
            (done) => {
              this.loading = false;
              this.search();
            },
            (error) => {
              this.error = error;
              this.loading = false;
            });
          this.search()
        }
      })
      .catch(() => { /* Isn't possible */ });

  }
  updatePassword(user: User) {
    const updatePasswordModal = this.modalService.open(ChangePasswordComponent, {
      centered: true,
      backdrop: 'static'
    });

    updatePasswordModal.componentInstance.user = user;

    updatePasswordModal.result
      .then(() => console.log('password updated'))
      .catch(() => { /* Isn't possible */ });

  }

  resetAuthentication(user: User) {
    this.loading = true;
    this.userService.resetMfa(user.id).subscribe(
      () => {
        this.loading = false;
      },
      (error) => {
        this.error = error;
        this.loading = false;
      });
  }

  isAnAdmin(): boolean {
    return this.authService.isAnAdmin();
  }

  getSourceCountries(user: User): string[] {
    return user.sourceCountries.map(c => ' ' + c.name );
  }

  canEdit(user: User): boolean {
    const myUser = this.loggedInUser;
    const myRole = this.authService.getLoggedInRole();

    let editable: boolean;
    switch (myRole) {
      case Role.systemadmin:
        editable = true;
        break;

      case Role.admin:
      case Role.partneradmin:
        if (user.partner?.id !== myUser.partner?.id) {
          //Can't edit another partner's user.
          editable = false;
        } else {
          //Can't edit users whose role is greater than mine.
          editable = !roleGreaterThan(Role[user?.role], myRole);
        }
        break;

      default:
        //Other user roles can't edit at all.
        editable = false;
    }

    return editable;
  }
}
