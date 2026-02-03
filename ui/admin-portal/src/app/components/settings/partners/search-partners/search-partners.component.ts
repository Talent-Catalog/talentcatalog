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
import {AuthorizationService} from "../../../../services/authorization.service";
import {UntypedFormBuilder, UntypedFormGroup} from "@angular/forms";
import {debounceTime, distinctUntilChanged} from "rxjs/operators";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {Partner, sourceCountriesAsString} from "../../../../model/partner";
import {PartnerService} from "../../../../services/partner.service";
import {SearchPartnerRequest} from "../../../../model/base";
import {
  CreateUpdatePartnerComponent
} from "../create-update-partner/create-update-partner.component";
import {User} from "../../../../model/user";
import {ConfirmationComponent} from "../../../util/confirm/confirmation.component";
import {SearchResults} from "../../../../model/search-results";

/*
   MODEL - Delegate all authentication logic to authService
 */

@Component({
  selector: 'app-search-partners',
  templateUrl: './search-partners.component.html',
  styleUrls: ['./search-partners.component.scss']
})
export class SearchPartnersComponent implements OnInit {
  error: any;
  loading: boolean;
  pageNumber: number;
  pageSize: number;
  readOnly: boolean;
  results: SearchResults<Partner>;
  searchForm: UntypedFormGroup;

  constructor(
    private partnerService: PartnerService,
    private fb: UntypedFormBuilder,
    private modalService: NgbModal,
    private authService: AuthorizationService) { }

  ngOnInit(): void {
    this.searchForm = this.fb.group({
      keyword: [''],
      status: ['active'],
    });
    this.pageNumber = 1;
    this.pageSize = 50;

    this.readOnly = this.authService.isReadOnly();

    //Monitor form changes
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

  /**
   * Search based on current search form contents
   */
  search() {
    this.error = null;
    this.loading = true;
    const request: SearchPartnerRequest =  {
      keyword: this.searchForm.value.keyword,
      status: this.searchForm.value.status,
      pageNumber: this.pageNumber - 1,
      pageSize: this.pageSize,
      sortFields: ['id'],
      sortDirection: 'ASC',
    };

    this.partnerService.searchPaged(request).subscribe(
      results => {
      this.results = results;
      this.loading = false;
    },
      error => {
        this.error = error;
        this.loading = false;
      });
  }

  addPartner() {
    const addPartnerModal = this.modalService.open(CreateUpdatePartnerComponent, {
      centered: true,
      backdrop: 'static'
    });

    addPartnerModal.result
    .then((partner: Partner) => this.postProcessPartner(partner))
    .catch(() => {});
  }

  editPartner(partner: Partner) {
    const editPartnerModal = this.modalService.open(CreateUpdatePartnerComponent, {
      centered: true,
      backdrop: 'static'
    });

    editPartnerModal.componentInstance.partner = partner;

    editPartnerModal.result
    .then((partner: Partner) => this.postProcessPartner(partner))
    .catch(() => {});
  }

  /**
   * Called following add or update of a partner
   * @param partner Partner that has just been added or updated.
   * @private
   */
  private postProcessPartner(partner: Partner): void {
    //Display publicApiKey if there is one
    if (partner.publicApiKey != null) {
      //Pop up api key display
      const confirmationModal = this.modalService.open(ConfirmationComponent);
      confirmationModal.componentInstance.title = "Copy partner's public API key";
      confirmationModal.componentInstance.showCancel = false;
      confirmationModal.componentInstance.message =
        "This public API key, " + partner.publicApiKey + " should be provided to the partner." +
        " It should not be saved anywhere." +
        " It will not be displayed again.";
      confirmationModal.result
      .then((result) => {})
      .catch(() => {});

      console.log(partner.publicApiKey);
    }
    this.search();
  }

  sourceCountries(partner: Partner) {
    return sourceCountriesAsString(partner);
  }

  showContact(user: User): string {

    return user ? user.firstName + " " + user.lastName + "(" + user.email + ")" : "";
  }

  canAccessSalesforce(): boolean {
    return this.authService.canAccessSalesforce();
  }

}
