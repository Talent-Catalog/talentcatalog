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
import {UntypedFormBuilder, UntypedFormGroup} from "@angular/forms";
import {SystemLanguage} from "../../../model/language";
import {LanguageService} from "../../../services/language.service";
import {CreateLanguageComponent} from "./create/create-language.component";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {User} from "../../../model/user";
import {AuthorizationService} from "../../../services/authorization.service";

@Component({
  selector: 'app-search-languages',
  templateUrl: './search-languages.component.html',
  styleUrls: ['./search-languages.component.scss']
})
export class SearchLanguagesComponent implements OnInit {

  @Input() loggedInUser: User;

  searchForm: UntypedFormGroup;
  loading: boolean;
  error: any;
  pageNumber: number;
  pageSize: number;
  results: SystemLanguage[];


  constructor(private fb: UntypedFormBuilder,
              private languageService: LanguageService,
              private modalService: NgbModal,
              private authService: AuthorizationService) {
  }

  ngOnInit() {
    this.search();
  }
  
  search() {
    this.loading = true;
    this.languageService.listSystemLanguages().subscribe(results => {
      this.results = results;
      this.loading = false;
    });
  }

  addLanguage() {
    const addLanguageModal = this.modalService.open(CreateLanguageComponent, {
      centered: true,
      backdrop: 'static'
    });

    addLanguageModal.result
      .then((language) => this.search())
      .catch(() => { /* Isn't possible */ });
  }

  isAnAdmin(): boolean {
    return this.authService.isAnAdmin();
  }
}
