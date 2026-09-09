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

import {TestBed} from '@angular/core/testing';
import {ActivatedRoute, Router} from '@angular/router';
import {RouterTestingModule} from '@angular/router/testing';
import {of} from 'rxjs';

import {RegistrationService} from './registration.service';
import {AuthenticationService} from './authentication.service';
import {environment} from '../../environments/environment';

describe('RegistrationService', () => {
  let service: RegistrationService;
  let authenticationService: jasmine.SpyObj<AuthenticationService>;
  let router: Router;
  let isAuthenticated = false;
  let isGrnInstance = false;
  let originalEnvironmentName: string;

  beforeEach(() => {
    originalEnvironmentName = environment.environmentName;
    isAuthenticated = false;
    isGrnInstance = false;
    authenticationService = jasmine.createSpyObj<AuthenticationService>(
      'AuthenticationService',
      ['isAuthenticated', 'isGrnInstance']
    );
    authenticationService.isAuthenticated.and.callFake(() => isAuthenticated);
    authenticationService.isGrnInstance.and.callFake(() => isGrnInstance);

    TestBed.configureTestingModule({
      imports: [RouterTestingModule],
      providers: [
        RegistrationService,
        {provide: AuthenticationService, useValue: authenticationService},
        {provide: ActivatedRoute, useValue: {queryParams: of({})}}
      ]
    });

    service = TestBed.inject(RegistrationService);
    router = TestBed.inject(Router);
    spyOn(router, 'navigate');
  });

  afterEach(() => {
    environment.environmentName = originalEnvironmentName;
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should include verifyplus step for unauthenticated local', () => {
    environment.environmentName = 'local';
    isAuthenticated = false;
    isGrnInstance = false;
    service.start();

    expect(service.steps.some(step => step.key === 'verifyplus')).toBeTrue();
  });

  it('should include verifyplus step for authenticated GRN staging', () => {
    environment.environmentName = 'staging';
    isAuthenticated = true;
    isGrnInstance = true;
    service.start();

    expect(service.steps.some(step => step.key === 'verifyplus')).toBeTrue();
  });

  it('should omit verifyplus step for authenticated TBB local', () => {
    environment.environmentName = 'local';
    isAuthenticated = true;
    isGrnInstance = false;
    service.start();

    expect(service.steps.some(step => step.key === 'verifyplus')).toBeFalse();
  });

  it('should omit verifyplus step for authenticated GRN prod', () => {
    environment.environmentName = 'prod';
    isAuthenticated = true;
    isGrnInstance = true;
    service.start();

    expect(service.steps.some(step => step.key === 'verifyplus')).toBeFalse();
  });

  it('should skip verifyplus when candidate becomes authenticated TBB during flow', () => {
    environment.environmentName = 'local';
    isAuthenticated = false;
    isGrnInstance = false;
    service.start();
    service.openStep('contact');

    isAuthenticated = true;
    service.next();

    expect(service.currentStepKey).toBe('personal');
  });
});
