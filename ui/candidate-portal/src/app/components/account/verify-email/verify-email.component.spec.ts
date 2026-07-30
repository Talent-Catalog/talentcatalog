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

import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {Subject, throwError} from 'rxjs';

import {UserService} from '../../../services/user.service';
import {VerifyEmailComponent} from './verify-email.component';

describe('VerifyEmailComponent', () => {
  let component: VerifyEmailComponent;
  let userServiceSpy: jasmine.SpyObj<UserService>;
  let activeModalSpy: jasmine.SpyObj<NgbActiveModal>;

  beforeEach(() => {
    userServiceSpy = jasmine.createSpyObj<UserService>(
      'UserService',
      ['sendVerifyEmail']
    );

    activeModalSpy = jasmine.createSpyObj<NgbActiveModal>(
      'NgbActiveModal',
      ['close']
    );

    component = new VerifyEmailComponent(
      userServiceSpy,
      activeModalSpy
    );

    component.userEmail = 'ehsan@example.com';
  });

  it('should create with the initial state', () => {
    expect(component).toBeTruthy();
    expect(component.state).toBe('idle');
    expect(component.emailSent).toBeFalse();
    expect(component.error).toBeUndefined();
  });

  it('should close the active modal', () => {
    component.closeModal();

    expect(activeModalSpy.close).toHaveBeenCalledTimes(1);
  });

  it('should enter loading state and send the supplied email', () => {
    const response$ = new Subject<Object>();
    userServiceSpy.sendVerifyEmail.and.returnValue(response$);

    component.sendVerifyEmail();

    expect(component.state).toBe('loading');
    expect(userServiceSpy.sendVerifyEmail)
    .toHaveBeenCalledTimes(1);

    const request =
      userServiceSpy.sendVerifyEmail.calls.mostRecent().args[0];

    expect(request.email).toBe('ehsan@example.com');
  });

  it('should enter emailSent state after a successful request', () => {
    const response$ = new Subject<Object>();
    userServiceSpy.sendVerifyEmail.and.returnValue(response$);

    component.sendVerifyEmail();
    response$.next({});

    expect(component.state).toBe('emailSent');
    expect(component.emailSent).toBeTrue();
    expect(component.error).toBeUndefined();
  });
  
  it('should use the server error message when sending fails', () => {
    const error = new Error(
      'Unable to send verification email'
    );

    const consoleSpy = spyOn(console, 'error');

    // RxJS 6 syntax: pass the error directly.
    userServiceSpy.sendVerifyEmail.and.returnValue(
      throwError(error)
    );

    component.sendVerifyEmail();

    expect(component.state).toBe('error');
    expect(component.emailSent).toBeFalse();
    expect(component.error).toBe(
      'Unable to send verification email'
    );

    expect(consoleSpy).toHaveBeenCalledOnceWith(
      'Error sending verification email:',
      error
    );
  });

  it('should use the fallback message when the error has no message', () => {
    const error = {};
    const consoleSpy = spyOn(console, 'error');

    // RxJS 6 syntax: pass the error directly.
    userServiceSpy.sendVerifyEmail.and.returnValue(
      throwError(error)
    );

    component.sendVerifyEmail();

    expect(component.state).toBe('error');
    expect(component.error).toBe(
      'An error occurred while sending the verification email.'
    );

    expect(consoleSpy).toHaveBeenCalledOnceWith(
      'Error sending verification email:',
      error
    );
  });
});
