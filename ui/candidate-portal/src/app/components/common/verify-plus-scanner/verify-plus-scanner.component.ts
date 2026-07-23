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

import {Component, ElementRef, EventEmitter, Input, OnDestroy, Output, ViewChild} from '@angular/core';
import {from, interval, of, Subscription} from 'rxjs';
import {exhaustMap, filter, take, tap} from 'rxjs/operators';
import {VerifyPlusDecoderService} from '../../../services/verify-plus-decoder.service';

/**
 * Component for scanning QR codes using the device's camera.
 *
 * This component captures high-resolution camera frames and decodes QR codes with zxing-wasm.
 * It emits events when a QR code is successfully scanned or when there is a scanner/camera error.
 *
 * @author sadatmalik
 */
@Component({
  selector: 'app-verify-plus-scanner',
  templateUrl: './verify-plus-scanner.component.html',
  styleUrl: './verify-plus-scanner.component.scss'
})
export class VerifyPlusScannerComponent implements OnDestroy {
  @Input() hideScanAgain = false;
  @Output() scanned = new EventEmitter<string>();
  @Output() scannerError = new EventEmitter<unknown>();
  @ViewChild('cameraVideo') cameraVideo?: ElementRef<HTMLVideoElement>;
  @ViewChild('captureCanvas') captureCanvas?: ElementRef<HTMLCanvasElement>;

  scanning = false;
  hasScanned = false;
  hasDevices = true;
  cameraPermission: boolean | null = null;
  invalidScan = false;
  private stream: MediaStream | null = null;
  private decodeSub: Subscription | null = null;

  constructor(private verifyPlusDecoderService: VerifyPlusDecoderService) {
  }

  startScanning() {
    this.invalidScan = false;
    this.scanning = true;
    this.cameraPermission = null;
    void this.startCameraAndDecode();
  }

  ngOnDestroy(): void {
    this.stopScanner();
  }

  private async startCameraAndDecode(): Promise<void> {
    this.stopDecodeLoop();
    this.stopCameraStream();

    try {
      await this.updateDeviceAvailability();

      if (!this.hasDevices) {
        this.scanning = false;
        return;
      }

      // MediaDevices camera APIs are native Promise-based browser calls,
      // so async/await is the most direct fit here.
      const stream = await navigator.mediaDevices.getUserMedia({
        video: {
          facingMode: {ideal: 'environment'},
          width: {ideal: 1920},
          height: {ideal: 1080}
        },
        audio: false
      });

      this.stream = stream;
      this.cameraPermission = true;
      this.scanning = true;

      const video = this.cameraVideo?.nativeElement;
      if (!video) {
        throw new Error('Camera preview element is not available.');
      }

      video.srcObject = stream;
      // HTMLMediaElement.play() is also Promise-based and may reject on browser policies.
      await video.play();
      this.startDecodeLoop();
    } catch (error) {
      this.handleCameraError(error);
    }
  }

  private async updateDeviceAvailability(): Promise<void> {
    if (!navigator.mediaDevices?.enumerateDevices) {
      return;
    }

    // Device enumeration comes from the same Promise-based MediaDevices API surface.
    const devices = await navigator.mediaDevices.enumerateDevices();
    this.hasDevices = devices.some(device => device.kind === 'videoinput');
  }

  private startDecodeLoop(): void {
    this.stopDecodeLoop();

    this.decodeSub = interval(250).pipe(
      exhaustMap(() => {
        const frame = this.captureFrame();
        return frame ? from(this.verifyPlusDecoderService.decode(frame)) : of(null);
      }),
      tap(decodedValue => {
        if (!decodedValue) {
          this.invalidScan = true;
        }
      }),
      filter((decodedValue): decodedValue is string => Boolean(decodedValue)),
      take(1)
    ).subscribe({
      next: decodedValue => this.onDecodeSuccess(decodedValue),
      error: (error: unknown) => {
        this.stopScanner();
        this.onScanError(error);
      }
    });
  }

  private captureFrame(): ImageData | null {
    const video = this.cameraVideo?.nativeElement;
    const canvas = this.captureCanvas?.nativeElement;
    if (!video || !canvas || video.videoWidth === 0 || video.videoHeight === 0) {
      return null;
    }

    const context = canvas.getContext('2d', {willReadFrequently: true});
    if (!context) {
      return null;
    }

    canvas.width = video.videoWidth;
    canvas.height = video.videoHeight;
    context.drawImage(video, 0, 0, canvas.width, canvas.height);
    return context.getImageData(0, 0, canvas.width, canvas.height);
  }

  private onDecodeSuccess(decodedValue: string): void {
    if (!this.scanning) {
      return;
    }

    this.invalidScan = false;
    this.hasScanned = true;
    this.stopScanner();
    this.scanned.emit(decodedValue);
  }

  private handleCameraError(error: unknown): void {
    this.stopScanner();

    if (this.isDomException(error, 'NotAllowedError')) {
      this.cameraPermission = false;
      return;
    }

    if (this.isDomException(error, 'NotFoundError')) {
      this.hasDevices = false;
      return;
    }

    this.onScanError(error);
  }

  private isDomException(error: unknown, name: string): boolean {
    return error instanceof DOMException && error.name === name;
  }

  private stopScanner(): void {
    this.stopDecodeLoop();
    this.stopCameraStream();
    this.scanning = false;
  }

  private stopDecodeLoop(): void {
    this.decodeSub?.unsubscribe();
    this.decodeSub = null;
  }

  private stopCameraStream(): void {
    if (this.stream) {
      this.stream.getTracks().forEach(track => track.stop());
      this.stream = null;
    }

    const video = this.cameraVideo?.nativeElement;
    if (video) {
      video.pause();
      video.srcObject = null;
    }
  }

  private onScanError(error: unknown): void {
    this.scannerError.emit(error);
  }

}
