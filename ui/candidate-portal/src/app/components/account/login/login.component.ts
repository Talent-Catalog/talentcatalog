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
import {UntypedFormBuilder, UntypedFormGroup, Validators} from "@angular/forms";
import {ActivatedRoute, Router} from "@angular/router";
import {CandidateService} from "../../../services/candidate.service";
import {AuthenticationService} from "../../../services/authentication.service";
import {LoginRequest} from "../../../model/base";
import {ChangePasswordComponent} from '../change-password/change-password.component';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {Candidate, CandidateStatus} from "../../../model/candidate";
import {forkJoin} from "rxjs";
import {TermsInfoDto, TermsType} from "../../../model/terms-info-dto";
import {TermsInfoService} from "../../../services/terms-info.service";

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit {

  loginForm: UntypedFormGroup;
  loading: boolean;
  returnUrl: string;
  error;

  constructor(private builder: UntypedFormBuilder,
              private authenticationService: AuthenticationService,
              private candidateService: CandidateService,
              private route: ActivatedRoute,
              private router: Router,
              private termsInfoService: TermsInfoService,
              private modalService: NgbModal) {
  }

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      this.returnUrl = params['returnUrl'] || '/home';
    });

    this.loginForm = this.builder.group({
      username: ['', Validators.required],
      password: ['', Validators.required]
    })
  }

  get username(): string {
    return this.loginForm.value.username;
  }

  get password(): string {
    return this.loginForm.value.password;
  }

  login() {
    this.error = null;
    if (this.loginForm.invalid) {
      return;
    }
    if (this.loading) { return; }
    this.loading = true;

    this.loginWithToken(null);
  }

  private loginWithToken(token: string) {
    const req: LoginRequest = new LoginRequest();
    req.username = this.username;
    req.password = this.password;
    req.reCaptchaV3Token = token;

    this.authenticationService.login(req)
      .subscribe(() => {
        this.loading = false;

        //Fetch the current candidate privacy policy and candidate info
        forkJoin({
          'currentPolicy': this.termsInfoService.getCurrentByType(TermsType.CANDIDATE_PRIVACY_POLICY),
          'candidate': this.candidateService.getCandidatePersonal()
        }).subscribe(
          results => {
            this.configure(results.candidate, results.currentPolicy)
          },
          err => this.error = err
        )
      }, error => {
        console.log(error);
        this.error = error;
        this.loading = false;
      });
  }

  private configure(candidate: Candidate, currentPolicy: TermsInfoDto) {
    //Save candidate number in storage to display in the header
    this.candidateService.setCandNumberStorage(candidate.candidateNumber);
    //Remember status
    this.authenticationService.setCandidateStatus(CandidateStatus[candidate.status]);


    //Check if the latest terms exist and whether the candidate has accepted the latest terms
    if (currentPolicy.content.length > 0 && currentPolicy.id != candidate.acceptedPrivacyPolicyId) {
      //Candidate needs to accept current policy. Ignore any returnUrl and send for normal
      //"home" processing which will prompt them to accept the policy.
      this.router.navigateByUrl("/home");
    } else {
      if (candidate.changePassword  === true) {
        this.modalService.open(ChangePasswordComponent, {
          centered: true
        });
      }
      this.router.navigateByUrl(this.returnUrl);
    }
  }
}

