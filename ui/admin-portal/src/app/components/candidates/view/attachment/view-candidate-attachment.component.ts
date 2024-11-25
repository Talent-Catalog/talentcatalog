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

import {Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges} from '@angular/core';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {Candidate} from '../../../../model/candidate';
import {UntypedFormBuilder, UntypedFormGroup} from '@angular/forms';
import {AttachmentType, CandidateAttachment} from '../../../../model/candidate-attachment';
import {CandidateAttachmentService} from '../../../../services/candidate-attachment.service';
import {environment} from '../../../../../environments/environment';
import {CreateCandidateAttachmentComponent} from './create/create-candidate-attachment.component';
import {ConfirmationComponent} from '../../../util/confirm/confirmation.component';
import {EditCandidateAttachmentComponent} from './edit/edit-candidate-attachment.component';

@Component({
  selector: 'app-view-candidate-attachment',
  templateUrl: './view-candidate-attachment.component.html',
  styleUrls: ['./view-candidate-attachment.component.scss']
})
export class ViewCandidateAttachmentComponent implements OnInit, OnChanges {

  @Input() candidate: Candidate;
  @Input() editable: boolean;
  @Output() candidateChanged = new EventEmitter();

  loading: boolean;
  error: any;
  s3BucketUrl = environment.s3BucketUrl;

  attachmentForm: UntypedFormGroup;
  shareableForm: UntypedFormGroup;
  expanded: boolean;
  attachments: CandidateAttachment[];
  cvs: CandidateAttachment[];
  other: CandidateAttachment[];
  hasMore: boolean;

  constructor(private candidateAttachmentService: CandidateAttachmentService,
              private modalService: NgbModal,
              private fb: UntypedFormBuilder) {
  }

  get AttachmentType() {
    return AttachmentType;
  }

  ngOnInit() {  }

  ngOnChanges(changes: SimpleChanges) {
    this.expanded = false;
    this.attachments = [];

    this.attachmentForm = this.fb.group({
      candidateId: [this.candidate.id],
      pageSize: 10,
      pageNumber: 0,
      sortDirection: 'DESC',
      sortFields: [['createdDate']]
    });

    // Only do paged search of attachments if on candidate profile (editable = true).
    // On the search card we display all attachments no paging.
    if (this.editable) {
      this.doPagedSearch(true);
    } else {
      this.attachments = this.candidate.candidateAttachments;
    }
  }

  doPagedSearch(refresh: boolean) {
    this.loading = true;
    refresh ? this.attachmentForm.controls['pageNumber'].patchValue(0) : null;
    this.candidateAttachmentService.searchPaged(this.attachmentForm.value).subscribe(
      results => {
        if (refresh) {
          this.attachments = results.content;
        } else {
          this.attachments.push(...results.content);
        }

        this.hasMore = results.totalPages > results.number + 1;
        this.loading = false;
      },
      error => {
        this.error = error;
        this.loading = false;
      })
    ;
  }

  loadMore() {
    this.attachmentForm.controls['pageNumber'].patchValue(this.attachmentForm.value.pageNumber + 1);
    this.doPagedSearch(false);
  }

  editCandidateAttachment(candidateAttachment: CandidateAttachment) {
      const editCandidateAttachmentModal = this.modalService.open(EditCandidateAttachmentComponent, {
      centered: true,
      backdrop: 'static'
    });

    editCandidateAttachmentModal.componentInstance.attachment = candidateAttachment;

    editCandidateAttachmentModal.result
      .then((updated) => {
        const index = this.attachments.findIndex(attachment => attachment.id === updated.id);
        if (index >= 0) {
          /* DEBUG */
          // console.log('index', index);
          this.attachments[index] = updated;
        } else {
          /* DEBUG */
          // console.log('updated', updated);
          this.doPagedSearch(true); // Shouldn't be necessary, but is here as a fail-safe
        }
      })
      .catch(() => { /* Isn't possible */
      });
  }

  addAttachment(type: string){
    const createCandidateAttachmentModal = this.modalService.open(CreateCandidateAttachmentComponent, {
      centered: true,
      backdrop: 'static'
    });

    createCandidateAttachmentModal.componentInstance.candidateId = this.candidate.id;
    createCandidateAttachmentModal.componentInstance.type = type || 'link';

    createCandidateAttachmentModal.result
      .then(() => {
        this.doPagedSearch(true);
        //Adding attachment should add a folder link if there was not one
        //there before. So emit a candidateChanged event.
        this.candidateChanged.emit();
      })
      .catch(() => { /* Isn't possible */ });
  }

  deleteCandidateAttachment(attachment: CandidateAttachment) {
    const deleteCountryModal = this.modalService.open(ConfirmationComponent, {
      centered: true,
      backdrop: 'static'
    });

    deleteCountryModal.componentInstance.message = 'Are you sure you want to delete ' + attachment.name + '?';

    deleteCountryModal.result
      .then((result) => {
        if (result === true) {
          this.candidateAttachmentService.deleteAttachment(attachment.id).subscribe(
            () => {
              this.doPagedSearch(true);
            },
            (error) => {
              this.error = error;
            });
        }
      })
      .catch(() => { /* Isn't possible */ });
  }

  downloadCandidateAttachment(attachment: CandidateAttachment) {
    this.error = null;
    this.loading = true;
    this.candidateAttachmentService.downloadAttachment(attachment.id, attachment.name).subscribe(
      () => {
        this.loading = false;
      },
      (error) => {
        this.error = error;
        this.loading = false;
      });
  }
}
