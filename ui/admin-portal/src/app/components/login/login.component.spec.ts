import {ComponentFixture, fakeAsync, TestBed, tick} from '@angular/core/testing';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {LoginComponent} from './login.component';
import {FormBuilder, ReactiveFormsModule} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {AuthenticationService} from "../../services/authentication.service";
import {LocalStorageModule} from "angular-2-local-storage";
import {of} from "rxjs";

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

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        HttpClientTestingModule,
        ReactiveFormsModule,
        LocalStorageModule.forRoot({}),
      ],
      declarations: [
        LoginComponent,
        RouterLinkStubDirective // Stub directive for routerLink
      ],
      providers: [
        AuthenticationService,
        FormBuilder,
        { provide: ActivatedRoute, useValue: { queryParams: of({}) } },
        { provide: Router, useValue: { navigateByUrl: jasmine.createSpy('navigateByUrl') } },
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
    expect(compiled.querySelector('input[formControlName="username"]')).toBeTruthy();
    expect(compiled.querySelector('input[formControlName="password"]')).toBeTruthy();
    expect(compiled.querySelector('input[formControlName="totpToken"]')).toBeTruthy();
    expect(compiled.querySelector('button[type="submit"]')).toBeTruthy();
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
  afterEach(() => {
    fixture.destroy();
  });
});
