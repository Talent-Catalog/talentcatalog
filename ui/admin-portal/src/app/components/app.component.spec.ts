/*
 * Copyright (c) 2021 Talent Beyond Boundaries.
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
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { AppComponent } from './app.component';
import { HttpClientTestingModule } from "@angular/common/http/testing";
import { LocalStorageModule } from "angular-2-local-storage";
 import { AuthenticationService } from "../services/authentication.service";
import { ActivatedRoute, Router } from "@angular/router";
 import { of, Subject } from "rxjs";
import { User } from "../model/user";

// Stub class for AuthenticationService
class AuthenticationServiceStub {
  loggedInUser$ = new Subject<User>();

  // Method to set the logged-in user
  setLoggedInUser(user: User) {
    this.loggedInUser$.next(user);
  }
}

// Test suite declaration
fdescribe('AppComponent', () => {
  let component: AppComponent;
  let fixture: ComponentFixture<AppComponent>;
  let authService: AuthenticationServiceStub;

  // Async setup before each test
  beforeEach(waitForAsync(() => {
    // Creating a spy object for AuthenticationService
    const authServiceSpy = jasmine.createSpyObj('AuthenticationService', ['loggedInUser$']);

    // TestBed configuration
    TestBed.configureTestingModule({
      imports: [
        HttpClientTestingModule,
        LocalStorageModule.forRoot({}),
        RouterTestingModule // Add RouterTestingModule to imports
      ],
      declarations: [AppComponent],
      providers: [
        { provide: AuthenticationService, useClass: AuthenticationServiceStub }, // Provide stubbed AuthenticationService
        { provide: Router, useClass: class { navigate = jasmine.createSpy('navigate'); events = of(); } }, // Mock Router
        { provide: ActivatedRoute, useValue: { queryParams: of({}), snapshot: { data: {} } } }, // Mock ActivatedRoute
      ]
    }).compileComponents();

    // Injecting AuthenticationServiceStub into the test suite
    authService = TestBed.inject(AuthenticationService) as AuthenticationServiceStub;
  }));

  // Setup before each test
  beforeEach(() => {
    fixture = TestBed.createComponent(AppComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  // Test case: should create the app
  it('should create the app', () => {
    expect(component).toBeTruthy();
  });

  // Test case: should set showHeader to true when a user is logged in
  it('should set showHeader to true when a user is logged in', () => {
    /* mock user object */
    const user: User = {
      approver: undefined,
      createdBy: undefined,
      createdDate: Date.now(),
      email: "test@gmail.com",
      firstName: "",
      id: 2131,
      jobCreator: false,
      lastLogin: Date.now(),
      lastName: "Ahmady",
      mfaConfigured: false,
      name: "",
      partner: undefined,
      purpose: "",
      readOnly: false,
      role: "",
      sourceCountries: [],
      status: "",
      updatedDate: 0,
      username: "test@gmail.com",
      usingMfa: false  };
    authService.setLoggedInUser(user);
    expect(component.showHeader).toBeTrue();
  });

  // Test case: should set showHeader to false when no user is logged in
  it('should set showHeader to false when no user is logged in', () => {
    authService.setLoggedInUser(null);
    expect(component.showHeader).toBeFalse();
  });
});
