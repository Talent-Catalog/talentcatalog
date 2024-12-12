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
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {Candidate} from '../../../../model/candidate';
import {UntypedFormBuilder} from '@angular/forms';
import {AttachmentType, CandidateAttachment} from '../../../../model/candidate-attachment';
import {CandidateAttachmentService} from '../../../../services/candidate-attachment.service';
import {CreateCandidateAttachmentComponent} from './create/create-candidate-attachment.component';
import {ConfirmationComponent} from '../../../util/confirm/confirmation.component';
import {EditCandidateAttachmentComponent} from './edit/edit-candidate-attachment.component';
import {CandidateService} from "../../../../services/candidate.service";

@Component({
  selector: 'app-view-candidate-attachment',
  templateUrl: './view-candidate-attachment.component.html',
  styleUrls: ['./view-candidate-attachment.component.scss']
})
export class ViewCandidateAttachmentComponent implements OnInit {

  @Input() candidate: Candidate;
  @Input() editable: boolean;

  loading: boolean;
  error: any;

  constructor(private candidateAttachmentService: CandidateAttachmentService,
              private candidateService: CandidateService,
              private modalService: NgbModal,
              private fb: UntypedFormBuilder) {
  }

  get AttachmentType() {
    return AttachmentType;
  }

  ngOnInit() {  }

  editCandidateAttachment(candidateAttachment: CandidateAttachment) {
      const editCandidateAttachmentModal = this.modalService.open(EditCandidateAttachmentComponent, {
      centered: true,
      backdrop: 'static'
    });

    editCandidateAttachmentModal.componentInstance.attachment = candidateAttachment;

    editCandidateAttachmentModal.result
      .then((updated) => {
        this.candidateService.updateCandidate();
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
        this.candidateService.updateCandidate();
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
              this.candidateService.updateCandidate();
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
