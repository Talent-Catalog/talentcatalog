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

import {NO_ERRORS_SCHEMA} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import html2canvas from 'html2canvas';
import jsPDF from 'jspdf';
import type {Mock} from 'vitest';
import {expect, vi} from 'vitest';
import {MockCandidate} from '../../../MockData/MockCandidate';
import {PrintPageComponent} from './print-page.component';

// These external package mocks require the packages to be externalized from
// the Angular *testing* build, configured in angular.json below.
vi.mock('html2canvas', () => ({
  default: vi.fn((element: HTMLElement) => Promise.resolve({
    width: Number.parseInt(element.style.width, 10) || 100,
    height: Number.parseInt(element.style.height, 10) || 100,
    toDataURL: vi.fn(() => 'data:image/jpeg;base64,/9j/4AAQSkZJRgABAQ==')
  }))
}));

vi.mock('jspdf', () => ({
  default: vi.fn().mockImplementation(function MockJsPdf() {
    return {
      addImage: vi.fn(),
      save: vi.fn()
    };
  })
}));

describe('PrintPageComponent', () => {
  let component: PrintPageComponent;
  let fixture: ComponentFixture<PrintPageComponent>;

  beforeEach(async () => {
    vi.clearAllMocks();
    await TestBed.configureTestingModule({
      declarations: [PrintPageComponent],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();
    fixture = TestBed.createComponent(PrintPageComponent);
    component = fixture.componentInstance;
    component.candidate = new MockCandidate();
    component.idToExport = 'testDiv';
    fixture.detectChanges();
  });

  afterEach(() => {
    document.getElementById('portraitDiv')?.remove();
    document.getElementById('landscapeDiv')?.remove();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('printPage should call window.print', () => {
    vi.spyOn(window, 'print').mockImplementation(() => {});
    component.printPage();
    expect(window.print).toHaveBeenCalledTimes(1);
  });

  it('should run ngOnInit', () => {
    expect(() => component.ngOnInit()).not.toThrow();
  });

  // A clear failure here means Angular bundled the real package and ignored
  // vi.mock(). Do not mask that problem with an extended timeout or it.skip().
  async function expectPdfExport(
    id: string, width: number, height: number, orientation: 'p' | 'l'
  ): Promise<void> {
    expect(vi.isMockFunction(html2canvas)).toBe(true);
    expect(vi.isMockFunction(jsPDF)).toBe(true);

    const element = document.createElement('div');
    element.id = id;
    element.style.width = `${width}px`;
    element.style.height = `${height}px`;
    element.textContent = 'PDF test fixture';
    document.body.appendChild(element);
    component.candidate = {
      user: {firstName: 'Test', lastName: 'Candidate'}
    } as any;

    component.exportAsPdf(id);
    expect(component.saving).toBe(true);
    await vi.waitFor(() => expect(component.saving).toBe(false), {timeout: 1500});

    expect(html2canvas).toHaveBeenCalledWith(
      element, expect.objectContaining({scale: 1})
    );
    const pdfConstructor = jsPDF as unknown as Mock;
    expect(pdfConstructor).toHaveBeenCalledTimes(1);
    expect(pdfConstructor.mock.calls[0][0]).toBe(orientation);
    const pdf = pdfConstructor.mock.results[0]?.value as {
      addImage: Mock;
      save: Mock;
    };
    expect(pdf.addImage).toHaveBeenCalledTimes(1);
    expect(pdf.save).toHaveBeenCalledWith(`${id}_Test_Candidate.pdf`);
  }

  it('should export a portrait PDF', async () => {
    await expectPdfExport('portraitDiv', 100, 200, 'p');
  });

  it('should export a landscape PDF', async () => {
    await expectPdfExport('landscapeDiv', 200, 100, 'l');
  });
});
