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
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";

@Component({
  selector: 'app-file-upload',
  templateUrl: './file-upload.component.html',
  styleUrls: ['./file-upload.component.scss']
})
export class FileUploadComponent implements OnInit {

  @Input() uploading: boolean;

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
    this.handleFileChanged(fileChangeEvent, 'file');
  }

  @Input() validExtensions: string[] = [
    'jpg',
    'png',
    'pdf',
    'doc',
    'docx',
    'txt',
  ];

  @Output() uploadStarted = new EventEmitter<{files: File[], type: string}>();

  error: any;
  hover: boolean;
  maxUploadSize: number;

  constructor(private modalService: NgbModal) {
  }

  ngOnInit() {
    this.maxUploadSize = 10485790;
  }

  handleFileChanged(event: any, type: string) {
    const files: File[] = [...event.target.files];

    if (!!files && files.length) {

      this.error = null;

      for (const file of files) {
        if (!this.validFile(file)) {
          return;
        }
        if (!this.validFileSize(file)) {
          return;
        }
      }
      this.uploadStarted.emit({files, type});
    }
  }

  validFile(file) {
    if (file.name.indexOf('.') === -1) {
      this.error = 'No file extension found. Please rename and re-upload this file.';
      return false;
    }

    const tokens = file.name.split('.');
    // todo make valid extensions more flexible for other component uses (e.g. CV uploads shouldn't be videos)
    // const validExtensions = [
    //   'jpg',
    //   'png',
    //   'pdf',
    //   'doc',
    //   'docx',
    //   'txt',
    // ];
    // const ext = tokens[tokens.length - 1].toLowerCase();
    // if (!validExtensions.includes(ext)) {
    //   this.error = 'Unsupported file extension. Please upload a file with one of the following extensions: ' + validExtensions.join(', ');
    //   return false;
    // }

    return true;
  }

  validFileSize(file: File): boolean {
    if (file.size > this.maxUploadSize) {
      this.error = "Max file size exceeded, please reduce file size to under 10MB."
      return false;
    } else {
      return true;
    }
  }

}
