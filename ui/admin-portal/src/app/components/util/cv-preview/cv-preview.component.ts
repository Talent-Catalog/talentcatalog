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

import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {AttachmentType, CandidateAttachment} from "../../../model/candidate-attachment";
import {Candidate} from "../../../model/candidate";

@Component({
  selector: 'app-cv-preview',
  templateUrl: './cv-preview.component.html',
  styleUrls: ['./cv-preview.component.scss']
})
export class CvPreviewComponent implements OnInit, OnChanges {
  @Input() candidate: Candidate;
  cvUrl: string;
  loading: boolean;

  constructor() { }

  ngOnInit(): void {
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.candidate?.previousValue !== changes.candidate?.currentValue) {
      this.loading = true;
      this.updateCvUrlForPreview();
    }
  }

  /**
   * Return the CV we want to display in the iframe - need to prioritise the listShareableCv &&
   * make sure it is a file that can be displayed and not automatically downloaded (see canPreviewCV method)
   */
  updateCvUrlForPreview() {
    if (this.candidate?.listShareableCv && this.canPreviewCv(this.candidate?.listShareableCv)) {
      this.processUrlForIframe(this.candidate.listShareableCv);
    } else if (this.candidate?.shareableCv && this.canPreviewCv(this.candidate?.shareableCv)) {
      this.processUrlForIframe(this.candidate.shareableCv);
    } else {
      this.cvUrl = null;
      this.loading = false;
    }
  }

  /**
   * If it is a Google file, we need to alter the link by replacing anything after the file id in the link with /preview.
   * This is so it will work in the iframe.
   * @param cv
   */
  processUrlForIframe(cv: CandidateAttachment) {
    if (cv?.type == 'googlefile') {
      this.cvUrl = cv?.url.substring(0, cv?.url.lastIndexOf('/')) + '/preview'
    } else {
      this.cvUrl = cv?.url;
    }
    this.loading = false;
  }

  /**
   * If the CV is hosted on Amazon s3 bucket (older file uploads only) and the file is a doc/docx it
   * automatically is downloaded and not displayed in the iframe. We want to skip previewing these CVs
   * so need to filter these out.
   * @param cv
   */
  canPreviewCv(cv: CandidateAttachment): boolean {
    if(cv.type == AttachmentType.file) {
      return cv.fileType != ('doc' && 'docx');
    } else {
      return true;
    }
  }

}
