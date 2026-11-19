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

import type {MockedObject} from "vitest";
import {TestBed} from '@angular/core/testing';
import {ActivatedRouteSnapshot, Router} from '@angular/router';
import {RoleGuardService} from './role-guard.service';
import {AuthorizationService} from './authorization.service';
import {Role} from "../model/user";
import {expect, vi} from 'vitest';

describe('RoleGuardService', () => {
    let service: RoleGuardService;
    let authService: MockedObject<AuthorizationService>;
    let router: MockedObject<Router>;

    beforeEach(() => {
        const authServiceSpy = {
            getLoggedInRole: vi.fn().mockName("AuthorizationService.getLoggedInRole")
        };
        const routerSpy = {
            navigate: vi.fn().mockName("Router.navigate")
        };

        TestBed.configureTestingModule({
            providers: [
                RoleGuardService,
                { provide: AuthorizationService, useValue: authServiceSpy },
                { provide: Router, useValue: routerSpy }
            ]
        });

        service = TestBed.inject(RoleGuardService);
        authService = TestBed.inject(AuthorizationService) as MockedObject<AuthorizationService>;
        router = TestBed.inject(Router) as MockedObject<Router>;
    });

    it('should be created', () => {
        expect(service).toBeTruthy();
    });

    describe('canActivate', () => {
        it('should return true if the user role is included in expectedRoles', () => {
            const mockRoute = new ActivatedRouteSnapshot();
            mockRoute.data = { expectedRoles: [Role.systemadmin, Role.admin] };
            authService.getLoggedInRole.mockReturnValue(Role.admin);

            expect(service.canActivate(mockRoute)).toBe(true);
        });

        it('should return false if the user role is not included in expectedRoles', () => {
            const mockRoute = new ActivatedRouteSnapshot();
            mockRoute.data = { expectedRoles: [Role.admin, Role.partneradmin] };
            authService.getLoggedInRole.mockReturnValue(Role.limited);

            expect(service.canActivate(mockRoute)).toBe(false);
        });

        it('should not navigate if the user role is included in expectedRoles', () => {
            const mockRoute = new ActivatedRouteSnapshot();
            mockRoute.data = { expectedRoles: [Role.systemadmin, Role.admin] };
            authService.getLoggedInRole.mockReturnValue(Role.partneradmin);

            service.canActivate(mockRoute);

            expect(router.navigate).not.toHaveBeenCalled();
        });

    });
});
