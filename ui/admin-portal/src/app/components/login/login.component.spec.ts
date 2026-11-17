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

import {ComponentFixture, fakeAsync, TestBed, tick} from '@angular/core/testing';
import {provideHttpClientTesting} from '@angular/common/http/testing';
import {provideHttpClient, withInterceptorsFromDi, withXhr} from '@angular/common/http';
import {LoginComponent} from './login.component';
import {ReactiveFormsModule, UntypedFormBuilder} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {AuthenticationService} from "../../services/authentication.service";
import {of} from "rxjs";

import {Directive, Input} from '@angular/core';
import {config_test} from "../../../config-test";
import {expect, vi} from 'vitest';

@Directive({
    selector: "[routerLink]", // Stub directive for routerLink
    standalone: false
})
export class RouterLinkStubDirective {
    @Input('routerLink')
    linkParams: any;
}

describe('LoginComponent', () => {
    let component: LoginComponent;
    let fixture: ComponentFixture<LoginComponent>;
    let authenticationService: AuthenticationService;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [
                ReactiveFormsModule
            ],
            declarations: [
                LoginComponent,
                RouterLinkStubDirective // Stub directive for routerLink
            ],
            providers: [
                AuthenticationService,
                UntypedFormBuilder,
                { provide: ActivatedRoute, useValue: { queryParams: of({}) } },
                { provide: Router, useValue: { navigateByUrl: vi.fn().mockName('navigateByUrl') } },
                provideHttpClient(withXhr(), withInterceptorsFromDi()),
                provideHttpClientTesting(),
            ]
        }).compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(LoginComponent);
        component = fixture.componentInstance;
        authenticationService = TestBed.inject(AuthenticationService);
        fixture.detectChanges(); // Trigger change detection
    });

    it('should create', () => {
        // Assert component creation
        expect(component).toBeTruthy();
    });

    it('should render the login form with required fields', () => {
        const compiled = fixture.nativeElement;
        // Assert login form and its required fields are rendered
        expect(compiled.querySelector('form')).toBeTruthy();
        expect(compiled.querySelector('tc-input[formControlName="username"]')).toBeTruthy();
        expect(compiled.querySelector('tc-input[formControlName="password"]')).toBeTruthy();
        expect(compiled.querySelector('tc-input[formControlName="totpToken"]')).toBeTruthy();
        expect(compiled.querySelector('tc-button')).toBeTruthy();
    });

    it('should call the login function when the form is submitted', fakeAsync(() => {
        const loginSpy = vi.spyOn(component, 'login');
        // Set up valid form data
        component.loginForm.patchValue({
            username: config_test.credentials.username,
            password: config_test.credentials.password,
            totpToken: config_test.credentials.totpToken
        });
        // Simulate form submission
        const form = fixture.nativeElement.querySelector('form');
        form.dispatchEvent(new Event('submit'));
        tick(); // Simulate async operations
        // Expect login function to have been called
        expect(loginSpy).toHaveBeenCalled();
    }));

    it('should not call the login function if the form is invalid', fakeAsync(() => {
        const loginSpy = vi.spyOn(authenticationService, 'login');
        // Set up invalid form data (empty username)
        component.loginForm.patchValue({
            username: '', // Empty username (required field)
            password: config_test.credentials.password, // Empty password (required field)
            totpToken: config_test.credentials.totpToken
        });
        // Simulate form submission
        const form = fixture.nativeElement.querySelector('form');
        form.dispatchEvent(new Event('submit'));
        tick(); // Simulate async operations
        // Expect login function not to have been called
        expect(loginSpy).not.toHaveBeenCalled();
    }));
    afterEach(() => {
        fixture.destroy();
    });
});

// ADMIN_VITEST_LOGIN_SUITE_RESTORE_20261117_V2
// An independent, top-level suite is required: an empty describe() is not a test.
// No network calls or app router are used in this unit-test fixture.
import {LoginComponent as RestoredLoginComponent20261117} from './login.component';
import {UntypedFormBuilder as RestoredLoginFormBuilder20261117} from '@angular/forms';
import {of as restoredLoginOf20261117, throwError as restoredLoginThrowError20261117} from 'rxjs';
import {vi as restoredLoginVi20261117} from 'vitest';

describe('LoginComponent (restored isolated behavior)', () => {
  function createLogin() {
    const auth = {
      login: restoredLoginVi20261117.fn(() => restoredLoginOf20261117(undefined)),
      getLoggedInUser: restoredLoginVi20261117.fn(() => ({ usingMfa: false, mfaConfigured: true }))
    };
    const router = { navigateByUrl: restoredLoginVi20261117.fn() };

    // Set only the constructor-injected collaborators used by these unit tests.
    // Avoid TestBed and any real HTTP, authentication, router, or modal calls.
    const component = Object.create(RestoredLoginComponent20261117.prototype) as RestoredLoginComponent20261117;
    Object.assign(component as unknown as Record<string, unknown>, {
      builder: new RestoredLoginFormBuilder20261117(),
      authenticationService: auth,
      route: { queryParams: restoredLoginOf20261117({ returnUrl: '/candidate/42' }) },
      router
    });
    component.ngOnInit();
    return { component, auth, router };
  }

  it('initializes required username and password fields', () => {
    const { component } = createLogin();
    expect(component.loginForm.get('username')?.hasError('required')).toBe(true);
    expect(component.loginForm.get('password')?.hasError('required')).toBe(true);
    expect(component.returnUrl).toBe('/candidate/42');
  });

  it('does not authenticate an invalid login form', () => {
    const { component, auth } = createLogin();
    component.login();
    expect(auth.login).not.toHaveBeenCalled();
    expect(component.loading).not.toBe(true);
  });

  it('authenticates a valid form and navigates after success', () => {
    const { component, auth, router } = createLogin();
    component.loginForm.setValue({
      username: 'valid.user', password: 'test-password', totpToken: ''
    });
    component.login();
    expect(auth.login).toHaveBeenCalledTimes(1);
    expect(router.navigateByUrl).toHaveBeenCalledWith('/candidate/42');
    expect(component.loading).toBe(false);
  });

  it('shows an authentication error without navigating', () => {
    const { component, auth, router } = createLogin();
    auth.login.mockReturnValue(restoredLoginThrowError20261117(() => new Error('Invalid credentials')));
    component.loginForm.setValue({
      username: 'valid.user', password: 'incorrect-password', totpToken: ''
    });
    component.login();
    expect((component.error as Error).message).toBe('Invalid credentials');
    expect(router.navigateByUrl).not.toHaveBeenCalled();
    expect(component.loading).toBe(false);
  });
});
