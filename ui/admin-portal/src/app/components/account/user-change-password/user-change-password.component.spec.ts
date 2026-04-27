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

import {ComponentFixture, fakeAsync, TestBed, tick, waitForAsync} from '@angular/core/testing';
import {UserChangePasswordComponent} from './user-change-password.component';
import {UntypedFormBuilder, ReactiveFormsModule} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {of, throwError} from 'rxjs';
import {UserService} from '../../../services/user.service';
import {config_test} from "../../../../config-test";

describe('UserChangePasswordComponent', () => {
  let component: UserChangePasswordComponent;
  let fixture: ComponentFixture<UserChangePasswordComponent>;
  let mockUserService: jasmine.SpyObj<UserService>;
  let mockActivatedRoute: any;
  let mockRouter: any;

  beforeEach(waitForAsync(() => {
    mockUserService = jasmine.createSpyObj('UserService', ['checkPasswordResetToken', 'resetPassword']);

    mockActivatedRoute = {
      paramMap: of({ get: () => config_test.credentials.totpToken })
    };
    mockRouter = jasmine.createSpyObj('Router', ['navigate']);
     // Mock implementation for checkPasswordResetToken
    mockUserService.checkPasswordResetToken.and.returnValue(of({ success: true }));
// Mock implementation for resetPassword
    mockUserService.resetPassword.and.returnValue(of({ success: true }));
    TestBed.configureTestingModule({
      declarations: [UserChangePasswordComponent],
      imports: [ReactiveFormsModule],
      providers: [
        UntypedFormBuilder,
        { provide: UserService, useValue: mockUserService },
        { provide: ActivatedRoute, useValue: mockActivatedRoute },
        { provide: Router, useValue: mockRouter }
      ]
    })
    .compileComponents();

  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(UserChangePasswordComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form based on token', () => {
    expect(component.changePasswordForm).toBeDefined();
    expect(component.token).toBe(config_test.credentials.totpToken);
  });
  //
  it('should initialize form without token', () => {
    mockActivatedRoute.paramMap = of({ get: () => null });
    fixture = TestBed.createComponent(UserChangePasswordComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    expect(component.changePasswordForm).toBeDefined();
    expect(component.token).toBeNull();
  });

  it('should update password successfully', fakeAsync(() => {
    component.reset = true;
    component.changePasswordForm.setValue({
      token: config_test.credentials.totpToken,
      password: config_test.credentials.password,
      passwordConfirmation: config_test.credentials.password
    });
    mockUserService.resetPassword.and.returnValue(of(null));
    component.updatePassword();
    tick(2100);
     expect(mockUserService.resetPassword).toHaveBeenCalledWith({
       token: config_test.credentials.totpToken,
       password: config_test.credentials.password,
       passwordConfirmation: config_test.credentials.password
    });
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/login']);
    expect(component.updated).toBeTruthy();
  }));

  it('should handle error during password update', () => {
    component.reset = true;
    component.changePasswordForm.setValue({
      token: config_test.credentials.totpToken,
      password: config_test.credentials.password,
      passwordConfirmation: config_test.credentials.password
    });
    const errorMessage = 'Password update failed';
    mockUserService.resetPassword.and.returnValue(throwError(errorMessage));
    component.updatePassword();
    expect(mockUserService.resetPassword).toHaveBeenCalledWith({
      token: config_test.credentials.totpToken,
      password: config_test.credentials.password,
      passwordConfirmation: config_test.credentials.password
    });
    expect(component.error).toBe(errorMessage);
  });
  //
  it('should reset form', () => {
    component.reset = true;
    component.changePasswordForm.setValue({
      token: config_test.credentials.totpToken,
      password: config_test.credentials.password,
      passwordConfirmation: config_test.credentials.password
    });
    component.resetForm();
    expect(component.changePasswordForm.value).toEqual({
      token: '',
      password: '',
      passwordConfirmation: ''
    });
    expect(component.updated).toBeFalsy();
    expect(component.error).toBeNull();
  });

  afterAll(()=>{
    fixture.destroy();
  })
});
