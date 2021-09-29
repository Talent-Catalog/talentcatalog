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


  constructor(private authService: AuthService,
              private candidateService: CandidateService,
              private router: Router) { }

  ngOnInit() {

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
    if (this.isUserLimited()) {
      return candidate.candidateNumber;
    } else {
      return candidate.candidateNumber + ": " + candidate.user.firstName + " " + candidate.user.lastName;
    }
  }


  logout() {
    this.authService.logout();
    this.router.navigate(['login']);
    localStorage.clear();
  }

  selectSearchResult ($event, input) {
    $event.preventDefault();
    input.value = '';
    this.router.navigate(['candidate',  $event.item.candidateNumber]);

  }

  isUserLimited(): boolean {
    const role = this.loggedInUser ? this.loggedInUser.role : null;
    return role === 'semilimited' || role === 'limited';
  }
}
