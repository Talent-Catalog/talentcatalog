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

describe('PrintPageComponent', () => {
  let component: PrintPageComponent;
  let fixture: ComponentFixture<PrintPageComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ PrintPageComponent ],
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
    spyOn(window, 'print');

    component.printPage();

    expect(window.print).toHaveBeenCalled();
  });


  async function waitForExportToFinish(
    timeoutMs: number = 10000
  ): Promise<void> {
    const startedAt = Date.now();

    while (component.saving) {
      if (Date.now() - startedAt > timeoutMs) {
        fail('PDF export did not finish within the timeout');
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

    expect(component.saving).toBeTrue();

    await waitForExportToFinish();

    expect(component.saving).toBeFalse();
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

    expect(component.saving).toBeTrue();

    await waitForExportToFinish();

    expect(component.saving).toBeFalse();
  });

});
