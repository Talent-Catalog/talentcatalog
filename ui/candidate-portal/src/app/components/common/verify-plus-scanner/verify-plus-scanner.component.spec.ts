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

import { ComponentFixture, discardPeriodicTasks, fakeAsync, flushMicrotasks, TestBed, tick } from '@angular/core/testing';
import { NO_ERRORS_SCHEMA, Pipe, PipeTransform } from '@angular/core';

import { VerifyPlusScannerComponent } from './verify-plus-scanner.component';
import { VerifyPlusDecoderService } from '../../../services/verify-plus-decoder.service';
import { vi, type Mock } from 'vitest';

describe('VerifyPlusScannerComponent', () => {
    let component: VerifyPlusScannerComponent;
    let fixture: ComponentFixture<VerifyPlusScannerComponent>;
    let decoderService: {
      decode: Mock<VerifyPlusDecoderService['decode']>;
    };
    let getUserMediaSpy: Mock;
    let enumerateDevicesSpy: Mock;
    let stopTrackSpy: Mock;

    const mediaTrack = { stop: () => undefined } as MediaStreamTrack;
    const mediaStream = { getTracks: () => [mediaTrack] } as MediaStream;

    afterEach(() => {
      fixture?.destroy();
      vi.restoreAllMocks();
    });

    beforeEach(() => {
        decoderService = {
          decode: vi.fn<VerifyPlusDecoderService['decode']>()
            .mockName('VerifyPlusDecoderService.decode')
        };
        stopTrackSpy = vi.spyOn(mediaTrack, 'stop').mockReturnValue(undefined);
        getUserMediaSpy = vi.fn().mockName('getUserMedia').mockResolvedValue(mediaStream);
        enumerateDevicesSpy = vi.fn().mockName('enumerateDevices').mockResolvedValue([
            { kind: 'videoinput' } as MediaDeviceInfo
        ]);
        vi.spyOn(HTMLMediaElement.prototype, 'play').mockResolvedValue();
        vi.spyOn(HTMLMediaElement.prototype, 'pause').mockReturnValue(undefined);

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
                { provide: VerifyPlusDecoderService, useValue: decoderService }
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
        vi.spyOn(component.scanned, 'emit').mockReturnValue(undefined);
        // Avoid using the real canvas/video APIs in the unit test.
        vi.spyOn(
          component as unknown as {
            captureFrame(): ImageData | null
          },
          'captureFrame'
        ).mockReturnValue({} as ImageData);

        decoderService.decode.mockResolvedValue('payload-value');

        component.scanning = true;
        (component as any).stream = mediaStream;
        (component as any).startDecodeLoop();

        tick(250);
        flushMicrotasks();

        expect(decoderService.decode).toHaveBeenCalled();
        expect(component.scanned.emit).toHaveBeenCalledWith('payload-value');
        expect(component.scanning).toBe(false);
        expect(stopTrackSpy).toHaveBeenCalled();

        discardPeriodicTasks();
    }));

    it('should mark invalid scan when decode misses', fakeAsync(() => {
        vi.spyOn(
          component as unknown as {
            captureFrame(): ImageData | null;
          },
          'captureFrame'
        ).mockReturnValue({} as ImageData);

        decoderService.decode.mockResolvedValue(null);

        component.scanning = true;
        (component as any).startDecodeLoop();

        expect(component.invalidScan).toBe(false);

        tick(250);
        flushMicrotasks();

        expect(decoderService.decode).toHaveBeenCalled();
        expect(component.invalidScan).toBe(true);

        component.ngOnDestroy();
        discardPeriodicTasks();
    }));

    it('should set hasDevices false when no camera devices are available', fakeAsync(() => {
        enumerateDevicesSpy.mockResolvedValue([]);
        component.startScanning();
        tick();
        expect(component.hasDevices).toBe(false);
        expect(component.scanning).toBe(false);
    }));

    it('should stop scanning when permission is denied', fakeAsync(() => {
        getUserMediaSpy.mockRejectedValue(new DOMException('denied', 'NotAllowedError'));
        component.startScanning();
        tick();
        expect(component.cameraPermission).toBe(false);
        expect(component.scanning).toBe(false);
    }));

   it('should emit scanner errors from decoder failures', fakeAsync(() => {
     const error = new Error('decoder-failed');

     const errorSpy = vi.spyOn(component.scannerError, 'emit');

     // Avoid real canvas APIs, which jsdom does not implement.
     vi.spyOn(
       component as unknown as {
         captureFrame(): ImageData | null;
       },
       'captureFrame'
     ).mockReturnValue({} as ImageData);

     decoderService.decode.mockRejectedValue(error);

     component.startScanning();
     fixture.detectChanges();
     tick();
     fixture.detectChanges();

     tick(260);
     flushMicrotasks();

     expect(decoderService.decode).toHaveBeenCalled();
     expect(errorSpy).toHaveBeenCalledWith(error);
     expect(component.scanning).toBe(false);

     component.ngOnDestroy();
     discardPeriodicTasks();
   }));

    it('should stop camera tracks on destroy', fakeAsync(() => {
        decoderService.decode.mockResolvedValue(null);

        component.startScanning();
        fixture.detectChanges();
        tick();
        fixture.detectChanges();

        component.ngOnDestroy();
        expect(stopTrackSpy).toHaveBeenCalled();
    }));
});
