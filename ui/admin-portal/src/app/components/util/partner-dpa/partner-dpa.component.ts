/*
 * Copyright (c) 2025 Talent Catalog.
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
import {Router} from '@angular/router';
import {forkJoin} from 'rxjs';
import {TermsInfoDto, TermsType} from "../../../model/terms-info-dto";
import {AuthenticationService} from "../../../services/authentication.service";
import {TermsInfoService} from "../../../services/terms-info.service";
import {PartnerService} from "../../../services/partner.service";
import {DtoType} from "../../../model/base";
import {Partner} from "../../../model/partner";
import {AuthorizationService} from "../../../services/authorization.service";

@Component({
  selector: 'app-partner-dpa',
  templateUrl: './partner-dpa.component.html',
  styleUrls: ['./partner-dpa.component.scss']
})
export class PartnerDpaComponent implements OnInit {
  content: string;
  currentDpa: TermsInfoDto;
  organizationName: string;
  termsRead: boolean = false;
  acceptedDpaDate: string | null = null;
  acceptedDpaId: string | null = null;
  error: any;
  loading: boolean = false;
  isSourcePartner: boolean = false;

  constructor(
    private authorizationService: AuthorizationService,
    private authenticationService: AuthenticationService,
    private termsInfoService: TermsInfoService,
    private partnerService: PartnerService,
    private router: Router
  ) {
  }

  ngOnInit(): void {
    this.loading = true;
    const user = this.authenticationService.getLoggedInUser();
    this.isSourcePartner = this.authorizationService.isSourcePartner();
    forkJoin({
      currentDpa: this.termsInfoService.getCurrentByType(TermsType.DATA_PROCESSING_AGREEMENT),
      partner: this.partnerService.getPartner(user.partner.id, DtoType.MINIMAL)
    }).subscribe({
      next: (results) => {
        this.configure(results.partner, results.currentDpa);
        if (!results.partner?.firstDpaSeenDate) {
          this.partnerService.setFirstDpaSeen().subscribe({
            next: (updatedPartner) => {
              this.configure(updatedPartner, results.currentDpa);
              this.loading = false;
            },
            error: (err) => {
              this.error = err;
              this.loading = false;
            }
          });
        } else {
          this.loading = false;
        }

      },
      error: (err) => {
        this.error = err;
        this.loading = false;
      }
    });
  }

  private configure(partner: Partner, currentDpa: TermsInfoDto) {
    this.currentDpa = currentDpa;
    this.organizationName = partner.name;
    this.content = currentDpa.content.replace('[Your Organization]', partner.name);
    this.acceptedDpaId = partner.acceptedDataProcessingAgreementId;
    this.acceptedDpaDate = partner.acceptedDataProcessingAgreementDate;
  }

  onScroll(event: Event) {
    const element = event.target as HTMLElement;
    if (element.scrollHeight - element.scrollTop <= element.clientHeight + 50) {
      this.termsRead = true;
    }
  }

  acceptTerms() {
    if (!this.termsRead) return;
    this.loading = true;
    this.partnerService.updateAcceptedDpa(this.currentDpa.id).subscribe({
      next: (partner: Partner) => {
        this.acceptedDpaId = partner.acceptedDataProcessingAgreementId;
        this.acceptedDpaDate = partner.acceptedDataProcessingAgreementDate;
        this.loading = false;
        this.router.navigateByUrl('/');
      },
      error: (err) => {
        this.error = err;
        this.loading = false;
      }
    });
  }

  skipDpa() {
    this.router.navigateByUrl('/');
  }
}
