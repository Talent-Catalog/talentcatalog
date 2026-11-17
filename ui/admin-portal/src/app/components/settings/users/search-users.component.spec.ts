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
import {SearchUsersComponent} from "./search-users.component";
import {ComponentFixture, TestBed, waitForAsync} from "@angular/core/testing";
import {UserService} from "../../../services/user.service";
import {AuthorizationService} from "../../../services/authorization.service";
import {AuthenticationService} from "../../../services/authentication.service";
import {NgbModal, NgbModalRef, NgbPaginationModule} from "@ng-bootstrap/ng-bootstrap";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";
import {provideHttpClientTesting} from "@angular/common/http/testing";
import {provideHttpClient, withInterceptorsFromDi, withXhr} from "@angular/common/http";
import {of, throwError} from "rxjs";
import {Role, User} from "../../../model/user";
import {CreateUpdateUserComponent} from "./create-update-user/create-update-user.component";
import {ConfirmationComponent} from "../../util/confirm/confirmation.component";
import {ChangePasswordComponent} from "../../account/change-password/change-password.component";
import {SearchResults} from "../../../model/search-results";
import {MockUser} from "../../../MockData/MockUser";
import {expect, vi} from 'vitest';

describe('SearchUsersComponent', () => {
    let component: SearchUsersComponent;
    let fixture: ComponentFixture<SearchUsersComponent>;
    let userServiceSpy: MockedObject<UserService>;
    let authServiceSpy: MockedObject<AuthorizationService>;
    let authService: AuthenticationService;
    let modalService: NgbModal;
    const mockUser = new MockUser();
    const mockSearchResUser: SearchResults<User> = {
        number: 1,
        size: 1,
        totalElements: 1,
        totalPages: 1,
        first: false,
        last: false,
        content: [mockUser]
    };
    beforeEach(waitForAsync(() => {
        const userServiceSpyObj = {
            searchPaged: vi.fn().mockName("UserService.searchPaged"),
            delete: vi.fn().mockName("UserService.delete"),
            resetMfa: vi.fn().mockName("UserService.resetMfa")
        };
        const authorizeServiceSpyObj = {
            getLoggedInRole: vi.fn().mockName("AuthorizationService.getLoggedInRole"),
            isAnAdmin: vi.fn().mockName("AuthorizationService.isAnAdmin"),
            isDefaultPartner: vi.fn().mockName("AuthorizationService.isDefaultPartner")
        };
        const authServiceSpyObj = {
            getLoggedInUser: vi.fn().mockName("AuthenticationService.getLoggedInUser"),
            setLoggedInUser: vi.fn().mockName("AuthenticationService.setLoggedInUser")
        };

        TestBed.configureTestingModule({
            declarations: [SearchUsersComponent],
            imports: [FormsModule, ReactiveFormsModule, NgSelectModule, NgbPaginationModule],
            providers: [
                { provide: UserService, useValue: userServiceSpyObj },
                { provide: AuthorizationService, useValue: authorizeServiceSpyObj },
                { provide: AuthenticationService, useValue: authServiceSpyObj },
                NgbModal,
                provideHttpClient(withXhr(), withInterceptorsFromDi()),
                provideHttpClientTesting()
            ]
        }).compileComponents();

        userServiceSpy = TestBed.inject(UserService) as MockedObject<UserService>;
        authServiceSpy = TestBed.inject(AuthorizationService) as MockedObject<AuthorizationService>;
        authService = TestBed.inject(AuthenticationService) as MockedObject<AuthenticationService>;
        modalService = TestBed.inject(NgbModal);

        userServiceSpy.searchPaged.mockReturnValue(of(mockSearchResUser));
        userServiceSpy.delete.mockReturnValue(of(true));
        userServiceSpy.resetMfa.mockReturnValue(of());

        authServiceSpy.getLoggedInRole.mockReturnValue(Role.admin);
        authServiceSpy.isAnAdmin.mockReturnValue(true);
        authServiceSpy.isDefaultPartner.mockReturnValue(true);

    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(SearchUsersComponent);
        component = fixture.componentInstance;
        component.loggedInUser = mockUser;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should initialize form and perform initial search', () => {
        expect(component.searchForm).toBeDefined();
        expect(userServiceSpy.searchPaged).toHaveBeenCalled();
    });

    it('should search users when form value changes', () => {
        component.searchForm.patchValue({ keyword: 'test' });
        expect(userServiceSpy.searchPaged).toHaveBeenCalledTimes(1);
    });

    it('should call addUser and open CreateUpdateUserComponent modal', () => {
        vi.spyOn(modalService, 'open').mockReturnValue({
            componentInstance: {},
            result: Promise.resolve('some result'),
            close: () => { }, // Mock close method
        } as NgbModalRef);
        component.addUser();
        expect(modalService.open).toHaveBeenCalledWith(CreateUpdateUserComponent, { centered: true, backdrop: 'static' });
    });

    it('should call editUser and open CreateUpdateUserComponent modal', () => {
        const user = { id: 1 } as User;
        vi.spyOn(modalService, 'open').mockReturnValue({
            componentInstance: {},
            result: Promise.resolve('some result'),
            close: () => { }, // Mock close method
        } as NgbModalRef);
        component.editUser(user);
        expect(modalService.open).toHaveBeenCalledWith(CreateUpdateUserComponent, { centered: true, backdrop: 'static' });
    });

    it('should call deleteUser and open ConfirmationComponent modal', () => {
        const user = { id: 1 } as User;
        vi.spyOn(modalService, 'open').mockReturnValue({
            componentInstance: {},
            result: Promise.resolve('some result'),
            close: () => { }, // Mock close method
        } as NgbModalRef);
        component.deleteUser(user);
        expect(modalService.open).toHaveBeenCalledWith(ConfirmationComponent, { centered: true, backdrop: 'static' });
    });

    it('should call updatePassword and open ChangePasswordComponent modal', () => {
        const user = { id: 1 } as User;
        vi.spyOn(modalService, 'open').mockReturnValue({
            componentInstance: {},
            result: Promise.resolve('some result'),
            close: () => { }, // Mock close method
        } as NgbModalRef);
        component.updatePassword(user);
        expect(modalService.open).toHaveBeenCalledWith(ChangePasswordComponent, { centered: true, backdrop: 'static' });
    });

    it('should call resetAuthentication and update user MFA', () => {
        const user = { id: 1 } as User;
        component.resetAuthentication(user);
        expect(userServiceSpy.resetMfa).toHaveBeenCalledWith(user.id);
    });

    it('should return correct admin status', () => {
        expect(component.isAnAdmin()).toBe(true);
    });

    it('should return source countries as a string array', () => {
        const user = {
            sourceCountries: [{ name: 'Country1' }, { name: 'Country2' }]
        } as User;
        const result = component.getSourceCountries(user);
        expect(result).toEqual([' Country1', ' Country2']);
    });

    it('should correctly identify if a user can be edited', () => {
        const user = { id: 2, role: 'user', partner: { id: 1 } } as User;
        expect(component.canEdit(user)).toBe(true);
    });

    it('should handle error when resetMfa fails', () => {
        userServiceSpy.resetMfa.mockReturnValue(throwError('error'));
        const user = { id: 1 } as User;
        component.resetAuthentication(user);
        expect(component.error).toBe('error');
    });
});
