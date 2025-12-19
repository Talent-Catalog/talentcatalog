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
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";

@Component({
  selector: 'app-delete',
  templateUrl: './delete-exam.component.html',
  styleUrls: ['./delete-exam.component.scss']
})
export class DeleteExamComponent implements OnInit {

  candidateExamId: number;
  deleting: boolean;

  constructor(private activeModal: NgbActiveModal) { }

  ngOnInit() {
  }

  cancel() {
    this.activeModal.dismiss(false);
  }

  confirm() {
    this.deleting = true;
    this.activeModal.close(true);
  }

}
