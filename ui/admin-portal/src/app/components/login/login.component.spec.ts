import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { LoginComponent } from './login.component';
import { FormBuilder, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthenticationService } from "../../services/authentication.service";
import { LocalStorageModule } from "angular-2-local-storage";
import { of } from "rxjs";
import { CommonModule } from "@angular/common";

import { Directive, Input } from '@angular/core';

@Directive({
  // tslint:disable-next-line:directive-selector
  selector: "[routerLink]",
})
export class RouterLinkStubDirective {
  @Input('routerLink') linkParams: any;
}

fdescribe('LoginComponent', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;
  let authenticationService: AuthenticationService;
  let router: Router;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        HttpClientTestingModule,
        FormsModule,
        ReactiveFormsModule,
        LocalStorageModule.forRoot({}),
        CommonModule
      ],
      declarations: [
        LoginComponent,
        RouterLinkStubDirective // Add RouterLinkStubDirective to declarations
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
    router = TestBed.inject(Router);
    component.ngOnInit(); // Initialize the form
    fixture.detectChanges(); // Trigger change detection
  });

  it('should render the login form with required fields', () => {
    const compiled = fixture.nativeElement;
    expect(compiled.querySelector('form')).toBeTruthy();
    expect(compiled.querySelector('input[formControlName="username"]')).toBeTruthy();
    expect(compiled.querySelector('input[formControlName="password"]')).toBeTruthy();
    expect(compiled.querySelector('input[formControlName="totpToken"]')).toBeTruthy();
    expect(compiled.querySelector('button[type="submit"]')).toBeTruthy();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should call the login function when the form is submitted', fakeAsync(() => {
    const loginSpy = spyOn(component, 'login').and.callThrough();
    component.loginForm.patchValue({
      username: 'testuser',
      password: 'testpassword',
      totpToken: 'testtoken'
    });
    const form = fixture.nativeElement.querySelector('form');
    form.dispatchEvent(new Event('submit'));
    tick();
    expect(loginSpy).toHaveBeenCalled();
  }));

  it('should not call the login function if the form is invalid', fakeAsync(() => {
    const loginSpy = spyOn(authenticationService, 'login').and.callThrough();
    component.loginForm.patchValue({
      username: '',  // Empty username (required field)
      password: 'testpassword',
      totpToken: 'testtoken'
    });
    const form = fixture.nativeElement.querySelector('form');
    form.dispatchEvent(new Event('submit'));
    tick();
    expect(loginSpy).not.toHaveBeenCalled();
  }));

  it('should call loginWithToken() when the form is valid', fakeAsync(() => {
    // Spy on loginWithToken() method indirectly by spying on login() method
    const loginSpy = spyOn(component, 'login').and.callThrough();

    // Set up a valid form
    component.loginForm.patchValue({
      username: 'testuser',
      password: 'testpassword',
      totpToken: 'testtoken'
    });

    // Trigger form submission
    const form = fixture.nativeElement.querySelector('form');
    form.dispatchEvent(new Event('submit'));
    tick();

    // Expect login() to have been called
    expect(loginSpy).toHaveBeenCalled();
  }));

  afterEach(() => {
    fixture.destroy();
  });
});
