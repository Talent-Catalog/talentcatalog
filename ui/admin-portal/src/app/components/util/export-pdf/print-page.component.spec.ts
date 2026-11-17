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


import {PrintPageComponent} from "./print-page.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {NO_ERRORS_SCHEMA} from "@angular/core";
import {MockCandidate} from "../../../MockData/MockCandidate";
import {expect, vi} from 'vitest';

describe('PrintPageComponent', () => {
    let component: PrintPageComponent;
    let fixture: ComponentFixture<PrintPageComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [PrintPageComponent],
            schemas: [NO_ERRORS_SCHEMA]
        })
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(PrintPageComponent);
        component = fixture.componentInstance;
        component.candidate = new MockCandidate();
        component.idToExport = 'testDiv';

        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('printPage should call window.print', () => {
        vi.spyOn(window, 'print').mockReturnValue(undefined);

        component.printPage();

        expect(window.print).toHaveBeenCalled();
    });


    async function waitForExportToFinish(timeoutMs: number = 10000): Promise<void> {
        const startedAt = Date.now();

        while (component.saving) {
            if (Date.now() - startedAt > timeoutMs) {
                expect.fail('PDF export did not finish within the timeout');
                return;
            }

            await new Promise<void>(resolve => {
                setTimeout(resolve, 50);
            });
        }
    }

    afterEach(() => {
        document.getElementById('portraitDiv')?.remove();
        document.getElementById('landscapeDiv')?.remove();
    });

    it('should run ngOnInit', () => {
        expect(() => component.ngOnInit()).not.toThrow();
    });

    it('should export a portrait PDF', async () => {
        const element = document.createElement('div');

        element.id = 'portraitDiv';
        element.textContent = 'Portrait PDF content';
        element.style.cssText = `
      position: absolute;
      left: 0;
      top: 0;
      width: 100px;
      height: 200px;
      display: block;
      background: white;
      color: black;
    `;

        document.body.appendChild(element);

        component.candidate = {
            user: {
                firstName: 'Test',
                lastName: 'Candidate'
            }
        } as any;

        component.exportAsPdf('portraitDiv');

        expect(component.saving).toBe(true);

        await waitForExportToFinish();

        expect(component.saving).toBe(false);
    });

    it('should export a landscape PDF', async () => {
        const element = document.createElement('div');

        element.id = 'landscapeDiv';
        element.textContent = 'Landscape PDF content';
        element.style.cssText = `
      position: absolute;
      left: 0;
      top: 0;
      width: 200px;
      height: 100px;
      display: block;
      background: white;
      color: black;
    `;

        document.body.appendChild(element);

        component.candidate = {
            user: {
                firstName: 'Test',
                lastName: 'Candidate'
            }
        } as any;

        component.exportAsPdf('landscapeDiv');

        expect(component.saving).toBe(true);

        await waitForExportToFinish();

        expect(component.saving).toBe(false);
    });

});


// ADMIN_PDF_TEST_MOCKS_20261117
// jsdom has no real canvas implementation. Test the component's portrait/landscape
// branches, loading flag and download request without relying on native rendering.
vi.mock('html2canvas', () => ({
  default: vi.fn((element: HTMLElement) => Promise.resolve({
    width: Number.parseInt(element?.style?.width || '', 10) || 100,
    height: Number.parseInt(element?.style?.height || '', 10) || 100,
    toDataURL: () => 'data:image/jpeg;base64,/9j/4AAQSkZJRgABAQ=='
  }))
}));
vi.mock('jspdf', () => ({
  default: class AdminTestJsPdf {
    addImage(_data: string, _x: number, _y: number, _w: number, _h: number): void {}
    save(_filename: string): void {}
  }
}));
