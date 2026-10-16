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
import {HttpClientTestingModule, HttpTestingController} from '@angular/common/http/testing';
import {LoginComponent} from './login.component';
import {ReactiveFormsModule, UntypedFormBuilder} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {AuthenticationService} from "../../services/authentication.service";
import {of} from "rxjs";
import {environment} from "../../../environments/environment";
import {TcInstanceType} from "../../model/tc-instance-type";

import {Directive, Input} from '@angular/core';
import {config_test} from "../../../config-test";

@Directive({
  selector: "[routerLink]", // Stub directive for routerLink
})
export class RouterLinkStubDirective {
  @Input('routerLink') linkParams: any;
}

describe('LoginComponent', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;
  let authenticationService: AuthenticationService;
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        HttpClientTestingModule,
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
        { provide: Router, useValue: { navigateByUrl: jasmine.createSpy('navigateByUrl') } },
      ]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    authenticationService = TestBed.inject(AuthenticationService);
    httpMock = TestBed.inject(HttpTestingController);
    fixture.detectChanges(); // Trigger change detection

    // ngOnInit fetches the instance type - resolve it with a default so tests that don't
    // care about GRN/TBB branding aren't left with a dangling request.
    httpMock.expectOne(`${environment.apiUrl}/auth/instance-type`)
      .flush({tcInstanceType: TcInstanceType.TBB});
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
    const loginSpy = spyOn(component, 'login').and.callThrough();
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
    const loginSpy = spyOn(authenticationService, 'login').and.callThrough();
    // Set up invalid form data (empty username)
    component.loginForm.patchValue({
      username: '',  // Empty username (required field)
      password: config_test.credentials.password,  // Empty password (required field)
      totpToken: config_test.credentials.totpToken
    });
    // Simulate form submission
    const form = fixture.nativeElement.querySelector('form');
    form.dispatchEvent(new Event('submit'));
    tick(); // Simulate async operations
    // Expect login function not to have been called
    expect(loginSpy).not.toHaveBeenCalled();
  }));
  it('should not render the logo until the instance type resolves', () => {
    const newFixture = TestBed.createComponent(LoginComponent);
    newFixture.detectChanges();

    expect(newFixture.nativeElement.querySelector('img.login-logo')).toBeNull();

    httpMock.expectOne(`${environment.apiUrl}/auth/instance-type`)
      .flush({tcInstanceType: TcInstanceType.GRN});
    newFixture.detectChanges();

    expect(newFixture.nativeElement.querySelector('img.login-logo')).toBeTruthy();
    newFixture.destroy();
  });

  it('should display the GRN logo when the instance type is GRN', () => {
    const newFixture = TestBed.createComponent(LoginComponent);
    const newComponent = newFixture.componentInstance;
    newFixture.detectChanges();

    httpMock.expectOne(`${environment.apiUrl}/auth/instance-type`)
      .flush({tcInstanceType: TcInstanceType.GRN});
    newFixture.detectChanges();

    expect(newComponent.loginImage).toContain('grnLogoDark.svg');
    newFixture.destroy();
  });

  it('should display the TBB logo when the instance type is TBB', () => {
    const newFixture = TestBed.createComponent(LoginComponent);
    const newComponent = newFixture.componentInstance;
    newFixture.detectChanges();

    httpMock.expectOne(`${environment.apiUrl}/auth/instance-type`)
      .flush({tcInstanceType: TcInstanceType.TBB});
    newFixture.detectChanges();

    expect(newComponent.loginImage).toContain('tcHorizontalLogo.png');
    newFixture.destroy();
  });

  afterEach(() => {
    fixture.destroy();
  });
});
