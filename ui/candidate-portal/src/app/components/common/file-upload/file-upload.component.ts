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

import {Component, EventEmitter, HostListener, Input, OnInit, Output} from '@angular/core';
import {WebcamImage} from "ngx-webcam";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {WebcamUploadComponent} from "../webcam-upload/webcam-upload.component";

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
    this.handleFileChanged(fileChangeEvent);
  }

  @Output() uploadStarted = new EventEmitter();

  error: any;
  hover: boolean;


  public webcamImage: WebcamImage = null;

  constructor(private modalService: NgbModal) {
  }

  ngOnInit() {
  }

  handleImage(webcamImage: WebcamImage) {
    this.webcamImage = webcamImage;
  }

  handleFileChanged(event: any) {
    const files: File[] = [...event.target.files];

    if (!!files && files.length) {

      this.error = null;

      for (const file of files) {
        if (!this.validFile(file)) {
          return;
        }
      }
      this.uploadStarted.emit(files);
    }
  }

  validFile(file) {
    if (file.name.indexOf('.') === -1) {
      this.error = 'No file extension found. Please rename and re-upload this file.';
      return false;
    }

    const tokens = file.name.split('.');
    const validExtensions = [
      'jpg',
      'png',
      'pdf',
      'doc',
      'docx',
      'txt',
    ];
    const ext = tokens[tokens.length - 1].toLowerCase();
    if (!validExtensions.includes(ext)) {
      this.error = 'Unsupported file extension. Please upload a file with one of the following extensions: ' + validExtensions.join(', ');
      return false;
    }

    return true;
  }

  webcamModal() {
    const webcamUploadModal = this.modalService.open(WebcamUploadComponent, {
      centered: true,
      backdrop: 'static'
    });

    webcamUploadModal.result
      .then((result) => {
        // remove occupation from occupations if confirmed modal
        console.log(result);
      })
      .catch(() => { /* Isn't possible */ });
  }

}
