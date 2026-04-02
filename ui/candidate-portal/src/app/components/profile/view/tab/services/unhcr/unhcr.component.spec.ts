/*
 * Copyright (c) 2026 Talent Catalog.
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

import {ComponentFixture, TestBed} from '@angular/core/testing';
import {NO_ERRORS_SCHEMA} from "@angular/core";
import {of, throwError} from 'rxjs';
import {UnhcrComponent} from './unhcr.component';
import {CasiPortalService} from "../../../../../../services/casi-portal.service";

describe('UnhcrComponent', () => {
  let component: UnhcrComponent;
  let fixture: ComponentFixture<UnhcrComponent>;
  let mockPortalService: jasmine.SpyObj<CasiPortalService>;

  beforeEach(async () => {
    mockPortalService = jasmine.createSpyObj('CasiPortalService', ['getAssignment', 'assign']);
    mockPortalService.getAssignment.and.returnValue(of(null as any));
    mockPortalService.assign.and.returnValue(of({
      resource: {resourceCode: 'https://help.unhcr.org/pakistan/'}
    } as any));

    await TestBed.configureTestingModule({
      declarations: [UnhcrComponent],
      providers: [{provide: CasiPortalService, useValue: mockPortalService}],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(UnhcrComponent);
    component = fixture.componentInstance;
    component.candidate = {
      id: 1,
      country: {name: 'Pakistan', isoCode: 'PK'}
    } as any;
    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should auto-assign when no assignment exists', () => {
    component.ngOnInit();
    expect(mockPortalService.assign).toHaveBeenCalledWith('UNHCR', 'HELP_SITE_LINK');
  });

  it('should surface load errors', () => {
    mockPortalService.getAssignment.and.returnValue(throwError(() => new Error('boom')));
    component.ngOnInit();
    expect(component.error).toBeTruthy();
  });
});
