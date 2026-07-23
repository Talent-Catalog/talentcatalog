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

import {ComponentFixture, TestBed, fakeAsync, tick, discardPeriodicTasks} from '@angular/core/testing';
import {NO_ERRORS_SCHEMA} from '@angular/core';

import {VerifyPlusScannerComponent} from './verify-plus-scanner.component';
import {VerifyPlusDecoderService} from '../../../services/verify-plus-decoder.service';

describe('VerifyPlusScannerComponent', () => {
  let component: VerifyPlusScannerComponent;
  let fixture: ComponentFixture<VerifyPlusScannerComponent>;
  let decoderService: jasmine.SpyObj<VerifyPlusDecoderService>;
  let getUserMediaSpy: jasmine.Spy;
  let enumerateDevicesSpy: jasmine.Spy;
  let stopTrackSpy: jasmine.Spy;

  const mediaTrack = {stop: () => undefined} as MediaStreamTrack;
  const mediaStream = {getTracks: () => [mediaTrack]} as MediaStream;

  beforeEach(() => {
    jasmine.getEnv().allowRespy(true);
    decoderService = jasmine.createSpyObj<VerifyPlusDecoderService>('VerifyPlusDecoderService', ['decode']);
    stopTrackSpy = spyOn(mediaTrack, 'stop');
    getUserMediaSpy = jasmine.createSpy('getUserMedia').and.resolveTo(mediaStream);
    enumerateDevicesSpy = jasmine.createSpy('enumerateDevices').and.resolveTo([
      {kind: 'videoinput'} as MediaDeviceInfo
    ]);
    spyOn(HTMLMediaElement.prototype, 'play').and.resolveTo();
    spyOn(HTMLMediaElement.prototype, 'pause');

    Object.defineProperty(navigator, 'mediaDevices', {
      value: {
        getUserMedia: getUserMediaSpy,
        enumerateDevices: enumerateDevicesSpy
      },
      configurable: true
    });

    TestBed.configureTestingModule({
      declarations: [VerifyPlusScannerComponent],
      providers: [
        {provide: VerifyPlusDecoderService, useValue: decoderService}
      ],
      schemas: [NO_ERRORS_SCHEMA]
    });

    fixture = TestBed.createComponent(VerifyPlusScannerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should emit scanned payload on successful decode and stop stream', fakeAsync(() => {
    spyOn(component.scanned, 'emit');
    decoderService.decode.and.resolveTo('payload-value');

    component.startScanning();
    fixture.detectChanges();
    tick();
    fixture.detectChanges();

    const video = fixture.nativeElement.querySelector('video') as HTMLVideoElement;
    expect(video).toBeTruthy();
    Object.defineProperty(video, 'videoWidth', {value: 1200, configurable: true});
    Object.defineProperty(video, 'videoHeight', {value: 900, configurable: true});

    tick(260);
    expect(component.scanned.emit).toHaveBeenCalledWith('payload-value');
    expect(component.scanning).toBeFalse();
    expect(stopTrackSpy).toHaveBeenCalled();
    discardPeriodicTasks();
  }));

  it('should mark invalid scan when decode misses', fakeAsync(() => {
    decoderService.decode.and.resolveTo(null);

    component.startScanning();
    fixture.detectChanges();
    tick();
    fixture.detectChanges();

    const video = fixture.nativeElement.querySelector('video') as HTMLVideoElement;
    Object.defineProperty(video, 'videoWidth', {value: 800, configurable: true});
    Object.defineProperty(video, 'videoHeight', {value: 600, configurable: true});

    tick(260);
    expect(component.invalidScan).toBeFalse();
    tick(260);
    expect(component.invalidScan).toBeTrue();
    component.ngOnDestroy();
    discardPeriodicTasks();
  }));

  it('should set hasDevices false when no camera devices are available', fakeAsync(() => {
    enumerateDevicesSpy.and.resolveTo([]);
    component.startScanning();
    tick();
    expect(component.hasDevices).toBeFalse();
    expect(component.scanning).toBeFalse();
  }));

  it('should stop scanning when permission is denied', fakeAsync(() => {
    getUserMediaSpy.and.rejectWith(new DOMException('denied', 'NotAllowedError'));
    component.startScanning();
    tick();
    expect(component.cameraPermission).toBeFalse();
    expect(component.scanning).toBeFalse();
  }));

  it('should emit scanner errors from decoder failures', fakeAsync(() => {
    spyOn(component.scannerError, 'emit');
    decoderService.decode.and.rejectWith(new Error('decoder-failed'));

    component.startScanning();
    fixture.detectChanges();
    tick();
    fixture.detectChanges();

    const video = fixture.nativeElement.querySelector('video') as HTMLVideoElement;
    Object.defineProperty(video, 'videoWidth', {value: 640, configurable: true});
    Object.defineProperty(video, 'videoHeight', {value: 480, configurable: true});

    tick(260);
    expect(component.scannerError.emit).toHaveBeenCalled();
    component.ngOnDestroy();
    discardPeriodicTasks();
  }));

  it('should stop camera tracks on destroy', fakeAsync(() => {
    decoderService.decode.and.resolveTo(null);

    component.startScanning();
    fixture.detectChanges();
    tick();
    fixture.detectChanges();

    component.ngOnDestroy();
    expect(stopTrackSpy).toHaveBeenCalled();
  }));
});
