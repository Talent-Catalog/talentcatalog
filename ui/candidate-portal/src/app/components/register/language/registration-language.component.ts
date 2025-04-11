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

import {Component, EventEmitter, Input, OnDestroy, OnInit, Output} from '@angular/core';
import {UntypedFormBuilder, UntypedFormGroup, Validators} from "@angular/forms";
import {Router} from "@angular/router";
import {CandidateLanguage} from "../../../model/candidate-language";
import {CandidateLanguageService} from "../../../services/candidate-language.service";
import {CandidateService} from "../../../services/candidate.service";
import {Language} from "../../../model/language";
import {LanguageService} from "../../../services/language.service";
import {LanguageLevel} from "../../../model/language-level";
import {LanguageLevelService} from "../../../services/language-level.service";
import {RegistrationService} from "../../../services/registration.service";
import {LangChangeEvent, TranslateService} from "@ngx-translate/core";
import {Occupation} from "../../../model/occupation";


@Component({
  selector: 'app-registration-language',
  templateUrl: './registration-language.component.html',
  styleUrls: ['./registration-language.component.scss']
})
export class RegistrationLanguageComponent implements OnInit, OnDestroy {

  /* A flag to indicate if the component is being used on the profile component */
  @Input() edit: boolean = false;

  @Output() onSave = new EventEmitter();

  error: any;
  _loading = {
    candidate: true,
    english: true,
    languages: true,
    languageLevels: true
  };
  saving: boolean;

  addingLanguage: boolean;
  form: UntypedFormGroup;
  candidateLanguages: CandidateLanguage[];
  english: Language;
  languages: Language[];
  languageLevels: LanguageLevel[];

  subscription;

  constructor(private fb: UntypedFormBuilder,
              private router: Router,
              private candidateService: CandidateService,
              private candidateLanguageService: CandidateLanguageService,
              private languageService: LanguageService,
              private languageLevelService: LanguageLevelService,
              private registrationService: RegistrationService,
              private translateService: TranslateService) {
  }

  ngOnInit() {
    this.candidateLanguages = [];
    this.addingLanguage = false;
    this.saving = false;
    this.form = this.fb.group({
      languageId: [null, Validators.required],
      spokenLevelId: [null, Validators.required],
      writtenLevelId: [null, Validators.required]
    });

    this.loadDropDownData();
    //listen for change of language and save
    this.subscription = this.translateService.onLangChange.subscribe((event: LangChangeEvent) => {
      this.loadDropDownData();
    });

  }

  loadDropDownData() {
    this._loading.languages = true;
    this._loading.languageLevels = true;
    this._loading.english = true;

    this.languageService.getLanguage('english').subscribe(result => {
      this.english = result;
      this._loading.english = false;
    }, error => {
      this.error = error;
      this._loading.english = false;
    });

    /* Load the languages */
    this.languageService.listLanguages().subscribe(
      (response) => {
        this.languages = response;
        this._loading.languages = false;
        this.loadCandidateLanguages();
      },
      (error) => {
        this.error = error;
        this._loading.languages = false;
      }
    );

    /* Load the language levels */
    this.languageLevelService.listLanguageLevels().subscribe(
      (response) => {
        this.languageLevels = response;
        this._loading.languageLevels = false;
      },
      (error) => {
        this.error = error;
        this._loading.languageLevels = false;
      }
    );
  }

  patchForm(lang?: CandidateLanguage) {
    this.form.patchValue({
      languageId: lang && lang.language ? lang.language.id : null,
      spokenLevelId: lang && lang.spokenLevel ? lang.spokenLevel.id : null,
      writtenLevelId: lang && lang.writtenLevel ? lang.writtenLevel.id : null
    });
  };

  loadCandidateLanguages() {
    this.candidateService.getCandidateLanguages().subscribe(
      (candidate) => {
        if (candidate.candidateLanguages && candidate.candidateLanguages.length) {
          // Sort the languages so that english is always at the top
          candidate.candidateLanguages
            .sort((a, b) => a.id > b.id ? -1 : 1) // Order by candidateLangauge id
            .sort((a, b) => b.language.name.toLowerCase().trim() == 'english' ? 1 : -1); // Float english to the top

          this.candidateLanguages = candidate.candidateLanguages.map(lang => {
            return {
              id: lang.id,
              language: lang.language,
              spokenLevel: lang.spokenLevel,
              writtenLevel: lang.writtenLevel,
              // Request object variables
              languageId: lang.language ? lang.language.id : null,
              spokenLevelId: lang.spokenLevel ? lang.spokenLevel.id : null,
              writtenLevelId: lang.writtenLevel ? lang.writtenLevel.id : null,
            }
          }) || [];
        } else {
          // Patch the form with the english language id
            this.form.patchValue({languageId: this.english?.id});
            this.addingLanguage = true;

        }
        this._loading.candidate = false;
      },
      (error) => {
        this.error = error;
        this._loading.candidate = false;
      }
    );
  }

  addLanguage() {
    if (this.addingLanguage) {
      this.candidateLanguages.push(this.form.value);
      this.patchForm();
    } else {
      this.addingLanguage = true;
    }
  }

  deleteCandidateLanguage(index: number) {
    this.candidateLanguages.splice(index, 1);
  }

  save(dir: string) {
    this.saving = true;
    if (this.addingLanguage && this.form.valid) {
      this.addLanguage();
    }
    const request = {
      updates: this.candidateLanguages
    };
    this.candidateLanguageService.updateCandidateLanguages(request).subscribe(
      (val) => {
        if (dir === 'next') {
          this.onSave.emit();
          this.registrationService.next();
        } else {
          this.registrationService.back();
        }
        this.saving = false;
      },
      (error) => {
        this.error = error;
        this.saving = false;
      });
  }

  back() {
    this.save('back');
  }

  next() {
    this.save('next');
  }

  get loading() {
    const l = this._loading;
    return l.candidate || l.languages || l.languageLevels;
  }

  get selectedFormLanguage() {
    return this.form.value.languageId;
  }

  /* Takes an optional language ID as param, otherwise assumes it's the forms value */
  getLanguageName(id?: number) {
    id = id || this.form.value.languageId;
    if (id) {
      return this.languages.find(lang => lang.id == id).name;
    }
    return '';
  }

  /* Takes an optional language ID as param, otherwise assumes it's the forms value */
  isEnglish(id?: number) {
    id = id || this.form.value.languageId;
    if (id) {
     return id == this.english.id;
    }
    return false;
  }

  get filteredLanguages(): Occupation[] {
    if (!this.languages) {
      return [];
    } else if (!this.candidateLanguages || !this.languages.length) {
      return this.languages
    } else {
      const existingIds = this.candidateLanguages.map(candidateLang => candidateLang.language
        ? candidateLang.language.id.toString()
        : candidateLang.languageId.toString()
      );
      return this.languages.filter(occ => !existingIds.includes(occ.id.toString()))
    }
  }

  cancel() {
    this.onSave.emit();
  }

  ngOnDestroy() {
    this.subscription.unsubscribe();
  }
}
