/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

import {Component, OnInit} from '@angular/core';
import {UntypedFormBuilder, UntypedFormGroup} from "@angular/forms";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {CandidateService, CvFormat, DownloadCVRequest} from "../../../services/candidate.service";

/**
 * Modal component fills request to open/DL CV generated from given candidates profile.
 * Usage examples {@link ShowCandidatesComponent.downloadGeneratedCV},
 * {@link ViewCandidateComponent.downloadGeneratedCV}
 */
@Component({
  selector: 'app-download-cv',
  templateUrl: './download-cv.component.html',
  styleUrls: ['./download-cv.component.scss']
})
export class DownloadCvComponent implements OnInit {
  error = null;
  loading = null;
  saving = null;
  googleDocUrl = null;
  googleDocPopupBlocked = false;
  form: UntypedFormGroup;
  candidateId: number;

  constructor(private activeModal: NgbActiveModal,
              private candidateService: CandidateService,
              private fb: UntypedFormBuilder) { }

  ngOnInit(): void {
    this.form = this.fb.group({
      name: [false],
      contact: [false],
      format: ['PDF'],
    });
  }

  onSave() {
    this.error = null;
    this.saving = true;
    this.googleDocUrl = null;

    const format: CvFormat = this.form.value.format;

    let googleDocWindow: Window | null = null;

    if (format === 'GOOGLE_DOC') {
      googleDocWindow = window.open('', '_blank');

      if (googleDocWindow) {
        googleDocWindow.opener = null;
        googleDocWindow.document.write('Creating Google Doc...');
      } else {
        this.googleDocPopupBlocked = true;
      }
    }

    const request: DownloadCVRequest = {
      candidateId: this.candidateId,
      showName: this.form.value.name,
      showContact: this.form.value.contact,
      format: format
    };

    this.candidateService.downloadCv(request).subscribe(
      result => {
        if (format === 'GOOGLE_DOC') {
          result.text().then(url => {
            this.googleDocUrl = url.trim();
            if (googleDocWindow) {
              googleDocWindow.location.href = this.googleDocUrl;
            }
            this.saving = false;
            this.closeModal();
          }).catch(error => {
            this.saving = false;
            this.error = error;
          });

          return;
        }

        const cvUrl = URL.createObjectURL(result);
        window.open(cvUrl, '_blank', 'noopener');

        this.saving = false;
        this.closeModal();
      },
      error => {
        this.saving = false;
        this.error = error;
      }
    );
  }
  closeModal() {
    this.activeModal.close();
  }

  dismiss() {
    this.activeModal.dismiss(false);
  }
}
