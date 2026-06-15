/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 *  the terms of the GNU General Public License as published by the Free
 *  Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

import {AuthenticationService} from "../../services/authentication.service";
import {of} from "rxjs";
import {IdpStatus} from "../../services/idp-status";

export function createMockAuthenticationService() {
  return {
    canViewChats: jasmine.createSpy('canViewChats'),
    clearAuthError: jasmine.createSpy('clearAuthError'),
    completeLogin: jasmine.createSpy('completeLogin'),
    getAuthStatus: jasmine.createSpy('getAuthStatus').and.returnValue(of({
      initialized: true,
      authenticated: true,
    } as IdpStatus)),
    getLoggedInUser: jasmine.createSpy('getLoggedInUser'),
    getToken: jasmine.createSpy('getToken'),
    isAuthenticated: jasmine.createSpy('isAuthenticated').and.returnValue(true),
    loggedInUser$: of(null),
    login: jasmine.createSpy('login'),
    logout: jasmine.createSpy('logout'),
    refreshToken: jasmine.createSpy('refreshToken'),
  };
}

export function provideMockAuthenticationService() {
  return {
    provide: AuthenticationService,
    useValue: createMockAuthenticationService()
  };
}
