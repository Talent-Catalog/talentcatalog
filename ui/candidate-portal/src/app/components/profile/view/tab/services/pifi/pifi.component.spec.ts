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

import {ComponentFixture, TestBed} from '@angular/core/testing';
import {NO_ERRORS_SCHEMA} from '@angular/core';
import {of, throwError} from 'rxjs';
import {PifiComponent} from './pifi.component';
import {CasiPortalService} from '../../../../../../services/casi-portal.service';

describe('PifiComponent', () => {
  let component: PifiComponent;
  let fixture: ComponentFixture<PifiComponent>;
  let mockPortalService: jasmine.SpyObj<CasiPortalService>;

  beforeEach(async () => {
    mockPortalService = jasmine.createSpyObj('CasiPortalService', ['getAssignment', 'assign']);
    mockPortalService.getAssignment.and.returnValue(of(null as any));
    mockPortalService.assign.and.returnValue(of({
      resource: {resourceCode: 'https://pifiproperty.com'}
    } as any));

    await TestBed.configureTestingModule({
      declarations: [PifiComponent],
      providers: [{provide: CasiPortalService, useValue: mockPortalService}],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(PifiComponent);
    component = fixture.componentInstance;
    component.candidate = {
      id: 1,
      country: {name: 'Australia', isoCode: 'AU'}
    } as any;
    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should auto-assign when no assignment exists', () => {
    component.ngOnInit();
    expect(mockPortalService.assign).toHaveBeenCalledWith('PIFI', 'HELP_SITE_LINK');
  });

  it('should surface load errors', () => {
    mockPortalService.getAssignment.and.returnValue(throwError(() => new Error('boom')));
    component.ngOnInit();
    expect(component.error).toBeTruthy();
    expect(component.loading).toBeFalse();
  });

  it('should use an existing assignment without assigning again', () => {
    const existing = {
      id: 9,
      resource: {resourceCode: 'https://migrants.pifiproperty.com/'}
    } as any;
    mockPortalService.getAssignment.and.returnValue(of(existing));
    mockPortalService.assign.calls.reset();

    component.ngOnInit();

    expect(component.assignment).toEqual(existing);
    expect(component.loading).toBeFalse();
    expect(mockPortalService.assign).not.toHaveBeenCalled();
  });

  it('should surface assign errors', () => {
    mockPortalService.getAssignment.and.returnValue(of(null as any));
    mockPortalService.assign.and.returnValue(throwError(() => new Error('assign fail')));

    component.ngOnInit();

    expect(component.error).toBeTruthy();
    expect(component.loading).toBeFalse();
  });

  it('should return the candidate country name', () => {
    expect(component.countryName).toBe('Australia');
  });

  it('should fall back when the candidate has no country', () => {
    component.candidate = {id: 1} as any;
    expect(component.countryName).toBe('your destination');
  });

  it('should emit when back is clicked', () => {
    spyOn(component.backButtonClicked, 'emit');
    component.onBackButtonClicked();
    expect(component.backButtonClicked.emit).toHaveBeenCalled();
  });
});
