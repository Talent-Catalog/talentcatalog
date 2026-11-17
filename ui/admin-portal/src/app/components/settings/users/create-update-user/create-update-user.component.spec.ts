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
import {CreateUpdateUserComponent} from "./create-update-user.component";
import {ComponentFixture, TestBed, waitForAsync} from "@angular/core/testing";
import {UserService} from "../../../../services/user.service";
import {AuthorizationService} from "../../../../services/authorization.service";
import {CountryService} from "../../../../services/country.service";
import {PartnerService} from "../../../../services/partner.service";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";
import {provideHttpClientTesting} from "@angular/common/http/testing";
import {provideHttpClient, withInterceptorsFromDi, withXhr} from "@angular/common/http";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {NO_ERRORS_SCHEMA} from "@angular/core";
import {of, throwError} from "rxjs";
import {MockUser} from "../../../../MockData/MockUser";
import {SearchUserRequest} from "../../../../model/base";
import {Role} from "../../../../model/user";
import {config_test} from "../../../../../config-test";
import {AuthenticationService} from "../../../../services/authentication.service";
import {expect, vi} from 'vitest';

describe('CreateUpdateUserComponent', () => {
    let component: CreateUpdateUserComponent;
    let fixture: ComponentFixture<CreateUpdateUserComponent>;
    let userServiceSpy: MockedObject<UserService>;
    let authenticationServiceSpy: MockedObject<AuthenticationService>;
    let authServiceSpy: MockedObject<AuthorizationService>;
    let countryServiceSpy: MockedObject<CountryService>;
    let partnerServiceSpy: MockedObject<PartnerService>;
    const mockUser = new MockUser();
    const updatedMockUser = mockUser;
    const mockSearchUserReq: SearchUserRequest = {
        partnerId: mockUser.partner.id,
        role: Role.systemadmin
    };
    beforeEach(waitForAsync(() => {
        const userServiceSpyObj = {
            create: vi.fn().mockName("UserService.create"),
            update: vi.fn().mockName("UserService.update"),
            search: vi.fn().mockName("UserService.search")
        };
        const authenticationServiceSpyObj = {
            getLoggedInUser: vi.fn().mockName("AuthenticationService.getLoggedInUser")
        };
        const authServiceSpyObj = {
            getLoggedInRole: vi.fn().mockName("AuthorizationService.getLoggedInRole"),
            canAssignPartner: vi.fn().mockName("AuthorizationService.canAssignPartner")
        };
        const countryServiceSpyObj = {
            listCountriesRestricted: vi.fn().mockName("CountryService.listCountriesRestricted")
        };
        const partnerServiceSpyObj = {
            listPartners: vi.fn().mockName("PartnerService.listPartners")
        };

        TestBed.configureTestingModule({
            declarations: [CreateUpdateUserComponent],
            imports: [FormsModule, ReactiveFormsModule, NgSelectModule],
            providers: [
                NgbActiveModal,
                { provide: UserService, useValue: userServiceSpyObj },
                { provide: AuthenticationService, useValue: authenticationServiceSpyObj },
                { provide: AuthorizationService, useValue: authServiceSpyObj },
                { provide: CountryService, useValue: countryServiceSpyObj },
                { provide: PartnerService, useValue: partnerServiceSpyObj },
                provideHttpClient(withXhr(), withInterceptorsFromDi()),
                provideHttpClientTesting()
            ],
            schemas: [NO_ERRORS_SCHEMA]
        }).compileComponents();

        userServiceSpy = TestBed.inject(UserService) as MockedObject<UserService>;
        authenticationServiceSpy = TestBed.inject(AuthenticationService) as MockedObject<AuthenticationService>;
        authServiceSpy = TestBed.inject(AuthorizationService) as MockedObject<AuthorizationService>;
        countryServiceSpy = TestBed.inject(CountryService) as MockedObject<CountryService>;
        partnerServiceSpy = TestBed.inject(PartnerService) as MockedObject<PartnerService>;

        userServiceSpy.create.mockReturnValue(of(mockUser));
        userServiceSpy.update.mockReturnValue(of(updatedMockUser));
        userServiceSpy.search.mockReturnValue(of([mockUser]));

        authServiceSpy.getLoggedInRole.mockReturnValue(mockSearchUserReq.role);
        authServiceSpy.canAssignPartner.mockReturnValue(true);

        authenticationServiceSpy.getLoggedInUser.mockReturnValue(mockUser);

        countryServiceSpy.listCountriesRestricted.mockReturnValue(of([]));
        partnerServiceSpy.listPartners.mockReturnValue(of([]));
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(CreateUpdateUserComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should initialize form with user data if user is provided', () => {
        component.user = mockUser;
        component.ngOnInit();
        expect(component.userForm.value.email).toBe('john.doe@example.com');
        expect(component.userForm.value.username).toBe('mockuser');
        expect(component.userForm.value.firstName).toBe('John');
        expect(component.userForm.value.lastName).toBe('Doe');
    });

    it('should call create method on save for new user', () => {
        component.user = null; // Ensure it's a new user
        component.ngOnInit();
        component.userForm.setValue({
            email: config_test.credentials.email,
            username: config_test.credentials.username,
            firstName: 'New',
            lastName: 'User',
            partnerId: 1,
            status: 'active',
            role: 'user',
            jobCreator: false,
            approverId: null,
            purpose: '',
            sourceCountries: [],
            readOnly: false,
            usingMfa: true,
            password: config_test.credentials.password
        });
        component.onSave();
        expect(userServiceSpy.create).toHaveBeenCalled();
    });

    it('should call update method on save for existing user', () => {
        component.user = updatedMockUser;
        component.ngOnInit();
        component.onSave();
        expect(userServiceSpy.update).toHaveBeenCalled();
    });

    it('should display an error message on save failure', () => {
        userServiceSpy.create.mockReturnValue(throwError('error'));
        component.user = null; // Ensure it's a new user
        component.ngOnInit();
        component.userForm.setValue({
            email: config_test.credentials.email,
            username: config_test.credentials.username,
            firstName: 'New',
            lastName: 'User',
            partnerId: 1,
            status: 'active',
            role: 'user',
            jobCreator: false,
            approverId: null,
            purpose: '',
            sourceCountries: [],
            readOnly: false,
            usingMfa: true,
            password: config_test.credentials.password
        });
        component.onSave();
        expect(component.error).toBe('error');
    });

    it('should filter role options based on logged-in role', () => {
        authServiceSpy.getLoggedInRole.mockReturnValue(Role.admin);
        component.ngOnInit();
        expect(component.roleOptions.some(role => role.key === 'systemadmin')).toBe(false);
    });

    it('should correctly identify if the component is in create mode', () => {
        component.user = null; // Ensure it's a new user
        expect(component.create).toBe(true);
        component.user = mockUser;
        expect(component.create).toBe(false);
    });

    it('should close the modal on successful save', () => {
        const activeModal = TestBed.inject(NgbActiveModal);
        vi.spyOn(activeModal, 'close').mockReturnValue(undefined);
        component.user = null; // Ensure it's a new user
        component.ngOnInit();
        component.userForm.setValue({
            email: config_test.credentials.email,
            username: config_test.credentials.username,
            firstName: 'New',
            lastName: 'User',
            partnerId: 1,
            status: 'active',
            role: 'user',
            jobCreator: false,
            approverId: null,
            purpose: '',
            sourceCountries: [],
            readOnly: false,
            usingMfa: true,
            password: config_test.credentials.password
        });
        component.onSave();
        expect(activeModal.close).toHaveBeenCalled();
    });

    it('should populate form with approvers on init', () => {
        userServiceSpy.search.mockReturnValue(of([mockUser]));
        component.ngOnInit();
        expect(component.approvers.length).toBe(1);
        expect(component.approvers[0].firstName).toBe('John');
    });
});
