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

import {Component, Input, OnInit} from '@angular/core';
import {
  CandidateAttachmentService,
  UpdateCandidateAttachmentRequest
} from '../../../services/candidate-attachment.service';
import {AttachmentType, CandidateAttachment} from '../../../model/candidate-attachment';
import {UntypedFormBuilder, UntypedFormGroup} from '@angular/forms';
import {environment} from '../../../../environments/environment';
import {CandidateService} from '../../../services/candidate.service';
import {forkJoin, Observable} from 'rxjs';
import {UserService} from '../../../services/user.service';
import {User} from '../../../model/user';

@Component({
  selector: 'app-candidate-attachments',
  templateUrl: './candidate-attachments.component.html',
  styleUrls: ['./candidate-attachments.component.scss']
})
export class CandidateAttachmentsComponent implements OnInit {

  @Input() preview: boolean = false;
  @Input() cv: boolean;

  downloading: boolean;
  error: any;
  _loading = {
    candidate: true,
    attachments: true,
    user: true,
    saving: false
  };
  deleting: boolean;
  uploading: boolean;

  s3BucketUrl: string = environment.s3BucketUrl;
  form: UntypedFormGroup;

  attachments: CandidateAttachment[] = [];
  candidateNumber: string;
  user: User;

  editTarget: CandidateAttachment;

  constructor(private fb: UntypedFormBuilder,
              private candidateService: CandidateService,
              private candidateAttachmentService: CandidateAttachmentService,
              private userService: UserService) { }

  get AttachmentType() {
    return AttachmentType;
  }

  ngOnInit() {
    this._loading.candidate = true;
    this._loading.user = true;
    this.editTarget = null;

    this.candidateService.getCandidatePersonal().subscribe(
      (response) => {
        this.candidateNumber = response.candidateNumber;
        this._loading.candidate = false;
      },
      (error) => {
        this.error = error;
        this._loading.candidate = false;
      });
    this.userService.getMyUser().subscribe(
      (response) => {
        this.user = response;
        this._loading.user = false;
      },
      (error) => {
        this.error = error;
        this._loading.user = false;
      }
    )
    this.refreshAttachments();

  }

  private refreshAttachments() {
    this._loading.attachments = true;
    this.candidateAttachmentService.listCandidateAttachments().subscribe(
      (response) => {
        if (!this.preview){
          this.attachments = response.filter(att => att.cv === this.cv);
        } else {
          this.attachments = response;
        }
        this._loading.attachments = false;
      },
      (error) => {
        this.error = error;
        this._loading.attachments = false;
      });
  }

  get loading() {
    const l = this._loading;
    return l.attachments || l.candidate;
  }

  get saving() {
    const l = this._loading;
    return l.saving;
  }

  getAttachmentUrl(att: CandidateAttachment) {
    if (att.type === AttachmentType.file) {
      return this.s3BucketUrl + '/candidate/' + (att.migrated ? 'migrated' : this.candidateNumber) + '/' + att.location;
    }
    return att.location;
  }

  deleteAttachment(attachment: CandidateAttachment) {
    this.deleting = true;
    this.candidateAttachmentService.deleteAttachment(attachment.id).subscribe(
      () => {
        this.attachments = this.attachments.filter(att => att.name !== attachment.name);
        this.deleting = false;
      },
      (error) => {
        this.error = error;
        this.deleting = false;
      });
  }

  startServerUpload($event) {
    this.error = null;
    this.uploading = true;
    this.attachments = [];

    const uploads: Observable<CandidateAttachment>[] = [];
    for (const file of $event.files) {
      const formData: FormData = new FormData();
      if ($event.type === 'camera') {
        // If a camera upload create new file name
        formData.append('file', file, 'CameraUpload_' + new Date().toLocaleDateString() + '_' + new Date().toLocaleTimeString() + '.jpg');
      } else {
        formData.append('file', file);
      }
      uploads.push(this.candidateAttachmentService
        .uploadAttachment(this.cv, formData));
    }

    forkJoin(...uploads).subscribe(
      () => {
        this.uploading = false;
        this.refreshAttachments();
      },
      error => {
        this.error = error;
        this.uploading = false;
      }
    );

  }

  downloadCandidateAttachment(attachment: CandidateAttachment) {
    this.error = null;
    this.downloading = true;
    this.candidateAttachmentService.downloadAttachment(attachment.id, attachment.name).subscribe(
      () => {
        this.downloading = false;
      },
      (error) => {
        this.error = error;
        this.downloading = false;
      });
  }

  editCandidateAttachment(attachment: CandidateAttachment) {
    if (this.editTarget) {
      this.editTarget = null;
    } else {
      this.editTarget = attachment;
    }
  }

  updateAttachmentName(attachment: CandidateAttachment, i) {
    this._loading.saving = true;
    const request: UpdateCandidateAttachmentRequest = {
      name: attachment.name
    };
    this.candidateAttachmentService.updateAttachment(attachment.id, request).subscribe(
      () => {
        this.attachments[i] = attachment;
        this.editTarget = null;
        this._loading.saving = false;
      }, (error) => {
        this.error = error;
      }
    )
  }
}
