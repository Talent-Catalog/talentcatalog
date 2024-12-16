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

import {UntypedFormBuilder, UntypedFormGroup, Validators} from "@angular/forms";
import {debounceTime, distinctUntilChanged} from "rxjs/operators";
import {LanguageLevel} from "../../../model/language-level";
import {LanguageLevelService} from "../../../services/language-level.service";
import {CreateLanguageLevelComponent} from "./create/create-language-level.component";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {EditLanguageLevelComponent} from "./edit/edit-language-level.component";
import {ConfirmationComponent} from "../../util/confirm/confirmation.component";
import {User} from "../../../model/user";
import {AuthorizationService} from "../../../services/authorization.service";
import {FileSelectorComponent} from "../../util/file-selector/file-selector.component";

@Component({
  selector: 'app-search-language-levels',
  templateUrl: './search-language-levels.component.html',
  styleUrls: ['./search-language-levels.component.scss']
})
export class SearchLanguageLevelsComponent implements OnInit {

  @Input() loggedInUser: User;

  importForm: UntypedFormGroup;
  searchForm: UntypedFormGroup;
  loading: boolean;
  error: any;
  pageNumber: number;
  pageSize: number;
  results: SearchResults<LanguageLevel>;

  constructor(private fb: UntypedFormBuilder,
              private languageLevelService: LanguageLevelService,
              private modalService: NgbModal,
              private authService: AuthorizationService) {
  }

  ngOnInit() {

    /* SET UP FORMS */
    this.importForm = this.fb.group({
      langCode: [null, Validators.required]
    });

    this.searchForm = this.fb.group({
      keyword: [''],
      status: ['active'],
    });
    this.pageNumber = 1;
    this.pageSize = 50;

    this.onChanges();
  }

  get langCode(): string {
    return this.importForm.value?.langCode;
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
    const request = this.searchForm.value;
    request.pageNumber = this.pageNumber - 1;
    request.pageSize = this.pageSize;
    this.languageLevelService.search(request).subscribe(results => {
      this.results = results;
      this.loading = false;
    });
  }

  addLanguageLevel() {
    const addLanguageLevelModal = this.modalService.open(CreateLanguageLevelComponent, {
      centered: true,
      backdrop: 'static'
    });

    addLanguageLevelModal.result
      .then((languageLevel) => this.search())
      .catch(() => { /* Isn't possible */ });
  }

  editLanguageLevel(languageLevel) {
    const editLanguageLevelModal = this.modalService.open(EditLanguageLevelComponent, {
      centered: true,
      backdrop: 'static'
    });

    editLanguageLevelModal.componentInstance.languageLevelId = languageLevel.id;

    editLanguageLevelModal.result
      .then(() => this.search())
      .catch(() => { /* Isn't possible */ });
  }

  deleteLanguageLevel(languageLevel) {
    const deleteLanguageLevelModal = this.modalService.open(ConfirmationComponent, {
      centered: true,
      backdrop: 'static'
    });

    deleteLanguageLevelModal.componentInstance.message = 'Are you sure you want to delete ' + languageLevel.level;

    deleteLanguageLevelModal.result
      .then((result) => {
        // console.log(result);
        if (result === true) {
          this.languageLevelService.delete(languageLevel.id).subscribe(
            () => {
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

  importTranslations() {
    const fileSelectorModal = this.modalService.open(FileSelectorComponent, {
      centered: true,
      backdrop: 'static'
    })

    fileSelectorModal.componentInstance.validExtensions = ['csv', 'txt'];
    fileSelectorModal.componentInstance.maxFiles = 1;
    fileSelectorModal.componentInstance.closeButtonLabel = "Import";
    fileSelectorModal.componentInstance.title = "Select file containing " + this.langCode + " translations";
    fileSelectorModal.componentInstance.instructions = "Select a file with one of the above " +
        "extensions where each line is CSV format with two fields, a numeric id followed by a translation.";

    fileSelectorModal.result
    .then((selectedFiles: File[]) => {
      this.doImport(selectedFiles);
    })
    .catch(() => { /* Isn't possible */ });

  }

  private doImport(files: File[]) {
    this.error = null;
    this.loading = true;
    this.languageLevelService.addSystemLanguageTranslations(this.langCode, files[0]).subscribe(
        result => {
          this.loading = false;
          this.search();
        },
        error => {
          this.error = error;
          this.loading = false;
        }
    )
  }

}
