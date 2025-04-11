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
import {Country} from "../../../model/country";
import {CountryService} from "../../../services/country.service";
import {CreateCountryComponent} from "./create/create-country.component";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {EditCountryComponent} from "./edit/edit-country.component";
import {ConfirmationComponent} from "../../util/confirm/confirmation.component";
import {User} from "../../../model/user";
import {AuthorizationService} from "../../../services/authorization.service";

@Component({
  selector: 'app-search-countries',
  templateUrl: './search-countries.component.html',
  styleUrls: ['./search-countries.component.scss']
})
export class SearchCountriesComponent implements OnInit {

  @Input() loggedInUser: User;

  searchForm: UntypedFormGroup;
  loading: boolean;
  error: any;
  pageNumber: number;
  pageSize: number;
  results: SearchResults<Country>;


  constructor(private fb: UntypedFormBuilder,
              private countryService: CountryService,
              private modalService: NgbModal,
              private authService: AuthorizationService) {
  }

  ngOnInit() {

    /* SET UP FORM */
    this.searchForm = this.fb.group({
      keyword: [''],
      status: ['active'],
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

  /* SEARCH FORM */
  search() {
    this.loading = true;
    let request = this.searchForm.value;
    request.pageNumber = this.pageNumber - 1;
    request.pageSize = this.pageSize;
    this.countryService.searchPaged(request).subscribe(results => {
      this.results = results;
      this.loading = false;
    });
  }

  addCountry() {
    const addCountryModal = this.modalService.open(CreateCountryComponent, {
      centered: true,
      backdrop: 'static'
    });

    addCountryModal.result
      .then((country) => this.search())
      .catch(() => { /* Isn't possible */ });
  }

  editCountry(country) {
    const editCountryModal = this.modalService.open(EditCountryComponent, {
      centered: true,
      backdrop: 'static'
    });

    editCountryModal.componentInstance.countryId = country.id;

    editCountryModal.result
      .then((country) => this.search())
      .catch(() => { /* Isn't possible */ });
  }

  deleteCountry(country) {
    const deleteCountryModal = this.modalService.open(ConfirmationComponent, {
      centered: true,
      backdrop: 'static'
    });

    deleteCountryModal.componentInstance.message = 'Are you sure you want to delete '+country.name;

    deleteCountryModal.result
      .then((result) => {
        // console.log(result);
        if (result === true) {
          this.countryService.delete(country.id).subscribe(
            (country) => {
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

  isAnAdmin(): boolean {
    return this.authService.isAnAdmin();
  }
}
