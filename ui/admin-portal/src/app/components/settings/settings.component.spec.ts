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
import {SettingsComponent} from "./settings.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {AuthorizationService} from "../../services/authorization.service";
import {AuthenticationService} from "../../services/authentication.service";
import {NgbModule, NgbNavChangeEvent} from "@ng-bootstrap/ng-bootstrap";
import {MockUser} from "../../MockData/MockUser";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {SearchUsersComponent} from "./users/search-users.component";
import {LocalStorageService} from "../../services/local-storage.service";

describe('SettingsComponent', () => {
  let component: SettingsComponent;
  let fixture: ComponentFixture<SettingsComponent>;
  let authService: jasmine.SpyObj<AuthorizationService>;
  let authSpy: jasmine.SpyObj<AuthorizationService>;
  let authenticationService: jasmine.SpyObj<AuthenticationService>;
  let localStorageService: jasmine.SpyObj<LocalStorageService>;

  beforeEach(async () => {
    authSpy = jasmine.createSpyObj('AuthorizationService', ['isSystemAdminOnly','isDefaultPartner','isAnAdmin', 'canManageCandidateTasks','getLoggedInRole']);
    const authMock = jasmine.createSpyObj('AuthenticationService', ['getLoggedInUser']);
    const localStorageMock = jasmine.createSpyObj('LocalStorageService', ['get', 'set']);

    await TestBed.configureTestingModule({
      imports: [
        NgbModule,  // Make sure NgbModule is imported
        FormsModule,
        ReactiveFormsModule,
        NgSelectModule,
        HttpClientTestingModule,
      ],
      declarations: [SettingsComponent,SearchUsersComponent],
      providers: [

        { provide: AuthorizationService, useValue: authSpy },
        { provide: AuthenticationService, useValue: authMock },
        { provide: LocalStorageService, useValue: localStorageMock },

      ]
    }).compileComponents();

    authService = TestBed.inject(AuthorizationService) as jasmine.SpyObj<AuthorizationService>;
    authenticationService = TestBed.inject(AuthenticationService) as jasmine.SpyObj<AuthenticationService>;
    localStorageService = TestBed.inject(LocalStorageService) as jasmine.SpyObj<LocalStorageService>;
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SettingsComponent);
    component = fixture.componentInstance;
    component.loggedInUser = new MockUser();
    authenticationService.getLoggedInUser.and.returnValue(new MockUser());
    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should get the logged in user on init', () => {
    expect(component.loggedInUser.username).toBe('mockuser');
  });

  it('should save the selected tab in local storage on tab change', () => {
    component.onTabChanged({ nextId: 'languages' } as NgbNavChangeEvent);
    expect(localStorageService.set).toHaveBeenCalledWith('SettingsLastTab', 'languages');
  });

  it('should check if the user is system admin', () => {
    authService.isSystemAdminOnly.and.returnValue(true);
    expect(component.systemAdminOnly()).toBeTrue();
  });

  it('should check if the user can see external links and tasks', () => {
    authService.canManageCandidateTasks.and.returnValue(true);
    expect(component.canSeeExternalLinksAndTasks()).toBeTrue();
  });
});
