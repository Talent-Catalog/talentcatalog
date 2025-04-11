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

import {Component, EventEmitter, Input, Output} from '@angular/core';
import {CandidateCertification} from "../../../model/candidate-certification";

@Component({
  selector: 'app-candidate-certification-card',
  templateUrl: './candidate-certification-card.component.html',
  styleUrls: ['./candidate-certification-card.component.scss']
})
export class CandidateCertificationCardComponent {

  @Input() certificate: CandidateCertification;
  @Input() disabled: boolean = false;
  @Input() preview: boolean = false;

  @Output() onDelete = new EventEmitter<CandidateCertification>();
  @Output() onEdit = new EventEmitter<CandidateCertification>();

  constructor() { }

  deleteCertificate() {
    this.onDelete.emit(this.certificate);
  }

  editCertificate() {
    this.onEdit.emit(this.certificate);
  }
}
