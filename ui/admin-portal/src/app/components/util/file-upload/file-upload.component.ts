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

import {Component, EventEmitter, HostListener, Input, OnInit, Output} from '@angular/core';
import {CandidateAttachmentService} from "../../../services/candidate-attachment.service";

@Component({
  selector: 'app-file-upload',
  templateUrl: './file-upload.component.html',
  styleUrls: ['./file-upload.component.scss']
})
export class FileUploadComponent implements OnInit {

  @HostListener('dragover', ['$event'])
  public onDragOver(evt) {
    evt.preventDefault();
    evt.stopPropagation();
    this.hover = true;
  }

  @HostListener('dragleave', ['$event'])
  public onDragLeave(evt) {
    evt.preventDefault();
    evt.stopPropagation();
    this.hover = false;
  }

  @HostListener('drop', ['$event'])
  public onDrop(evt) {
    evt.preventDefault();
    evt.stopPropagation();
    this.hover = false;
    const fileChangeEvent = {target: {files: evt.dataTransfer.files}};
    this.handleFileChanged(fileChangeEvent);
  }

  @Input() validExtensions: string[] = [
    'jpg',
    'png',
    'pdf',
    'doc',
    'docx',
    'txt',
  ];

  @Output() error = new EventEmitter();
  @Output() newFiles = new EventEmitter();

  hover: boolean;

  constructor(private candidateAttachmentService: CandidateAttachmentService) {
  }

  ngOnInit() {
  }

  handleFileChanged(event: any) {
    const files: File[] = [...event.target.files];

    if (!!files && files.length) {

      for (const file of files) {
        if (!this.validFile(file)) {
          return;
        }
      }
      this.newFiles.emit(files);
    }
  }

  validFile(file: File) {
    if (file.name.indexOf('.') === -1) {
      this.error.emit('No file extension found. Please rename and re-select this file.');
      return false;
    }

    const tokens = file.name.split('.');
    const ext = tokens[tokens.length - 1].toLowerCase();
    if (!this.validExtensions.includes(ext)) {
      this.error.emit(
        'Unsupported file extension. Please select a file with one of the following extensions: '
        + this.validExtensions.join(', '));
      return false;
    }

    if (file.size > this.candidateAttachmentService.getMaxUploadFileSize()) {
      this.error.emit('File is too big. Size is ' + file.size +
        '. Max size is ' + this.candidateAttachmentService.getMaxUploadFileSize());
      return false;
    }

    return true;
  }
}
