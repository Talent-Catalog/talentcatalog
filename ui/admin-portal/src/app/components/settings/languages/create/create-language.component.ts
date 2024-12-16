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
import {UntypedFormBuilder, UntypedFormGroup, Validators} from '@angular/forms';
import {Language, SystemLanguage} from "../../../../model/language";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {LanguageService} from "../../../../services/language.service";

@Component({
  selector: 'app-create-language',
  templateUrl: './create-language.component.html',
  styleUrls: ['./create-language.component.scss']
})

export class CreateLanguageComponent implements OnInit {

  languageForm: UntypedFormGroup;
  error;
  saving: boolean;

  constructor(private activeModal: NgbActiveModal,
              private fb: UntypedFormBuilder,
              private languageService: LanguageService) {
  }

  ngOnInit() {
    this.languageForm = this.fb.group({
      langCode: [null, Validators.required],
    });
  }

  onSave() {
    this.saving = true;
    this.languageService.addSystemLanguage(this.languageForm.value.langCode).subscribe(
      (language) => {
        this.closeModal(language)
        this.saving = false;
      },
      (error) => {
        this.error = error;
        this.saving = false;
      });
  }

  closeModal(language: SystemLanguage) {
    this.activeModal.close(language);
  }

  dismiss() {
    this.activeModal.dismiss(false);
  }
}
