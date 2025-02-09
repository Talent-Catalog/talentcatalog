/*
 * Copyright (c) 2025 Talent Catalog.
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

import {ComponentFixture, fakeAsync, flush, TestBed, tick, waitForAsync} from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { RouterTestingModule } from '@angular/router/testing';
import { of, throwError } from 'rxjs';
import { VerifyEmailComponent } from './verify-email.component';
import { UserService } from '../../../services/user.service';
import { AuthenticationService } from '../../../services/authentication.service';
import { ActivatedRoute, Router } from '@angular/router'; // Import Router
import { MockUser } from '../../../MockData/MockUser';

describe('VerifyEmailComponent', () => {
  let component: VerifyEmailComponent;
  let fixture: ComponentFixture<VerifyEmailComponent>;
  let userServiceSpy: jasmine.SpyObj<UserService>;
  let authServiceSpy: jasmine.SpyObj<AuthenticationService>;
  let routerSpy: jasmine.SpyObj<Router>;

  beforeEach(async () => {
    const userServiceSpyObj = jasmine.createSpyObj('UserService', ['get', 'sendVerifyEmail', 'checkEmailVerificationToken', 'verifyEmail']);
    const authServiceSpyObj = jasmine.createSpyObj('AuthenticationService', ['getLoggedInUser']);
    const routerSpyObj = jasmine.createSpyObj('Router', ['navigate']);

    await TestBed.configureTestingModule({
      declarations: [VerifyEmailComponent],
      imports: [HttpClientTestingModule, ReactiveFormsModule, RouterTestingModule],
      providers: [
        { provide: UserService, useValue: userServiceSpyObj },
        { provide: AuthenticationService, useValue: authServiceSpyObj },
        { provide: Router, useValue: routerSpyObj },
        { provide: ActivatedRoute, useValue: { snapshot: { queryParamMap: { get: () => 'testToken' } } } }
      ]
    }).compileComponents();

    userServiceSpy = TestBed.inject(UserService) as jasmine.SpyObj<UserService>;
    authServiceSpy = TestBed.inject(AuthenticationService) as jasmine.SpyObj<AuthenticationService>;
    routerSpy = TestBed.inject(Router) as jasmine.SpyObj<Router>;
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(VerifyEmailComponent);
    component = fixture.componentInstance;
    authServiceSpy.getLoggedInUser.and.returnValue(new MockUser());
    userServiceSpy.get.and.returnValue(of(new MockUser()));
    userServiceSpy.checkEmailVerificationToken.and.returnValue(of({"token": "eb154587-2c91-4be9-9bcd-f15450474d24"}));
    userServiceSpy.verifyEmail.and.returnValue(of({}));
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize user email on ngOnInit', () => {
    component.ngOnInit();
    expect(component.userEmail).toBe('john.doe@example.com');
  });

  it('should send verification email and handle success', waitForAsync(() => {
    userServiceSpy.sendVerifyEmail.and.returnValue(of({}));
    component.sendVerifyEmail('testToken');
    fixture.whenStable().then(() => {
      expect(component.emailSent).toBeTrue();
      expect(component.state).toBe('emailSent');
    });
  }));

  it('should handle error when sending verification email', waitForAsync(() => {
    userServiceSpy.sendVerifyEmail.and.returnValue(throwError(() => new Error('Error occurred')));
    component.sendVerifyEmail('testToken');
    fixture.whenStable().then(() => {
      expect(component.state).toBe('error');
    });
  }));
});
