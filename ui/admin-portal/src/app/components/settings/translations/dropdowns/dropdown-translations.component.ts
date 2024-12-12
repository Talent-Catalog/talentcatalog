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
import {SearchResults} from '../../../../model/search-results';
import {UntypedFormArray, UntypedFormBuilder, UntypedFormGroup, Validators} from "@angular/forms";
import {debounceTime, distinctUntilChanged} from "rxjs/operators";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {LanguageService} from "../../../../services/language.service";
import {CountryService} from "../../../../services/country.service";
import {TranslationService} from "../../../../services/translation.service";
import {TranslatedObject} from '../../../../model/translated-object';
import {SystemLanguage} from '../../../../model/language';
import {User} from "../../../../model/user";

@Component({
  selector: 'app-dropdown-translations',
  templateUrl: './dropdown-translations.component.html',
  styleUrls: ['./dropdown-translations.component.scss']
})
export class DropdownTranslationsComponent implements OnInit {

  @Input() loggedInUser: User;

  searchForm: UntypedFormGroup;
  loading: boolean;
  error: any;
  pageNumber: number;
  pageSize: number;
  results: SearchResults<TranslatedObject>;
  systemLanguages: SystemLanguage[];
  types: SearchResults<any>;

  topLevelForm: UntypedFormGroup;
  translations: UntypedFormArray;


  constructor(private fb: UntypedFormBuilder,
              private modalService: NgbModal,
              private languageService: LanguageService,
              private countryService: CountryService,
              private translationService: TranslationService) {
  }

  ngOnInit() {

    /* SET UP FORM */
    this.searchForm = this.fb.group({
      keyword: [''],
      type: [null, Validators.required],
      language: [null, Validators.required],
    });
    this.pageNumber = 1;
    this.pageSize = 50;

    this.getSystemLanguages();
    this.onChanges();

    this.topLevelForm = this.fb.group({
      translations: this.fb.array([])
    });
  }

  getSystemLanguages() {
    this.loading = true;
    this.languageService.listSystemLanguages().subscribe(
      (response) => {
        this.systemLanguages = response.filter(sl => sl.language !== 'en');
        this.loading = false;
      },
      (error) => {
        console.error(error);
      });
  }

  onChanges(): void {
    /* SEARCH ON CHANGE*/
    this.searchForm.valueChanges
      .pipe(
        debounceTime(400),
        distinctUntilChanged()
      )
      .subscribe(res => {
        //reset page number as changing types
        // this.pageNumber = 1;
        this.search();
      });
    this.search();
  }

  /* SEARCH FORM */
  search() {
    const request = this.searchForm.value;
    request.pageNumber = this.pageNumber - 1;
    request.pageSize = this.pageSize;

    if (this.searchForm.valid) {
      this.loading = true;
      const type = this.searchForm.controls['type'].value;
      const language = this.searchForm.controls['language'].value;
      this.translationService.search(request.type, request).subscribe(results => {
          this.results = results;
          this.loading = false;
          //form for editing
          this.topLevelForm = this.fb.group({
            translations: this.fb.array(
              this.results.content.map(t => this.fb.group({
                translatedId: [t.translatedId],
                objectId: [t.id, [Validators.required]],
                objectType: [type, [Validators.required]],
                language: [language, [Validators.required]],
                value: [t.translatedName, [Validators.required, Validators.minLength(2)]]
              }))
            )
          });
          this.loading = false;
        },
        (error) => {
          this.error = error;
          this.loading = false;
        });

    }


  }

  updateTranslation(index) {
    const translationForm = this.topLevelForm.get(`translations.${index}`) as UntypedFormGroup;
    const request = translationForm.value;
    if (request.translatedId) {
      //update
      this.translationService.update(request.translatedId, request).subscribe(results => {
        this.search();
      })
    } else {
      // create
      this.translationService.create(request).subscribe(results => {
          this.search();
        }
      );
    }


  }


}
