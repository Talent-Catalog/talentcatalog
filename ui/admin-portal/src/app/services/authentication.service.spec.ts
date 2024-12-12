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

import {TestBed} from '@angular/core/testing';
import {HttpClientTestingModule, HttpTestingController} from '@angular/common/http/testing';
import {AuthenticationService} from './authentication.service';
import {User} from '../model/user';
import {LoginRequest} from '../model/base';
import {JwtResponse} from '../model/jwt-response';
import {environment} from '../../environments/environment';
import {of} from 'rxjs';
import {HttpClient} from '@angular/common/http';
import {config_test} from "../../config-test";
import {EncodedQrImage} from "../util/qr";
import {LocalStorageService} from "./local-storage.service";

describe('AuthenticationService', () => {
  let service: AuthenticationService;
  let httpMock: HttpTestingController;
  let localStorageService: jasmine.SpyObj<LocalStorageService>;

  beforeEach(() => {
    const localStorageSpy = jasmine.createSpyObj('LocalStorageService', ['get', 'set', 'remove']);

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        AuthenticationService,
        { provide: LocalStorageService, useValue: localStorageSpy }
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

  it('should set logged in user and update local storage', () => {
    const user: User = { id: 1, name: 'Test User', role: 'user', readOnly: false } as User;
    service.setLoggedInUser(user);

    expect(service['loggedInUser']).toEqual(user);
    expect(localStorageService.set).toHaveBeenCalledWith('user', user);
  });

  it('should log in and store credentials', () => {
    const credentials: LoginRequest = { username: config_test.credentials.username, password: config_test.credentials.password,totpToken:config_test.credentials.totpToken,reCaptchaV3Token:'' };
    const jwtResponse: JwtResponse = { accessToken: 'test-token', name:'', gender:'', user: { id: 1, name: 'Test User', role: 'user', readOnly: false } as User };

    service.login(credentials).subscribe(response => {
      expect(service['loggedInUser']).toEqual(jwtResponse.user);
      expect(localStorageService.set).toHaveBeenCalledWith('access-token', jwtResponse.accessToken);
      expect(localStorageService.set).toHaveBeenCalledWith('user', jwtResponse.user);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/auth/login`);
    expect(req.request.method).toBe('POST');
    req.flush(jwtResponse);
  });

  it('should handle login error', () => {
    const credentials: LoginRequest = { username: config_test.credentials.username, password: config_test.credentials.password,totpToken:config_test.credentials.totpToken,reCaptchaV3Token:'' };
    const errorResponse = { status: 401, statusText: 'Unauthorized' };

    service.login(credentials).subscribe(
      () => fail('expected an error, not credentials'),
      error => {
        expect(error.status).toBe(401);
      }
    );

    const req = httpMock.expectOne(`${environment.apiUrl}/auth/login`);
    expect(req.request.method).toBe('POST');
    req.flush(null, errorResponse);
  });

  it('should log out and clear credentials', () => {
    const httpClient = TestBed.inject(HttpClient);
    const postSpy = httpClient.post = jasmine.createSpy().and.returnValue(of(null));

    service.logout();

    expect(postSpy).toHaveBeenCalledWith(`${environment.apiUrl}/auth/logout`, null);
    expect(localStorageService.remove).toHaveBeenCalledWith('user');
    expect(localStorageService.remove).toHaveBeenCalledWith('access-token');
    expect(service['loggedInUser']).toBeNull();
  });

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
