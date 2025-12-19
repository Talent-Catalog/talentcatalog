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

import {CreateUpdateUserComponent} from "./create-update-user.component";
import {ComponentFixture, TestBed, waitForAsync} from "@angular/core/testing";
import {UserService} from "../../../../services/user.service";
import {AuthorizationService} from "../../../../services/authorization.service";
import {CountryService} from "../../../../services/country.service";
import {PartnerService} from "../../../../services/partner.service";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {NO_ERRORS_SCHEMA} from "@angular/core";
import {of, throwError} from "rxjs";
import {MockUser} from "../../../../MockData/MockUser";
import {SearchUserRequest} from "../../../../model/base";
import {Role} from "../../../../model/user";
import {config_test} from "../../../../../config-test";
import {AuthenticationService} from "../../../../services/authentication.service";

describe('CreateUpdateUserComponent', () => {
  let component: CreateUpdateUserComponent;
  let fixture: ComponentFixture<CreateUpdateUserComponent>;
  let userServiceSpy: jasmine.SpyObj<UserService>;
  let authenticationServiceSpy: jasmine.SpyObj<AuthenticationService>;
  let authServiceSpy: jasmine.SpyObj<AuthorizationService>;
  let countryServiceSpy: jasmine.SpyObj<CountryService>;
  let partnerServiceSpy: jasmine.SpyObj<PartnerService>;
  const mockUser = new MockUser();
  const updatedMockUser = mockUser;
  const mockSearchUserReq:SearchUserRequest = {
    partnerId: mockUser.partner.id,
    role: Role.systemadmin
  }
  beforeEach(waitForAsync(() => {
    const userServiceSpyObj = jasmine.createSpyObj('UserService', ['create', 'update', 'search']);
    const authenticationServiceSpyObj = jasmine.createSpyObj('AuthenticationService', ['getLoggedInUser']);
    const authServiceSpyObj = jasmine.createSpyObj('AuthorizationService', ['getLoggedInRole', 'canAssignPartner']);
    const countryServiceSpyObj = jasmine.createSpyObj('CountryService', ['listCountriesRestricted']);
    const partnerServiceSpyObj = jasmine.createSpyObj('PartnerService', ['listPartners']);

    TestBed.configureTestingModule({
      declarations: [CreateUpdateUserComponent],
      imports: [FormsModule,ReactiveFormsModule,NgSelectModule,HttpClientTestingModule],
      providers: [
        NgbActiveModal,
        { provide: UserService, useValue: userServiceSpyObj },
        { provide: AuthenticationService, useValue: authenticationServiceSpyObj },
        { provide: AuthorizationService, useValue: authServiceSpyObj },
        { provide: CountryService, useValue: countryServiceSpyObj },
        { provide: PartnerService, useValue: partnerServiceSpyObj }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    userServiceSpy = TestBed.inject(UserService) as jasmine.SpyObj<UserService>;
    authenticationServiceSpy = TestBed.inject(AuthenticationService) as jasmine.SpyObj<AuthenticationService>;
    authServiceSpy = TestBed.inject(AuthorizationService) as jasmine.SpyObj<AuthorizationService>;
    countryServiceSpy = TestBed.inject(CountryService) as jasmine.SpyObj<CountryService>;
    partnerServiceSpy = TestBed.inject(PartnerService) as jasmine.SpyObj<PartnerService>;

    userServiceSpy.create.and.returnValue(of(mockUser));
    userServiceSpy.update.and.returnValue(of(updatedMockUser));
    userServiceSpy.search.and.returnValue(of([mockUser]));

    authServiceSpy.getLoggedInRole.and.returnValue(mockSearchUserReq.role);
    authServiceSpy.canAssignPartner.and.returnValue(true);

    authenticationServiceSpy.getLoggedInUser.and.returnValue(mockUser);

    countryServiceSpy.listCountriesRestricted.and.returnValue(of([]));
    partnerServiceSpy.listPartners.and.returnValue(of([]));
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
    userServiceSpy.create.and.returnValue(throwError('error'));
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
    authServiceSpy.getLoggedInRole.and.returnValue(Role.admin);
    component.ngOnInit();
    expect(component.roleOptions.some(role => role.key === 'systemadmin')).toBeFalse();
  });

  it('should correctly identify if the component is in create mode', () => {
    component.user = null; // Ensure it's a new user
    expect(component.create).toBeTrue();
    component.user = mockUser;
    expect(component.create).toBeFalse();
  });

  it('should close the modal on successful save', () => {
    const activeModal = TestBed.inject(NgbActiveModal);
    spyOn(activeModal, 'close');
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
    userServiceSpy.search.and.returnValue(of([mockUser]));
    component.ngOnInit();
    expect(component.approvers.length).toBe(1);
    expect(component.approvers[0].firstName).toBe('John');
  });
});
