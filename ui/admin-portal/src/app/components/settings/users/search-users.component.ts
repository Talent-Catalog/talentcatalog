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

import {Component, Input, OnInit} from '@angular/core';


import {SearchResults} from '../../../model/search-results';

import {FormBuilder, FormGroup} from "@angular/forms";
import {debounceTime, distinctUntilChanged} from "rxjs/operators";
import {User} from "../../../model/user";
import {UserService} from "../../../services/user.service";
import {CreateUserComponent} from "./create/create-user.component";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {EditUserComponent} from "./edit/edit-user.component";
import {ConfirmationComponent} from "../../util/confirm/confirmation.component";
import {AuthService} from '../../../services/auth.service';
import {ChangePasswordComponent} from "../../account/change-password/change-password.component";
import {ChangeUsernameComponent} from "../../account/change-username/change-username.component";


@Component({
  selector: 'app-search-users',
  templateUrl: './search-users.component.html',
  styleUrls: ['./search-users.component.scss']
})
export class SearchUsersComponent implements OnInit {

  @Input() loggedInUser: User;

  searchForm: FormGroup;
  loading: boolean;
  error: any;
  pageNumber: number;
  pageSize: number;
  results: SearchResults<User>;

  constructor(private fb: FormBuilder,
              private userService: UserService,
              private modalService: NgbModal,
              private authService: AuthService) { }

  ngOnInit() {

  /* SET UP FORM */
    this.searchForm = this.fb.group({
      keyword: [''],
      role: [['limited', 'semilimited', 'sourcepartneradmin', 'admin']],
      status: ['active']
    });
    this.pageNumber = 1;
    this.pageSize = 50;

    this.onChanges();
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
    this.loggedInUser = this.authService.getLoggedInUser();
    this.search();
  }


/* SEARCH FORM */
  search() {
    this.loading = true;
    const request = this.searchForm.value;
    request.pageNumber = this.pageNumber - 1;
    request.pageSize =  this.pageSize;
    this.userService.search(request).subscribe(results => {
      this.results = results;
      this.loading = false;
    });
  }

  addUser() {
    const addUserModal = this.modalService.open(CreateUserComponent, {
      centered: true,
      backdrop: 'static'
    });

    addUserModal.result
      .then((user) => this.search())
      .catch(() => { /* Isn't possible */ });
  }

  editUser(user) {
    const editUserModal = this.modalService.open(EditUserComponent, {
      centered: true,
      backdrop: 'static'
    });

    editUserModal.componentInstance.userId = user.id;

    editUserModal.result
      .then((updatedUser) => {
        this.search()
        // UPDATES VIEW IF LOGGED IN ADMIN USER CHANGES ROLE THEMSELVES
        if (this.loggedInUser.id === updatedUser.id){
          this.authService.setNewLoggedInUser(updatedUser);
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

  updateUsername(user: User) {
    const updateUsernameModal = this.modalService.open(ChangeUsernameComponent, {
      centered: true,
      backdrop: 'static'
    });

    updateUsernameModal.componentInstance.user = user;

    updateUsernameModal.result
      .then(() => {
          this.loading = false;
          this.search();
        },
        (error) => {
          this.error = error;
          this.loading = false;
        });
  }
}
