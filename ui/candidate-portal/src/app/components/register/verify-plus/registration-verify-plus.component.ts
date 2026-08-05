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

import {Component, OnInit, ViewChild} from '@angular/core';
import {finalize} from 'rxjs/operators';
import {VerifyPlusScannerComponent} from '../../common/verify-plus-scanner/verify-plus-scanner.component';
import {VerifyPlusScanResult, VerifyPlusService} from '../../../services/verify-plus.service';
import {RegistrationService} from '../../../services/registration.service';
import {AuthenticationService} from '../../../services/authentication.service';

/**
 * Component for the Verify Plus registration step.
 */
@Component({
  selector: 'app-registration-verify-plus',
  templateUrl: './registration-verify-plus.component.html',
  styleUrls: ['./registration-verify-plus.component.scss']
})
export class RegistrationVerifyPlusComponent implements OnInit {
  @ViewChild(VerifyPlusScannerComponent) scanner?: VerifyPlusScannerComponent;

  decodedPayload: string | null = null;
  scannerError: unknown;
  submitting = false;
  submitResult: VerifyPlusScanResult | null = null;
  submitError = false;
  submitErrorMessage: string | null = null;

  constructor(
    private verifyPlusService: VerifyPlusService,
    private authenticationService: AuthenticationService,
    public registrationService: RegistrationService
  ) {
  }

  ngOnInit(): void {
    if (!this.authenticationService.isGrnInstance()) {
      this.registrationService.next();
    }
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

  get nextDisabled(): boolean {
    return this.submitting;
  }

  onScanned(payload: string): void {
    this.decodedPayload = payload;
    this.scannerError = null;
    this.submitResult = null;
    this.submitError = false;
    this.submitErrorMessage = null;
  }

  onScannerError(error: unknown): void {
    this.scannerError = error;
  }

  onConfirm(): void {
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

  onRescan(): void {
    this.decodedPayload = null;
    this.submitResult = null;
    this.submitError = false;
    this.submitErrorMessage = null;
    this.scanner?.startScanning();
  }

  onBack(): void {
    this.registrationService.back();
  }

  onNext(): void {
    this.registrationService.next();
  }
}
