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

import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {UntypedFormGroup} from "@angular/forms";
import {RegistrationService} from "../../../services/registration.service";
import { TranslateService } from '@ngx-translate/core';

@Component({
  selector: 'app-registration-upload-file',
  templateUrl: './registration-upload-file.component.html',
  styleUrls: ['./registration-upload-file.component.scss']
})
export class RegistrationUploadFileComponent implements OnInit {

  /* A flag to indicate if the component is being used on the profile component */
  @Input() edit: boolean = false;

  cvWarning!: string;
  @Output() onSave = new EventEmitter();

  form: UntypedFormGroup;
  error: any;
  // Component states
  saving: boolean;
  activeIndexes: number | null;


  constructor(public registrationService: RegistrationService, private translateService: TranslateService) {
  }

  ngOnInit() {
    // Make the accordions closed if editing (to allow better view of footer navigation)
    // For tc-accordion, activeIndexes uses 0-based index: 0 = first panel, null = all closed
    if (!this.edit) {
      this.activeIndexes = 0; // 'upload-cv' corresponds to the first panel (index 0)
    } else {
      this.activeIndexes = null; // '' corresponds to all panels closed
    }

    this.translateService.get('REGISTRATION.ATTACHMENTS.CV.WARNING').subscribe((translated: string) => {
      this.cvWarning = translated;
    });
  }

  // Methods during registration process.
  next() {
    this.registrationService.next();
  }
  back() {
    this.registrationService.back();
  }

  // Methods during edit process.
  update() {
    this.onSave.emit();
  }
  cancel() {
    this.onSave.emit();
  }


}
