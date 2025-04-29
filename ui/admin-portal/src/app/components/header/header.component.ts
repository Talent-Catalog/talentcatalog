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
import {Router} from "@angular/router";
import {AuthorizationService} from "../../services/authorization.service";
import {CandidateService} from "../../services/candidate.service";
import {Candidate} from "../../model/candidate";
import {Observable, of} from "rxjs";
import {catchError, debounceTime, distinctUntilChanged, map, switchMap, tap} from "rxjs/operators";
import {User} from "../../model/user";
import {BrandingInfo, BrandingService} from "../../services/branding.service";
import {AuthenticationService} from "../../services/authentication.service";
import {UserService} from "../../services/user.service";

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss']
})
export class HeaderComponent implements OnInit {

  isNavbarCollapsed = true;
  doEmailPhoneOrWhatsappSearch;
  doNumberOrNameSearch;
  doExternalIdSearch;
  searchFailed: boolean;
  searching: boolean;
  error;
  loggedInUser: User;
  logo: string;
  websiteUrl: string;

  stagingEnv: boolean = false;


  constructor(
    private authService: AuthorizationService,
    private authenticationService: AuthenticationService,
              private brandingService: BrandingService,
              private candidateService: CandidateService,
              private router: Router) { }

  ngOnInit() {
    this.stagingEnv = window.location.host == 'tctalent-test.org'

    this.brandingService.getBrandingInfo().subscribe(
      (response: BrandingInfo) => {
        this.logo = response.logo;
        this.websiteUrl = response.websiteUrl;
      },
      (error) => this.error = error
    );

    this.doNumberOrNameSearch = (text$: Observable<string>) =>
      text$.pipe(
        debounceTime(300),
        distinctUntilChanged(),
        tap(() => {
          this.searching = true;
          this.error = null
        }),
        switchMap(candidateNumberOrName =>
          this.candidateService.findByCandidateNumberOrName({candidateNumberOrName: candidateNumberOrName, pageSize: 10}).pipe(
            tap(() => this.searchFailed = false),
            map(result => result.content),
            catchError(() => {
              this.searchFailed = true;
              return of([]);
            }))
        ),
        tap(() => this.searching = false)
      );

    this.doExternalIdSearch = (text$: Observable<string>) =>
      text$.pipe(
        debounceTime(300),
        distinctUntilChanged(),
        tap(() => {
          this.searching = true;
          this.error = null
        }),
        switchMap(externalId =>
          this.candidateService.findByExternalId({externalId: externalId, pageSize: 10}).pipe(
            tap(() => this.searchFailed = false),
            map(result => result.content),
            catchError(() => {
              this.searchFailed = true;
              return of([]);
            }))
        ),
        tap(() => this.searching = false)
      );

    this.doEmailPhoneOrWhatsappSearch = (text$: Observable<string>) =>
      text$.pipe(
        debounceTime(300),
        distinctUntilChanged(),
        tap(() => {
          this.searching = true;
          this.error = null
        }),
        switchMap(candidateEmailPhoneOrWhatsapp =>
          this.candidateService.findByCandidateEmailPhoneOrWhatsapp({candidateEmailPhoneOrWhatsapp: candidateEmailPhoneOrWhatsapp, pageSize: 10}).pipe(
            tap(() => this.searchFailed = false),
            map(result => result.content),
            catchError(() => {
              this.searchFailed = true;
              return of([]);
            }))
        ),
        tap(() => this.searching = false)
      );
    this.loggedInUser = this.authenticationService.getLoggedInUser();
    if (this.loggedInUser == null) {
      this.logout();
    }
  }

  renderCandidateRow(candidate: Candidate) {
    if (this.canViewCandidateName()) {
      return candidate.candidateNumber + ": " + candidate.user.firstName + " " + candidate.user.lastName;
    } else {
      return candidate.candidateNumber;
    }
  }

  loggedInUserInfo(): string {
    if (this.loggedInUser?.partner == null) {
      //If we don't know our source partner, major issue - so just logout.
      this.logout();
    }

    let info: string;
    if (this.loggedInUser == null) {
      info = "Not logged in";
    } else {
      let user = this.loggedInUser;
      info = UserService.userToString(user, true, true);
    }

    return info;
  }

  logout() {
    this.authenticationService.logout();
  }

  selectSearchResult ($event, input) {
    $event.preventDefault();
    input.value = '';
    this.router.navigate(['candidate',  $event.item.candidateNumber]);

  }

  canViewCandidateName(): boolean {
    return this.authService.canViewCandidateName();
  }

  isEmployerPartner(): boolean {
    return this.authService.isEmployerPartner();
  }

  isAnAdmin(): boolean {
    return this.authService.isAnAdmin();
  }

  isSystemAdminOnly(): boolean {
    return this.authService.isSystemAdminOnly();
  }

  isStagingEnv(): boolean {
    return window.location.host == 'tctalent-test.org';
  }

  isLocalEnv(): boolean {
    return window.location.host == 'localhost:4201';
  }
}
