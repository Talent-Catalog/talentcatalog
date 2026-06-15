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

import {fakeAsync, flushMicrotasks, TestBed} from '@angular/core/testing';
import {HttpClientTestingModule, HttpTestingController} from '@angular/common/http/testing';
import {AuthenticationService} from './authentication.service';
import {User} from '../model/user';
import {environment} from '../../environments/environment';
import {EncodedQrImage} from "../util/qr";
import {LocalStorageService} from "./local-storage.service";
import {IDP_PROVIDER} from "./idp.tokens";
import {IdpProvider} from "./idp-provider";
import {AuthenticationResponse} from "../model/authentication-response";

describe('AuthenticationService', () => {
  let service: AuthenticationService;
  let httpMock: HttpTestingController;
  let localStorageService: jasmine.SpyObj<LocalStorageService>;
  let idpProvider: jasmine.SpyObj<IdpProvider>;

  beforeEach(() => {
    const localStorageSpy = jasmine.createSpyObj('LocalStorageService', ['get', 'set', 'remove']);
    idpProvider = jasmine.createSpyObj<IdpProvider>('IdpProvider', [
      'login',
      'logout',
      'getProfile'
    ]);
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        AuthenticationService,
        { provide: IDP_PROVIDER, useValue: idpProvider },
        { provide: LocalStorageService, useValue: localStorageSpy },
      ]
    });

    service = TestBed.inject(AuthenticationService);
    httpMock = TestBed.inject(HttpTestingController);
    localStorageService = TestBed.inject(LocalStorageService) as jasmine.SpyObj<LocalStorageService>;
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should delegate logout to the IDP provider', async () => {

    idpProvider.logout.and.resolveTo();

    await service.logout();

    expect(idpProvider.logout)

    .toHaveBeenCalledOnceWith();
  });

  it('should delegate login to the IDP provider', async () => {

    idpProvider.login.and.resolveTo();

    await service.login('http://localhost/callback', 'en');

    expect(idpProvider.login)

    .toHaveBeenCalledOnceWith('http://localhost/callback', 'en');

  });

  it('should set logged in user and update local storage', () => {
    const user: User = { id: 1, name: 'Test User', role: 'user', readOnly: false } as User;
    service.setLoggedInUser(user);

    expect(service['loggedInUser']).toEqual(user);
    expect(localStorageService.set).toHaveBeenCalledWith('user', user);
  });
  it('should use English as the default login language', async () => {

    idpProvider.login.and.resolveTo();

    await service.login('http://localhost/callback');

    expect(idpProvider.login)

    .toHaveBeenCalledOnceWith('http://localhost/callback', 'en');

  });

  it('should complete login by sending the IDP profile to the server', fakeAsync(() => {

    const profile = {

      id: 'idp-user-123',

      email: 'candidate@example.org',

      firstName: 'Test',

      lastName: 'Candidate'

    };

    const response: AuthenticationResponse = {
      tcInstanceType: 'TBB',
      user: { id: 1, name: 'Test User', role: 'user', readOnly: false } as User,
      canViewChats: true
    } as AuthenticationResponse;

    idpProvider.getProfile.and.resolveTo(profile as any);

    spyOn<any>(service, 'storeAuthenticationData');

    service.completeLogin().subscribe();

    flushMicrotasks();

    const req = httpMock.expectOne(`${service['apiUrl']}/login`);

    expect(req.request.method).toBe('POST');

    expect(req.request.body).toEqual(profile);

    req.flush(response);

    expect(service['storeAuthenticationData'])

    .toHaveBeenCalledOnceWith(response);

  }));

  it('should propagate an IDP profile error', fakeAsync (() => {
    const error = new Error('Profile failed');

    idpProvider.getProfile.and.rejectWith(error);

    service.completeLogin().subscribe({
      next: () => fail('Expected error'),

      error: err => {
        expect(err).toBe(error);
      }
    });
  }));

  it('should propagate a server login error', fakeAsync( () => {

    const profile = {
      id: 'idp-user-123',
      email: 'candidate@example.org'
    };

    idpProvider.getProfile.and.resolveTo(profile as any);

    let receivedError: any;
    service.completeLogin().subscribe({
      next: () => fail('Expected error'),
      error: error => receivedError = error
      }
    );

    flushMicrotasks()

    const req = httpMock.expectOne(`${service['apiUrl']}/login`);
    req.flush(
      { message: 'Login failed' },
      { status: 500, statusText: 'Server Error' }
    );

    expect(receivedError.status).toBe(500);
  }));

  it('should get logged in user from local storage', () => {
    const user: User = { id: 1, name: 'Test User', role: 'user', readOnly: false } as User;
    localStorageService.get.and.returnValue(user);

    const loggedInUser = service.getLoggedInUser();
    expect(loggedInUser).toEqual(user);
    expect(localStorageService.get).toHaveBeenCalledWith('user');
  });

  it('should handle invalid user info in local storage', () => {
    localStorageService.get.and.returnValue({});

    spyOn(service, 'logout');
    const loggedInUser = service.getLoggedInUser();
    expect(loggedInUser).toBeNull();
    expect(service.logout).toHaveBeenCalled();
  });

  it('should set null logged in user if local storage is cleared', () => {
    localStorageService.get.and.returnValue(null);

    const loggedInUser = service.getLoggedInUser();
    expect(loggedInUser).toBeNull();
    expect(localStorageService.get).toHaveBeenCalledWith('user');
  });

  it('should complete loggedInUser$ subject on destroy', () => {
    spyOn(service['loggedInUser$'], 'complete');
    service.ngOnDestroy();
    expect(service['loggedInUser$'].complete).toHaveBeenCalled();
  });

  it('should set up MFA and return encoded QR image', () => {
    const encodedQrImage:EncodedQrImage = { base64Encoding: 'test-image' };

    service.mfaSetup().subscribe(response => {
      return expect(response).toEqual(encodedQrImage);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/auth/mfa-setup`);
    expect(req.request.method).toBe('POST');
    req.flush(encodedQrImage);
  });
});
