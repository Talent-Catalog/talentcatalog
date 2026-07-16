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

import {Component, EventEmitter, Input, Output} from '@angular/core';
import {Candidate} from '../../../../../../model/candidate';

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

  decodedPayload: string | null = null;
  scannerError: unknown;

  onScanned(payload: string) {
    this.decodedPayload = payload;
    this.scannerError = null;
  }

  onScannerError(error: unknown) {
    this.scannerError = error;
  }

  onBackButtonClicked() {
    this.backButtonClicked.emit();
  }
}
