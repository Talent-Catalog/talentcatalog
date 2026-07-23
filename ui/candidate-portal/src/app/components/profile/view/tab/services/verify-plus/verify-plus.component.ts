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

import {Component, EventEmitter, Input, Output, ViewChild} from '@angular/core';
import {finalize} from 'rxjs/operators';
import {Candidate} from '../../../../../../model/candidate';
import {VerifyPlusScannerComponent} from '../../../../../common/verify-plus-scanner/verify-plus-scanner.component';
import {VerifyPlusScanResult, VerifyPlusService} from '../../../../../../services/verify-plus.service';

/**
 * Component for verifying a candidate's credentials using a QR code scanner.
 *
 * This component displays a QR code scanner and handles the scanning process. It emits events when
 * a QR code is successfully scanned, when there is an error with the scanner, and when the back
 * button is clicked.
 *
 * @author sadatmalik
 */
@Component({
  selector: 'app-verify-plus',
  templateUrl: './verify-plus.component.html',
  styleUrl: './verify-plus.component.scss'
})
export class VerifyPlusComponent {
  @Input() candidate!: Candidate;
  @Output() backButtonClicked = new EventEmitter<void>();
  @ViewChild(VerifyPlusScannerComponent) scanner?: VerifyPlusScannerComponent;

  decodedPayload: string | null = null;
  scannerError: unknown;
  submitting = false;
  submitResult: VerifyPlusScanResult | null = null;
  submitError = false;
  submitErrorMessage: string | null = null;

  constructor(private verifyPlusService: VerifyPlusService) {
  }

  get formattedPayload(): string {
    if (!this.decodedPayload) {
      return '';
    }

    try {
      return JSON.stringify(JSON.parse(this.decodedPayload), null, 2);
    } catch {
      return this.decodedPayload;
    }
  }

  onScanned(payload: string) {
    this.decodedPayload = payload;
    this.scannerError = null;
    this.submitResult = null;
    this.submitError = false;
    this.submitErrorMessage = null;
  }

  onScannerError(error: unknown) {
    this.scannerError = error;
  }

  onConfirm() {
    if (!this.decodedPayload || this.submitting) {
      return;
    }

    this.submitError = false;
    this.submitErrorMessage = null;
    this.submitting = true;

    this.verifyPlusService.submitScan(this.decodedPayload)
      .pipe(finalize(() => this.submitting = false))
      .subscribe({
        next: (result) => {
          this.submitResult = result;
        },
        error: (message: unknown) => {
          this.submitError = true;
          this.submitErrorMessage = typeof message === 'string' ? message : null;
        }
      });
  }

  onRescan() {
    this.decodedPayload = null;
    this.submitResult = null;
    this.submitError = false;
    this.submitErrorMessage = null;
    this.scanner?.startScanning();
  }

  onBackButtonClicked() {
    this.backButtonClicked.emit();
  }
}
