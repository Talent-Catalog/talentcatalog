/*
 * Copyright (c) 2024 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

import {ExportPdfComponent} from "./export-pdf.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {MockCandidate} from "../../../MockData/MockCandidate";

describe('ExportPdfComponent', () => {
  let component: ExportPdfComponent;
  let fixture: ComponentFixture<ExportPdfComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ExportPdfComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ExportPdfComponent);
    component = fixture.componentInstance;
    component.candidate = new MockCandidate();
    component.idToExport = 'testDiv';

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should set saving to true when exportAsPdf is called', () => {
    const formName = 'testForm';
    const mockCanvas = document.createElement('canvas');
    document.body.appendChild(mockCanvas);

    mockCanvas.width = 600;
    mockCanvas.height = 1200;
    window['scrollY'] = -1;
    spyOn(document, 'getElementById').and.returnValue(mockCanvas); // Mock getElementById

    component.exportAsPdf(formName);

    expect(component.saving).toBeTrue();
    expect(document.getElementById).toHaveBeenCalledWith(formName);
    document.body.removeChild(mockCanvas);

  });
});
