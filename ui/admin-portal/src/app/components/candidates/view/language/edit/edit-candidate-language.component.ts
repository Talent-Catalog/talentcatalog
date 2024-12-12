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
import {UntypedFormBuilder, UntypedFormGroup} from '@angular/forms';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {LanguageService} from '../../../../../services/language.service';
import {CandidateLanguage} from '../../../../../model/candidate-language';
import {CountryService} from '../../../../../services/country.service';
import {LanguageLevelService} from '../../../../../services/language-level.service';
import {
  CandidateLanguageService,
  UpdateCandidateLanguageRequest
} from '../../../../../services/candidate-language.service';

@Component({
  selector: 'app-edit-candidate-language',
  templateUrl: './edit-candidate-language.component.html',
  styleUrls: ['./edit-candidate-language.component.scss']
})
export class EditCandidateLanguageComponent implements OnInit {

  candidateLanguage: CandidateLanguage;

  candidateForm: UntypedFormGroup;

  languages = [];
  languageLevels = [];
  years = [];
  error;
  loading: boolean;
  saving: boolean;

  constructor(private activeModal: NgbActiveModal,
              private fb: UntypedFormBuilder,
              private languageService: LanguageService,
              private candidateLanguageService: CandidateLanguageService,
              private countryService: CountryService,
              private languageLevelService: LanguageLevelService) {
  }

  ngOnInit() {
    this.loading = true;

    /* Load the languages */
    this.languageService.listLanguages().subscribe(
      (response) => {
        this.languages = response;
        this.loading = false;
      },
      (error) => {
        this.error = error;
        this.loading = false;
      }
    );

    /* Load the language levels */
    this.languageLevelService.listLanguageLevels().subscribe(
      (response) => {
        this.languageLevels = response;
        this.loading = false;
      },
      (error) => {
        this.error = error;
        this.loading = false;
      }
    );

    this.candidateForm = this.fb.group({
      languageId: [this.candidateLanguage.language.id],
      spokenLevelId: [this.candidateLanguage.spokenLevel.id],
      writtenLevelId: [this.candidateLanguage.writtenLevel.id]
    });
    this.loading = false;
  }

  onSave() {
    this.saving = true;
    const request: UpdateCandidateLanguageRequest = {
      id: this.candidateLanguage.id,
      languageId: this.candidateForm.value.languageId,
      spokenLevelId: this.candidateForm.value.spokenLevelId,
      writtenLevelId: this.candidateForm.value.writtenLevelId,
    }
    this.candidateLanguageService.update(request).subscribe(
      (candidateLanguage) => {
        this.closeModal(candidateLanguage);
        this.saving = false;
      },
      (error) => {
        this.error = error;
        this.saving = false;
      });
  }

  closeModal(candidateLanguage: CandidateLanguage) {
    this.activeModal.close(candidateLanguage);
  }

  dismiss() {
    this.activeModal.dismiss(false);
  }
}
