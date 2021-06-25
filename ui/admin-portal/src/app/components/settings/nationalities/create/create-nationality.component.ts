/*
 * Copyright (c) 2021 Talent Beyond Boundaries.
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

import {Component} from '@angular/core';

@Component({
  selector: 'app-create-nationality',
  templateUrl: './create-nationality.component.html',
  styleUrls: ['./create-nationality.component.scss']
})

export class CreateNationalityComponent {

  // nationalityForm: FormGroup;
  // error;
  // saving: boolean;
  //
  // constructor(private activeModal: NgbActiveModal,
  //             private fb: FormBuilder,
  //             private nationalityService: NationalityService) {
  // }
  //
  // ngOnInit() {
  //   this.nationalityForm = this.fb.group({
  //     name: [null, Validators.required],
  //     status: [null, Validators.required],
  //   });
  // }
  //
  // onSave() {
  //   this.saving = true;
  //   this.nationalityService.create(this.nationalityForm.value).subscribe(
  //     (nationality) => {
  //       this.closeModal(nationality)
  //       this.saving = false;
  //     },
  //     (error) => {
  //       this.error = error;
  //       this.saving = false;
  //     });
  // }
  //
  // closeModal(nationality: Nationality) {
  //   this.activeModal.close(nationality);
  // }
  //
  // dismiss() {
  //   this.activeModal.dismiss(false);
  // }
}
