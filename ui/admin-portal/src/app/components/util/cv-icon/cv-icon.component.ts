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

import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {
  AttachmentType,
  CandidateAttachment,
  SearchCandidateAttachmentsRequest
} from '../../../model/candidate-attachment';
import {environment} from '../../../../environments/environment';
import {CandidateAttachmentService} from '../../../services/candidate-attachment.service';
import {Candidate} from '../../../model/candidate';
import {saveBlob} from "../../../util/file";

@Component({
  selector: 'app-cv-icon',
  templateUrl: './cv-icon.component.html',
  styleUrls: ['./cv-icon.component.scss']
})
export class CvIconComponent implements OnInit {
  // Required Input
  @Input() candidate: Candidate;
  //Optional Input - if a candidate attachment is passed in, this will only
  //open the single attachment.
  @Input() attachment: CandidateAttachment;

  //Used to indicate loading status.
  @Output() loadingStatus = new EventEmitter<boolean>();

  cvs: CandidateAttachment[];
  s3BucketUrl = environment.s3BucketUrl;

  constructor(private candidateAttachmentService: CandidateAttachmentService) { }

  ngOnInit() {
    this.getAttachments();
  }

  getAttachments() {
    this.cvs = [];
    // If there is a single attachment passed down
    if (this.attachment) {
      this.cvs.push(this.attachment)
    } else {
      // Otherwise get all attachments
      const request: SearchCandidateAttachmentsRequest = {
        candidateId: this.candidate.id,
        cvOnly: true
      }
      this.candidateAttachmentService.search(request).subscribe(
        results => {
          this.cvs = results;
        },
        error => {
          console.log(error);
        })
      ;
    }
  }

  getAttachmentUrl(att: CandidateAttachment) {
    if (att.type === AttachmentType.file) {
      return this.s3BucketUrl + '/candidate/' + (att.migrated ? 'migrated' :
        this.candidate.candidateNumber) + '/' + att.location;
    }
    return att.location;
  }

  openCVs() {
    for (let i = 0; i < this.cvs.length; i++) {
      const cv = this.cvs[i];
      if (cv.type === AttachmentType.googlefile) {
        this.downloadCandidateAttachment(cv)
      } else {
        const newTab = window.open();
        const url = this.getAttachmentUrl(cv);
        newTab.location.href = url;
      }
    }
  }

  downloadCandidateAttachment(attachment: CandidateAttachment) {
    this.loadingStatus.emit(true);
    this.candidateAttachmentService.downloadAttachment(attachment.id).subscribe(
      (resp: Blob) => {
        saveBlob(resp, attachment.name);
        this.loadingStatus.emit(false);
      },
      (error) => {
        console.log(error);
        this.loadingStatus.emit(false);
      });
  }

}
