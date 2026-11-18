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
import {expect, vi} from 'vitest';
import {MockCandidate} from '../../../MockData/MockCandidate';
import {PrintPageComponent} from './print-page.component';

describe('PrintPageComponent', () => {
  let component: PrintPageComponent;
  let fixture: ComponentFixture<PrintPageComponent>;

  beforeEach(async () => {
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
    vi.restoreAllMocks();
    fixture.destroy();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('printPage should call window.print', () => {
    const printSpy = vi.spyOn(window, 'print').mockImplementation(() => {});
    component.printPage();
    expect(printSpy).toHaveBeenCalledTimes(1);
  });

  it('should run ngOnInit', () => {
    expect(() => component.ngOnInit()).not.toThrow();
  });
});
