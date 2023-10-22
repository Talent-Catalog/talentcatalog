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
import {Router} from "@angular/router";
import {AuthService} from "../../services/auth.service";
import {CandidateService} from "../../services/candidate.service";
import {Candidate} from "../../model/candidate";
import {Observable, of} from "rxjs";
import {catchError, debounceTime, distinctUntilChanged, map, switchMap, tap} from "rxjs/operators";
import {User} from "../../model/user";
import {BrandingInfo, BrandingService} from "../../services/branding.service";
import {ChatService} from "../../services/chat.service";

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss']
})
export class HeaderComponent implements OnInit {

  isNavbarCollapsed = true;
  doEmailOrPhoneSearch;
  doNumberOrNameSearch;
  doExternalIdSearch;
  searchFailed: boolean;
  searching: boolean;
  error;
  loggedInUser: User;
  logo: string;
  websiteUrl: string;

  stagingEnv: boolean = false;


  constructor(private authService: AuthService,
              private brandingService: BrandingService,
              private candidateService: CandidateService,
              private chatService: ChatService,
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

    this.doEmailOrPhoneSearch = (text$: Observable<string>) =>
      text$.pipe(
        debounceTime(300),
        distinctUntilChanged(),
        tap(() => {
          this.searching = true;
          this.error = null
        }),
        switchMap(candidateEmailOrPhone =>
          this.candidateService.findByCandidateEmailOrPhone({candidateEmailOrPhone: candidateEmailOrPhone, pageSize: 10}).pipe(
            tap(() => this.searchFailed = false),
            map(result => result.content),
            catchError(() => {
              this.searchFailed = true;
              return of([]);
            }))
        ),
        tap(() => this.searching = false)
      );
    this.loggedInUser = this.authService.getLoggedInUser();
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
      info = this.loggedInUser.username + " (" + this.loggedInUser.partner?.abbreviation
        + " " + this.loggedInUser.role + ")";
    }
    return info;
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['login']);
    localStorage.clear();
    this.chatService.unsubscribeAll();
  }

  selectSearchResult ($event, input) {
    $event.preventDefault();
    input.value = '';
    this.router.navigate(['candidate',  $event.item.candidateNumber]);

  }

  canCreateJob(): boolean {
    return this.authService.canCreateJob();
  }

  canViewCandidateName(): boolean {
    return this.authService.canViewCandidateName();
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
