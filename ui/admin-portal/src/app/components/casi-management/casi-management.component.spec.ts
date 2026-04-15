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

import {NO_ERRORS_SCHEMA} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {of} from 'rxjs';
import {CasiManagementComponent} from './casi-management.component';
import {AuthenticationService} from "../../services/authentication.service";
import {AuthorizationService} from "../../services/authorization.service";
import {LocalStorageService} from "../../services/local-storage.service";

describe('CasiManagementComponent', () => {
  let component: CasiManagementComponent;
  let fixture: ComponentFixture<CasiManagementComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [CasiManagementComponent],
      providers: [
        {
          provide: AuthenticationService,
          useValue: {getLoggedInUser: () => ({id: 1})}
        },
        {
          provide: AuthorizationService,
          useValue: {isSystemAdminOnly: () => true}
        },
        {
          provide: LocalStorageService,
          useValue: {get: () => null, set: () => of(null)}
        }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(CasiManagementComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
