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

import {ComponentFixture, TestBed} from '@angular/core/testing';
import {TranslateModule, TranslateService} from '@ngx-translate/core';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {RouterTestingModule} from '@angular/router/testing';
import {RegistrationService} from '../../../services/registration.service';
import {SharedModule} from '../../../shared/shared.module';

import {RegistrationUploadFileComponent} from './registration-upload-file.component';

describe('UploadFileComponent', () => {
  let component: RegistrationUploadFileComponent;
  let fixture: ComponentFixture<RegistrationUploadFileComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ RegistrationUploadFileComponent ],
      imports: [
        SharedModule,
        HttpClientTestingModule,
        RouterTestingModule,
        TranslateModule.forRoot()
      ],
      providers: [
        RegistrationService,
        TranslateService
      ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(RegistrationUploadFileComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize activeIndexes to 0 when edit is false', () => {
    component.edit = false;
    component.ngOnInit();
    expect(component.activeIndexes).toBe(0);
  });

  it('should initialize activeIndexes to null when edit is true', () => {
    component.edit = true;
    component.ngOnInit();
    expect(component.activeIndexes).toBeNull();
  });
});
