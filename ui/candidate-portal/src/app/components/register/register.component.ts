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

import {Component, OnDestroy, OnInit} from '@angular/core';
import {RegistrationService} from "../../services/registration.service";
import {ActivatedRoute, Router} from "@angular/router";
import {LanguageService} from "../../services/language.service";
import {AuthenticationService} from "../../services/authentication.service";
import {CandidateService} from "../../services/candidate.service";
import {Partner} from "../../model/partner";

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.scss']
})
export class RegisterComponent implements OnInit, OnDestroy {

  private partner: Partner;
  private loadingPartner: boolean = false;

  constructor(public registrationService: RegistrationService,
              private candidateService: CandidateService,
              public authenticationService: AuthenticationService,
              private route: ActivatedRoute,
              private languageService: LanguageService,
              public router: Router) { }

  ngOnInit() {

    //Note that we deliberately use a snapshot rather than a subscribe
    //so that we pick up the lang query even if there is a redirect.
    //eg like login redirects to /home if already authenticated.
    //With a subscribe, the query can be lost in the redirect.
    const lang = this.route.snapshot.queryParams['lang'];
    //Need to delay changing language otherwise you get ExpressionChangedAfterItHasBeenCheckedError
    setTimeout(
      () => this.languageService.changeLanguage(lang), 1000
    )

    this.registrationService.start();
  }

  ngOnDestroy(): void {
    this.registrationService.stop();
  }

  logout() {
    this.authenticationService.logout();
  }

  get partnerName(): string {
    if (!this.partner && !this.loadingPartner) {
      this.loadPartner();
    }
    return this.partner?.name;
  }

  private loadPartner() {
    if (!this.partner) {
      this.loadingPartner = true;
      //Grab partner from candidate
      this.candidateService.getCandidatePersonal().subscribe(
        {
          next: candidate => {
            this.partner = candidate?.user?.partner;
            this.loadingPartner = false
          },
          error: err => {
            this.partner = null;
            this.loadingPartner = false;
            console.log("RegisterComponent: failed to load candidate-" + err);
          }
        }
      )
    }
  }

  isRegistered(): boolean {
    return this.authenticationService.isRegistered();
  }
}

