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

import {BarcodeFormat} from '@zxing/library';
import {Component, EventEmitter, Output} from '@angular/core';

/**
 * Component for scanning QR codes using the device's camera.
 *
 * This component uses the ZXing library to scan QR codes. It emits events when a QR code is
 * successfully scanned, when there is an error with the scanner, and when camera permissions are
 * granted or denied.
 *
 * @author sadatmalik
 */
@Component({
  selector: 'app-verify-plus-scanner',
  templateUrl: './verify-plus-scanner.component.html',
  styleUrl: './verify-plus-scanner.component.scss'
})
export class VerifyPlusScannerComponent {
  @Output() scanned = new EventEmitter<string>();
  @Output() scannerError = new EventEmitter<unknown>();

  readonly formats = [BarcodeFormat.QR_CODE];

  scanning = false;
  hasDevices = true;
  cameraPermission: boolean | null = null;
  invalidScan = false;
  selectedDevice: MediaDeviceInfo | undefined;

  startScanning() {
    this.invalidScan = false;
    this.scanning = true;
  }

  onCamerasFound(devices: MediaDeviceInfo[]) {
    this.hasDevices = devices.length > 0;
    this.selectedDevice = this.selectBackCamera(devices);
  }

  onCamerasNotFound() {
    this.hasDevices = false;
  }

  onPermissionResponse(hasPermission: boolean) {
    this.cameraPermission = hasPermission;
    if (!hasPermission) {
      this.scanning = false;
    }
  }

  onScanSuccess(decodedValue: string) {
    if (!decodedValue) {
      this.invalidScan = true;
      return;
    }

    this.invalidScan = false;
    this.scanned.emit(decodedValue);
  }

  onScanFailure() {
    this.invalidScan = true;
  }

  onScanError(error: unknown) {
    this.scannerError.emit(error);
  }

  private selectBackCamera(devices: MediaDeviceInfo[]): MediaDeviceInfo | undefined {
    if (!devices.length) {
      return undefined;
    }

    const backCamera = devices.find(device =>
      /back|rear|environment/i.test(device.label)
    );

    return backCamera ?? devices[0];
  }
}
