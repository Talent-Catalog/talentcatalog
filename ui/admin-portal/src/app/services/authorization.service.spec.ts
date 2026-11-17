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
import {AuthenticationService} from './authentication.service';
import {AuthorizationService} from './authorization.service';
import {Role, User} from '../model/user';
import {Candidate} from '../model/candidate';
import {Partner} from "../model/partner";
import {CandidateSource} from "../model/base";
import {MockJob} from "../MockData/MockJob";
import {Job} from "../model/job";
import {expect, vi} from 'vitest';

describe('AuthorizationService', () => {
    let service: AuthorizationService;
    let authenticationServiceSpy: MockedObject<AuthenticationService>;
    let job: Job;

    beforeEach(() => {
        const spy = {
            getLoggedInUser: vi.fn().mockName("AuthenticationService.getLoggedInUser")
        };

        TestBed.configureTestingModule({
            providers: [
                AuthorizationService,
                { provide: AuthenticationService, useValue: spy }
            ]
        });

        service = TestBed.inject(AuthorizationService);
        authenticationServiceSpy = TestBed.inject(AuthenticationService) as MockedObject<AuthenticationService>;
        job = MockJob;
    });

    it('should be created', () => {
        expect(service).toBeTruthy();
    });

    it('should return assignable roles for partneradmin', () => {
        const user: User = { id: 1, name: 'Partner Admin', role: 'partneradmin', readOnly: false } as User;
        authenticationServiceSpy.getLoggedInUser.mockReturnValue(user);

        const roles = service.assignableUserRoles();
        expect(roles).toEqual([Role.limited, Role.semilimited]);
    });

    it('should return assignable roles for admin', () => {
        const user: User = { id: 1, name: 'Admin', role: 'admin', readOnly: false } as User;
        authenticationServiceSpy.getLoggedInUser.mockReturnValue(user);

        const roles = service.assignableUserRoles();
        expect(roles).toEqual([Role.limited, Role.semilimited, Role.partneradmin]);
    });

    it('should return assignable roles for systemadmin', () => {
        const user: User = { id: 1, name: 'System Admin', role: 'systemadmin', readOnly: false } as User;
        authenticationServiceSpy.getLoggedInUser.mockReturnValue(user);

        const roles = service.assignableUserRoles();
        expect(roles).toEqual(Object.values(Role));
    });

    it('should allow partner assignment for systemadmin', () => {
        const user: User = { id: 1, name: 'System Admin', role: 'systemadmin', readOnly: false } as User;
        authenticationServiceSpy.getLoggedInUser.mockReturnValue(user);

        const canAssign = service.canAssignPartner();
        expect(canAssign).toBe(true);
    });

    it('should not allow partner assignment for other roles', () => {
        const user: User = { id: 1, name: 'Admin', role: 'admin', readOnly: false } as User;
        authenticationServiceSpy.getLoggedInUser.mockReturnValue(user);

        const canAssign = service.canAssignPartner();
        expect(canAssign).toBe(false);
    });

    it('should allow viewing candidate country for admin roles', () => {
        const user: User = { id: 1, name: 'Admin', role: 'admin', readOnly: false } as User;
        authenticationServiceSpy.getLoggedInUser.mockReturnValue(user);

        const canView = service.canViewCandidateCountry();
        expect(canView).toBe(true);
    });

    it('should allow viewing candidate CV for job creators', () => {
        const user: User = { id: 1, name: 'Job Creator', role: 'admin', readOnly: false, partner: { jobCreator: true } as Partner } as User;
        authenticationServiceSpy.getLoggedInUser.mockReturnValue(user);

        const canView = service.canViewCandidateCV();
        expect(canView).toBe(true);
    });

    it('should not allow viewing candidate CV for non-job creators', () => {
        const user: User = { id: 1, name: 'Non Job Creator', role: 'admin', readOnly: false, partner: { jobCreator: false } as Partner } as User;
        authenticationServiceSpy.getLoggedInUser.mockReturnValue(user);

        const canView = service.canViewCandidateCV();
        expect(canView).toBe(false);
    });

    it('should allow viewing candidate name for source partners', () => {
        const user: User = { id: 1, name: 'Source Partner', role: 'admin', readOnly: false, partner: { sourcePartner: true } as Partner } as User;
        authenticationServiceSpy.getLoggedInUser.mockReturnValue(user);

        const canView = service.canViewCandidateName();
        expect(canView).toBe(true);
    });

    it('should not allow viewing candidate name for non-source partners', () => {
        const user: User = { id: 1, name: 'Non Source Partner', role: 'admin', readOnly: false, partner: { sourcePartner: false } as Partner } as User;
        authenticationServiceSpy.getLoggedInUser.mockReturnValue(user);

        const canView = service.canViewCandidateName();
        expect(canView).toBe(false);
    });

    it('should allow editing candidate details for default source partners', () => {
        const user: User = { id: 1, name: 'Default Source Partner', role: 'admin', readOnly: false, partner: { defaultSourcePartner: true } as Partner } as User;
        authenticationServiceSpy.getLoggedInUser.mockReturnValue(user);
        const candidate: Candidate = { id: 1, user: { partner: { id: 1 } as Partner } as User } as Candidate;

        const canEdit = service.isEditableCandidate(candidate);
        expect(canEdit).toBe(true);
    });

    it('should not allow editing candidate details for read-only users', () => {
        const user: User = { id: 1, name: 'Read Only User', role: 'admin', readOnly: true, partner: { defaultSourcePartner: false } as Partner } as User;
        authenticationServiceSpy.getLoggedInUser.mockReturnValue(user);
        const candidate: Candidate = { id: 1, user: { partner: { id: 1 } as Partner } as User } as Candidate;

        const canEdit = service.isEditableCandidate(candidate);
        expect(canEdit).toBe(false);
    });

    it('should return true for isAnAdmin for admin roles', () => {
        const user: User = { id: 1, name: 'Admin', role: 'admin', readOnly: false } as User;
        authenticationServiceSpy.getLoggedInUser.mockReturnValue(user);

        const isAdmin = service.isAnAdmin();
        expect(isAdmin).toBe(true);
    });

    it('should return false for isAnAdmin for non-admin roles', () => {
        const user: User = { id: 1, name: 'Limited User', role: 'limited', readOnly: false } as User;
        authenticationServiceSpy.getLoggedInUser.mockReturnValue(user);

        const isAdmin = service.isAnAdmin();
        expect(isAdmin).toBe(false);
    });

    it('should return true for isAuthenticated if user is logged in', () => {
        const user: User = { id: 1, name: 'Logged In User', role: 'admin', readOnly: false } as User;
        authenticationServiceSpy.getLoggedInUser.mockReturnValue(user);

        const isAuthenticated = service.isAuthenticated();
        expect(isAuthenticated).toBe(true);
    });

    it('should return false for isAuthenticated if no user is logged in', () => {
        authenticationServiceSpy.getLoggedInUser.mockReturnValue(null);

        const isAuthenticated = service.isAuthenticated();
        expect(isAuthenticated).toBe(false);
    });

    it('should return true for isReadOnly if user is read-only', () => {
        const user: User = { id: 1, name: 'Read Only User', role: 'admin', readOnly: true } as User;
        authenticationServiceSpy.getLoggedInUser.mockReturnValue(user);

        const isReadOnly = service.isReadOnly();
        expect(isReadOnly).toBe(true);
    });

    it('should return false for isReadOnly if user is not read-only', () => {
        const user: User = { id: 1, name: 'Non Read Only User', role: 'admin', readOnly: false } as User;
        authenticationServiceSpy.getLoggedInUser.mockReturnValue(user);

        const isReadOnly = service.isReadOnly();
        expect(isReadOnly).toBe(false);
    });

    it('should return true for isSystemAdminOnly if user is systemadmin', () => {
        const user: User = { id: 1, name: 'System Admin', role: 'systemadmin', readOnly: false } as User;
        authenticationServiceSpy.getLoggedInUser.mockReturnValue(user);

        const isSystemAdminOnly = service.isSystemAdminOnly();
        expect(isSystemAdminOnly).toBe(true);
    });

    it('should return false for isSystemAdminOnly if user is not systemadmin', () => {
        const user: User = { id: 1, name: 'Admin', role: 'admin', readOnly: false } as User;
        authenticationServiceSpy.getLoggedInUser.mockReturnValue(user);

        const isSystemAdminOnly = service.isSystemAdminOnly();
        expect(isSystemAdminOnly).toBe(false);
    });

    it('should return true for canEraseCandidateData if user is systemadmin and not read-only', () => {
        const user: User = { id: 1, name: 'System Admin', role: 'systemadmin', readOnly: false } as User;
        authenticationServiceSpy.getLoggedInUser.mockReturnValue(user);

        const canErase = service.canEraseCandidateData();
        expect(canErase).toBe(true);
    });

    it('should return false for canEraseCandidateData if user is systemadmin but read-only', () => {
        const user: User = { id: 1, name: 'Read Only System Admin', role: 'systemadmin', readOnly: true } as User;
        authenticationServiceSpy.getLoggedInUser.mockReturnValue(user);

        const canErase = service.canEraseCandidateData();
        expect(canErase).toBe(false);
    });

    it('should return false for canEraseCandidateData if user is not systemadmin', () => {
        const user: User = { id: 1, name: 'Admin', role: 'admin', readOnly: false } as User;
        authenticationServiceSpy.getLoggedInUser.mockReturnValue(user);

        const canErase = service.canEraseCandidateData();
        expect(canErase).toBe(false);
    });

    it('should return true for isAdminOrGreater if user is admin or systemadmin', () => {
        const user: User = { id: 1, name: 'System Admin', role: 'systemadmin', readOnly: false } as User;
        authenticationServiceSpy.getLoggedInUser.mockReturnValue(user);

        const isAdminOrGreater = service.isAdminOrGreater();
        expect(isAdminOrGreater).toBe(true);
    });

    it('should return false for isAdminOrGreater if user is not admin or systemadmin', () => {
        const user: User = { id: 1, name: 'Limited User', role: 'limited', readOnly: false } as User;
        authenticationServiceSpy.getLoggedInUser.mockReturnValue(user);

        const isAdminOrGreater = service.isAdminOrGreater();
        expect(isAdminOrGreater).toBe(false);
    });

    it('should return true for isPartnerAdmin if user is partneradmin', () => {
        const user: User = { id: 1, name: 'Partner Admin', role: 'partneradmin', readOnly: false } as User;
        authenticationServiceSpy.getLoggedInUser.mockReturnValue(user);

        const isPartnerAdmin = service.isPartnerAdminOrGreater();
        expect(isPartnerAdmin).toBe(true);
    });

    it('should return false for isPartnerAdmin if user is not partneradmin', () => {
        const user: User = { id: 1, name: 'Admin', role: 'limited', readOnly: false } as User;
        authenticationServiceSpy.getLoggedInUser.mockReturnValue(user);

        const isPartnerAdmin = service.isPartnerAdminOrGreater();
        expect(isPartnerAdmin).toBe(false);
    });

    it('should return true for isAdminOrPartnerAdmin if user is admin or partneradmin', () => {
        const user: User = { id: 1, name: 'Admin', role: 'admin', readOnly: false } as User;
        authenticationServiceSpy.getLoggedInUser.mockReturnValue(user);

        const isAdminOrPartnerAdmin = service.isPartnerAdminOrGreater();
        expect(isAdminOrPartnerAdmin).toBe(true);
    });

    it('should return false for isAdminOrPartnerAdmin if user is not admin or partneradmin', () => {
        const user: User = { id: 1, name: 'Limited User', role: 'limited', readOnly: false } as User;
        authenticationServiceSpy.getLoggedInUser.mockReturnValue(user);

        const isAdminOrPartnerAdmin = service.isPartnerAdminOrGreater();
        expect(isAdminOrPartnerAdmin).toBe(false);
    });

    it('should determine if source can be edited', () => {
        const source: CandidateSource = { fixed: false } as CandidateSource;
        const user: User = { id: 1, name: 'Limited User', role: 'limited', readOnly: false } as User;
        authenticationServiceSpy.getLoggedInUser.mockReturnValue(user);

        expect(service.canEditCandidateSource(source)).toBe(true);

        user.readOnly = true;
        expect(service.canEditCandidateSource(source)).toBe(false);

        user.readOnly = false;
        source.fixed = true;
        source.createdBy = user;
        expect(service.canEditCandidateSource(source)).toBe(true);

        source.createdBy = { ...user, id: 2 };
        expect(service.canEditCandidateSource(source)).toBe(false);
    });

    it('should determine if source is mine', () => {
        const user: User = { id: 1 } as User;
        const source: CandidateSource = { createdBy: user } as CandidateSource;
        authenticationServiceSpy.getLoggedInUser.mockReturnValue(user);
        expect(service.isCandidateSourceMine(source)).toBe(true);

        authenticationServiceSpy.getLoggedInUser.mockReturnValue({ ...user, id: 2 });
        expect(service.isCandidateSourceMine(source)).toBe(false);
    });

    it('should determine if source is starred by me', () => {
        const user: User = { id: 1 } as User;
        const users: User[] = [user];
        authenticationServiceSpy.getLoggedInUser.mockReturnValue(user);
        expect(service.isStarredByMe(users)).toBe(true);

        authenticationServiceSpy.getLoggedInUser.mockReturnValue({ ...user, id: 2 });
        expect(service.isStarredByMe(users)).toBe(false);
    });

    it('should return true for System Admin', () => {
        const user: User = { id: 2, role: 'systemadmin' } as User; // Not the creator
        authenticationServiceSpy.getLoggedInUser.mockReturnValue(user);

        expect(service.canChangeJobName(job)).toBe(true);
    });

    it('should return true for job creator', () => {
        const user: User = { id: 1, role: 'systemadmin' } as User;
        authenticationServiceSpy.getLoggedInUser.mockReturnValue(user);

        expect(service.canChangeJobName(job)).toBe(true);
    });

    it('should return false for a user who is not the creator and not a system admin', () => {
        const user: User = { id: 2, role: 'admin' } as User; // Not the creator or a System Admin
        authenticationServiceSpy.getLoggedInUser.mockReturnValue(user);

        expect(service.canChangeJobName(job)).toBe(false);
    });

});
