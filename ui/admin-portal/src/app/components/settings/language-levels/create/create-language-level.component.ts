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
import {LanguageLevel} from "../../../../model/language-level";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {LanguageLevelService} from "../../../../services/language-level.service";

@Component({
  selector: 'app-create-language-level',
  templateUrl: './create-language-level.component.html',
  styleUrls: ['./create-language-level.component.scss']
})

export class CreateLanguageLevelComponent implements OnInit {

  languageLevelForm: UntypedFormGroup;
  error;
  saving: boolean;

  constructor(private activeModal: NgbActiveModal,
              private fb: UntypedFormBuilder,
              private languageLevelService: LanguageLevelService) {
  }

  ngOnInit() {
    this.languageLevelForm = this.fb.group({
      level: [null, Validators.required],
      name: [null, Validators.required],
      status: [null, Validators.required],
    });
  }

  onSave() {
    this.saving = true;
    this.languageLevelService.create(this.languageLevelForm.value).subscribe(
      (languageLevel) => {
        this.closeModal(languageLevel)
        this.saving = false;
      },
      (error) => {
        this.error = error;
        this.saving = false;
      });
  }

  closeModal(languageLevel: LanguageLevel) {
    this.activeModal.close(languageLevel);
  }

  dismiss() {
    this.activeModal.dismiss(false);
  }
}
