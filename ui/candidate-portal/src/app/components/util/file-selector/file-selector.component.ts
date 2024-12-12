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
  selector: 'app-file-selector',
  templateUrl: './file-selector.component.html',
  styleUrls: ['./file-selector.component.scss']
})
export class FileSelectorComponent implements OnInit {

  closeButtonLabel: string = "Close";
  error: string;
  instructions: string;
  maxFiles: number = 0;
  title: string = "Select files"
  validExtensions: string[] = [
    'jpg',
    'png',
    'pdf',
    'doc',
    'docx',
    'txt',
  ];

  selectedFiles: File[] = [];

  constructor(private modal: NgbActiveModal) { }

  ngOnInit(): void {
  }

  addFiles($event) {
    this.error = null;
    if (this.maxFiles > 0 && $event.files.length > this.maxFiles) {
      this.error = "Only " + this.maxFiles + " file(s) can be selected."
    } else {
      this.selectedFiles = $event.files;
    }
  }

  cancel() {
    this.modal.close();
  }

  close() {
    this.modal.close(this.selectedFiles);
  }

  isValid() {
    return this.selectedFiles.length === 1;
  }

  onError(error: string) {
    this.error = error;
  }
}
