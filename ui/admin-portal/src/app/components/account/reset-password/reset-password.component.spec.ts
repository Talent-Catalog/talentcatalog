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
import {ResetPasswordComponent} from './reset-password.component';
import {UntypedFormBuilder, ReactiveFormsModule} from '@angular/forms';
import {UserService} from "../../../services/user.service";
import {of} from 'rxjs';
import {SendResetPasswordEmailRequest} from "../../../model/candidate";
import {config_test} from "../../../../config-test";

describe('ResetPasswordComponent', () => {
  let component: ResetPasswordComponent;
  let fixture: ComponentFixture<ResetPasswordComponent>;
  let userService: jasmine.SpyObj<UserService>;

  beforeEach(async () => {
    const userServiceSpy = jasmine.createSpyObj('UserService', ['sendResetPassword']);
    await TestBed.configureTestingModule({
      imports: [ReactiveFormsModule],
      declarations: [ResetPasswordComponent],
      providers: [UntypedFormBuilder, { provide: UserService, useValue: userServiceSpy }]
    }).compileComponents();
    userService = TestBed.inject(UserService) as jasmine.SpyObj<UserService>;
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ResetPasswordComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should initialize the form with an empty email field', () => {
    expect(component.resetPasswordForm.value.email).toEqual('');
  });
  it('should clear email field and mark form controls as pristine when resetForm() is called', () => {
    // Arrange
    component.resetPasswordForm.patchValue({ email: config_test.credentials.email });
    // Act
    component.resetForm();
    // Assert
    expect(component.resetPasswordForm.value.email).toEqual('');
    expect(component.resetPasswordForm.controls['email'].pristine).toBeTrue();
  });
  it('should call sendResetEmail() function when "Email Me" button is clicked with a valid email', () => {
    // Arrange
    component.resetPasswordForm.patchValue({ email: config_test.credentials.email});
    userService.sendResetPassword.and.returnValue(of(true));
    // Act
    component.sendResetEmail();
    // Assert
    expect(userService.sendResetPassword).toHaveBeenCalledWith(jasmine.objectContaining({ email: config_test.credentials.email } as SendResetPasswordEmailRequest));
  });
});
