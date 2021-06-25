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
  selector: 'app-edit-nationality',
  templateUrl: './edit-nationality.component.html',
  styleUrls: ['./edit-nationality.component.scss']
})
export class EditNationalityComponent {

  // nationalityId: number;
  // nationalityForm: FormGroup;
  // error;
  // loading: boolean;
  // saving: boolean;
  //
  // constructor(private activeModal: NgbActiveModal,
  //             private fb: FormBuilder,
  //             private nationalityService: NationalityService) {
  // }
  //
  // ngOnInit() {
  //   this.loading = true;
  //   this.nationalityService.get(this.nationalityId).subscribe(nationality => {
  //     this.nationalityForm = this.fb.group({
  //       name: [nationality.name, Validators.required],
  //       status: [nationality.status, Validators.required],
  //     });
  //     this.loading = false;
  //   });
  // }
  //
  // onSave() {
  //   this.saving = true;
  //   this.nationalityService.update(this.nationalityId, this.nationalityForm.value).subscribe(
  //     (nationality) => {
  //       this.closeModal(nationality);
  //       this.saving = false;
  //     },
  //     (error) => {
  //       this.error = error;
  //       this.saving = false;
  //     });
  // }
  //
  // closeModal(nationality: Country) {
  //   this.activeModal.close(nationality);
  // }
  //
  // dismiss() {
  //   this.activeModal.dismiss(false);
  // }
}
