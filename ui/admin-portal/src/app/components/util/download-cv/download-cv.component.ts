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
import {UntypedFormBuilder, UntypedFormGroup} from "@angular/forms";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {finalize} from "rxjs/operators";
import {CandidateService, DownloadCVRequest} from "../../../services/candidate.service";

/**
 * Modal component fills request to open or download a generated candidate CV.
 *
 * <p>
 * Supported output formats:
 * </p>
 * <ul>
 *   <li>PDF download</li>
 *   <li>Word (.docx)</li>
 *   <li>Native Google Doc creation in the candidate's existing Drive folder</li>
 * </ul>
 *
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
  loading = false;
  saving = false;
  form: UntypedFormGroup;
  candidateId: number;

  constructor(private activeModal: NgbActiveModal,
              private candidateService: CandidateService,
              private fb: UntypedFormBuilder) { }

  ngOnInit(): void {
    this.form = this.fb.group({
      name: [false],
      contact: [false],
      format: ['pdf'],
    });
  }

  onSave(): void {
    this.error = null;
    this.loading = true;

    const request = this.buildRequest();
    const format = this.form.value.format;

    if (format === 'google-doc') {
      this.createGoogleDoc(request);
      return;
    }

    this.downloadFile(request, format);
  }

  private buildRequest(): DownloadCVRequest {
    return {
      candidateId: this.candidateId,
      showName: this.form.value.name,
      showContact: this.form.value.contact
    };
  }

  private createGoogleDoc(request: DownloadCVRequest): void {
    this.candidateService.createGoogleDoc(request)
    .pipe(finalize(() => this.loading = false))
    .subscribe(
      result => {
        this.openUrl(result.url);
        this.closeModal();
      },
      error => this.handleError(error)
    );
  }

  private downloadFile(request: DownloadCVRequest, format: string): void {
    const download$ = format === 'docx'
      ? this.candidateService.downloadCvDocx(request)
      : this.candidateService.downloadCv(request);

    download$
    .pipe(finalize(() => this.loading = false))
    .subscribe(
      result => {
        this.openBlobInNewTab(result);
        this.closeModal();
      },
      error => this.handleError(error)
    );
  }

  private openUrl(url: string): void {
    window.open(url, '_blank');
  }

  private openBlobInNewTab(blob: Blob): void {
    const objectUrl = URL.createObjectURL(blob);
    const tab = window.open('', '_blank');

    if (tab) {
      tab.location.href = objectUrl;
    }
  }

  private handleError(error: any): void {
    this.error = error;
  }

  closeModal(): void {
    this.activeModal.close();
  }

  dismiss(): void {
    this.activeModal.dismiss(false);
  }
}
