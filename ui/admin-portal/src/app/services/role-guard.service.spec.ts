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
import {ActivatedRouteSnapshot, Router} from '@angular/router';
import {RoleGuardService} from './role-guard.service';
import {AuthorizationService} from './authorization.service';
import {Role} from "../model/user";

describe('RoleGuardService', () => {
  let service: RoleGuardService;
  let authService: jasmine.SpyObj<AuthorizationService>;
  let router: jasmine.SpyObj<Router>;

  beforeEach(() => {
    const authServiceSpy = jasmine.createSpyObj('AuthorizationService', ['getLoggedInRole']);
    const routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    TestBed.configureTestingModule({
      providers: [
        RoleGuardService,
        { provide: AuthorizationService, useValue: authServiceSpy },
        { provide: Router, useValue: routerSpy }
      ]
    });

    service = TestBed.inject(RoleGuardService);
    authService = TestBed.inject(AuthorizationService) as jasmine.SpyObj<AuthorizationService>;
    router = TestBed.inject(Router) as jasmine.SpyObj<Router>;
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('canActivate', () => {
    it('should return true if the user role is included in expectedRoles', () => {
      const mockRoute = new ActivatedRouteSnapshot();
      mockRoute.data = { expectedRoles: [Role.systemadmin,Role.admin] };
      authService.getLoggedInRole.and.returnValue(Role.admin);

      expect(service.canActivate(mockRoute)).toBeTrue();
    });

    it('should return false if the user role is not included in expectedRoles', () => {
      const mockRoute = new ActivatedRouteSnapshot();
      mockRoute.data = { expectedRoles: [Role.admin,Role.partneradmin] };
      authService.getLoggedInRole.and.returnValue(Role.limited);

      expect(service.canActivate(mockRoute)).toBeFalse();
    });

    it('should not navigate if the user role is included in expectedRoles', () => {
      const mockRoute = new ActivatedRouteSnapshot();
      mockRoute.data = { expectedRoles: [Role.systemadmin,Role.admin] };
      authService.getLoggedInRole.and.returnValue(Role.partneradmin);

      service.canActivate(mockRoute);

      expect(router.navigate).not.toHaveBeenCalled();
    });

  });
});
