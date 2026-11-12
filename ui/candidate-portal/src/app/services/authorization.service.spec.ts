import type { MockedObject } from "vitest";
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

import { AuthorizationService } from "./authorization.service";
import { AuthenticationService } from "./authentication.service";
import { TestBed } from "@angular/core/testing";


describe('AuthorizationService', () => {
    let service: AuthorizationService;
    let authenticationServiceSpy: MockedObject<AuthenticationService>;

    beforeEach(() => {
        authenticationServiceSpy = {
            canViewChats: vi.fn().mockName("AuthenticationService.canViewChats")
        } as unknown as MockedObject<AuthenticationService>;

        TestBed.configureTestingModule({
            providers: [
                AuthorizationService,
                {
                    provide: AuthenticationService,
                    useValue: authenticationServiceSpy
                }
            ]
        });

        service = TestBed.inject(AuthorizationService);
    });

    it('should be created', () => {
        expect(service).toBeTruthy();
    });

    it('should return true when the authenticated user can view chats', () => {
        authenticationServiceSpy.canViewChats.mockReturnValue(true);

        expect(service.canViewChats()).toBe(true);
        expect(authenticationServiceSpy.canViewChats).toHaveBeenCalled();
    });

    it('should return false when the authenticated user cannot view chats', () => {
        authenticationServiceSpy.canViewChats.mockReturnValue(false);

        expect(service.canViewChats()).toBe(false);
        expect(authenticationServiceSpy.canViewChats).toHaveBeenCalled();
    });
});
