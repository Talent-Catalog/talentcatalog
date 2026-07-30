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
import {Component, forwardRef, Input, Pipe, PipeTransform} from '@angular/core';
import {ComponentFixture, fakeAsync, TestBed, tick} from '@angular/core/testing';
import {By} from '@angular/platform-browser';
import {
  ControlValueAccessor,
  NG_VALUE_ACCESSOR,
  ReactiveFormsModule,
  UntypedFormBuilder,
  Validators
} from '@angular/forms';
import {ActivatedRoute, convertToParamMap, ParamMap, Router} from '@angular/router';
import {BehaviorSubject, of, throwError} from 'rxjs';

import {ChangePasswordComponent} from './change-password.component';
import {UserService} from '../../../services/user.service';

@Pipe({name: 'translate'})
class TranslatePipeStub implements PipeTransform {
  transform(value: string): string {
    return value;
  }
}

@Component({selector: 'app-error', template: ''})
class AppErrorStubComponent {
  @Input() error: any;
}

@Component({selector: 'tc-loading', template: ''})
class TcLoadingStubComponent {
  @Input() loading: boolean;
}

@Component({
  selector: 'tc-alert',
  template: '<ng-content></ng-content>'
})
class TcAlertStubComponent {
  @Input() type: string;
}

@Component({
  selector: 'tc-field',
  template: '<ng-content></ng-content>'
})
class TcFieldStubComponent {
}

@Component({
  selector: 'tc-label',
  template: '<ng-content></ng-content>'
})
class TcLabelStubComponent {
  @Input() for: string;
}

@Component({
  selector: 'tc-input',
  template: '',
  providers: [{
    provide: NG_VALUE_ACCESSOR,
    useExisting: forwardRef(() => TcInputStubComponent),
    multi: true
  }]
})
class TcInputStubComponent implements ControlValueAccessor {
  @Input() id: string;
  @Input() type: string;

  writeValue(): void {
  }

  registerOnChange(): void {
  }

  registerOnTouched(): void {
  }

  setDisabledState(): void {
  }
}

@Component({
  selector: 'tc-button',
  template: '<ng-content></ng-content>'
})
class TcButtonStubComponent {
  @Input() disabled: boolean;
}

describe('ChangePasswordComponent', () => {
  let component: ChangePasswordComponent;
  let fixture: ComponentFixture<ChangePasswordComponent>;
  let formBuilder: UntypedFormBuilder;
  let userServiceSpy: jasmine.SpyObj<UserService>;
  let routerSpy: jasmine.SpyObj<Router>;
  let paramMapSubject: BehaviorSubject<ParamMap>;

  beforeEach(async () => {
    userServiceSpy = jasmine.createSpyObj<UserService>(
      'UserService',
      [
        'checkPasswordResetToken',
        'resetPassword',
        'updatePassword'
      ]
    );

    routerSpy = jasmine.createSpyObj<Router>(
      'Router',
      ['navigate']
    );

    paramMapSubject = new BehaviorSubject<ParamMap>(
      convertToParamMap({})
    );

    userServiceSpy.checkPasswordResetToken.and.returnValue(of(null));
    userServiceSpy.resetPassword.and.returnValue(of(null));
    userServiceSpy.updatePassword.and.returnValue(of(null));
    routerSpy.navigate.and.returnValue(Promise.resolve(true));

    await TestBed.configureTestingModule({
      imports: [ReactiveFormsModule],
      declarations: [
        ChangePasswordComponent,
        TranslatePipeStub,
        AppErrorStubComponent,
        TcLoadingStubComponent,
        TcAlertStubComponent,
        TcFieldStubComponent,
        TcLabelStubComponent,
        TcInputStubComponent,
        TcButtonStubComponent
      ],
      providers: [
        {
          provide: UserService,
          useValue: userServiceSpy
        },
        {
          provide: Router,
          useValue: routerSpy
        },
        {
          provide: ActivatedRoute,
          useValue: {
            paramMap: paramMapSubject.asObservable()
          }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ChangePasswordComponent);
    component = fixture.componentInstance;
    formBuilder = TestBed.inject(UntypedFormBuilder);
    fixture.detectChanges();
  });

  it('should render tc loading, three fields, and a disabled submit button in change-password mode', () => {
    const loading = fixture.debugElement.query(
      By.directive(TcLoadingStubComponent)
    ).componentInstance as TcLoadingStubComponent;

    const fields = fixture.debugElement.queryAll(
      By.directive(TcFieldStubComponent)
    );

    const button = fixture.debugElement.query(
      By.directive(TcButtonStubComponent)
    ).componentInstance as TcButtonStubComponent;

    expect(loading.loading).toBeFalse();
    expect(fields.length).toBe(3);
    expect(button.disabled).toBeTrue();
  });

  it('should hide the old password field in reset mode', () => {
    component.reset = true;
    component.form = formBuilder.group({
      token: ['token'],
      password: [''],
      passwordConfirmation: ['']
    });

    fixture.detectChanges();

    const inputs = fixture.debugElement.queryAll(
      By.directive(TcInputStubComponent)
    ).map(
      debugElement =>
        debugElement.componentInstance as TcInputStubComponent
    );

    expect(
      inputs.some(input => input.id === 'oldPassword')
    ).toBeFalse();

    expect(inputs.length).toBe(2);
  });

  it('should show a success tc-alert after the password is updated', () => {
    component.updated = true;
    fixture.detectChanges();

    const alert = fixture.debugElement.query(
      By.directive(TcAlertStubComponent)
    ).componentInstance as TcAlertStubComponent;

    expect(alert.type).toBe('success');
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize the default component state', () => {
    expect(component.loading).toBeFalse();
    expect(component.reset).toBeFalse();
    expect(component.error).toBeNull();
    expect(component.updated).toBeFalse();
    expect(component.tokenInvalid).toBeFalse();
  });

  it('should create a change-password form when no token exists', () => {
    expect(component.form).toBeDefined();
    expect(component.form.get('oldPassword')).not.toBeNull();
    expect(component.form.get('password')).not.toBeNull();
    expect(component.form.get('passwordConfirmation')).not.toBeNull();
    expect(component.form.get('token')).toBeNull();
    expect(component.form.invalid).toBeTrue();
  });

  it('should require all fields in change-password mode', () => {
    const oldPassword = component.form.get('oldPassword');
    const password = component.form.get('password');
    const passwordConfirmation = component.form.get(
      'passwordConfirmation'
    );

    expect(oldPassword.hasError('required')).toBeTrue();
    expect(password.hasError('required')).toBeTrue();
    expect(passwordConfirmation.hasError('required')).toBeTrue();

    oldPassword.setValue('old-password');
    password.setValue('new-password');
    passwordConfirmation.setValue('new-password');

    expect(component.form.valid).toBeTrue();
  });

  it('should validate a password-reset token', () => {
    userServiceSpy.checkPasswordResetToken.calls.reset();

    paramMapSubject.next(
      convertToParamMap({token: 'valid-token'})
    );

    expect(
      userServiceSpy.checkPasswordResetToken
    ).toHaveBeenCalledTimes(1);

    expect(
      userServiceSpy.checkPasswordResetToken
    ).toHaveBeenCalledWith({
      token: 'valid-token'
    });

    expect(component.token).toBe('valid-token');
  });

  it('should create a reset-password form when the token is valid', () => {
    paramMapSubject.next(
      convertToParamMap({token: 'valid-token'})
    );

    expect(component.loading).toBeFalse();
    expect(component.reset).toBeTrue();
    expect(component.tokenInvalid).toBeFalse();

    expect(component.form.get('token')).not.toBeNull();
    expect(component.form.get('oldPassword')).toBeNull();
    expect(component.form.get('password')).not.toBeNull();

    expect(
      component.form.get('passwordConfirmation')
    ).not.toBeNull();

    expect(component.form.get('token').value).toBe('valid-token');
    expect(component.form.invalid).toBeTrue();
  });

  it('should require the reset-password fields', () => {
    paramMapSubject.next(
      convertToParamMap({token: 'valid-token'})
    );

    const token = component.form.get('token');
    const password = component.form.get('password');
    const passwordConfirmation = component.form.get(
      'passwordConfirmation'
    );

    expect(token.hasError('required')).toBeFalse();
    expect(password.hasError('required')).toBeTrue();
    expect(passwordConfirmation.hasError('required')).toBeTrue();

    password.setValue('new-password');
    passwordConfirmation.setValue('new-password');

    expect(component.form.valid).toBeTrue();
  });

  it('should mark the token as invalid when token validation fails', () => {
    const validationError = {
      status: 400,
      message: 'Password-reset token is invalid'
    };

    userServiceSpy.checkPasswordResetToken.and.returnValue(
      throwError(validationError)
    );

    paramMapSubject.next(
      convertToParamMap({token: 'invalid-token'})
    );

    expect(component.error).toEqual(validationError);
    expect(component.tokenInvalid).toBeTrue();
    expect(component.reset).toBeFalse();
  });

  it('should hide the form when the token is invalid', () => {
    userServiceSpy.checkPasswordResetToken.and.returnValue(
      throwError('Invalid token')
    );

    paramMapSubject.next(
      convertToParamMap({token: 'invalid-token'})
    );

    fixture.detectChanges();

    expect(
      fixture.debugElement.query(By.css('form'))
    ).toBeNull();
  });

  it('should hide the form while loading', () => {
    component.loading = true;
    fixture.detectChanges();

    expect(
      fixture.debugElement.query(By.css('form'))
    ).toBeNull();
  });

  it('should pass loading state to the loading component', () => {
    component.loading = true;
    fixture.detectChanges();

    const loading = fixture.debugElement.query(
      By.directive(TcLoadingStubComponent)
    ).componentInstance as TcLoadingStubComponent;

    expect(loading.loading).toBeTrue();
  });

  it('should pass an error to the error component', () => {
    component.error = 'Unable to change password';
    fixture.detectChanges();

    const error = fixture.debugElement.query(
      By.directive(AppErrorStubComponent)
    ).componentInstance as AppErrorStubComponent;

    expect(error.error).toBe('Unable to change password');
  });

  it('should enable the button when the form is valid and dirty', () => {
    component.form.patchValue({
      oldPassword: 'old-password',
      password: 'new-password',
      passwordConfirmation: 'new-password'
    });

    component.form.markAsDirty();
    fixture.detectChanges();

    const button = fixture.debugElement.query(
      By.directive(TcButtonStubComponent)
    ).componentInstance as TcButtonStubComponent;

    expect(component.form.valid).toBeTrue();
    expect(component.form.dirty).toBeTrue();
    expect(button.disabled).toBeFalse();
  });

  it('should hide the submit button while loading', () => {
    component.form.patchValue({
      oldPassword: 'old-password',
      password: 'new-password',
      passwordConfirmation: 'new-password'
    });

    component.form.markAsDirty();
    component.loading = true;
    fixture.detectChanges();

    const button = fixture.debugElement.query(
      By.directive(TcButtonStubComponent)
    );

    expect(button).toBeNull();
  });

  it('should update a password in change-password mode', () => {
    const request = {
      oldPassword: 'old-password',
      password: 'new-password',
      passwordConfirmation: 'new-password'
    };

    component.reset = false;
    component.form.setValue(request);

    component.updatePassword();

    expect(userServiceSpy.updatePassword).toHaveBeenCalledTimes(1);
    expect(userServiceSpy.updatePassword).toHaveBeenCalledWith(request);
    expect(userServiceSpy.resetPassword).not.toHaveBeenCalled();
  });

  it('should reset the form and show success after changing a password', () => {
    const resetFormSpy = spyOn(
      component,
      'resetForm'
    ).and.callThrough();

    component.reset = false;

    component.form.setValue({
      oldPassword: 'old-password',
      password: 'new-password',
      passwordConfirmation: 'new-password'
    });

    component.form.markAsDirty();

    component.updatePassword();

    expect(resetFormSpy).toHaveBeenCalledTimes(1);
    expect(component.updated).toBeTrue();
    expect(component.error).toBeNull();

    expect(component.form.value).toEqual({
      oldPassword: '',
      password: '',
      passwordConfirmation: ''
    });

    expect(component.form.pristine).toBeTrue();
  });

  it('should store the error when changing a password fails', () => {
    const updateError = {
      status: 400,
      message: 'Current password is incorrect'
    };

    userServiceSpy.updatePassword.and.returnValue(
      throwError(updateError)
    );

    component.reset = false;

    component.form.setValue({
      oldPassword: 'incorrect-password',
      password: 'new-password',
      passwordConfirmation: 'new-password'
    });

    component.updated = true;
    component.error = 'Previous error';

    component.updatePassword();

    expect(component.updated).toBeFalse();
    expect(component.error).toEqual(updateError);
    expect(userServiceSpy.resetPassword).not.toHaveBeenCalled();
  });

  it('should reset a password in reset mode', fakeAsync(() => {
    const request = {
      token: 'valid-token',
      password: 'new-password',
      passwordConfirmation: 'new-password'
    };

    component.reset = true;

    component.form = formBuilder.group({
      token: [
        request.token,
        Validators.required
      ],
      password: [
        request.password,
        Validators.required
      ],
      passwordConfirmation: [
        request.passwordConfirmation,
        Validators.required
      ]
    });

    component.updatePassword();

    expect(userServiceSpy.resetPassword).toHaveBeenCalledTimes(1);
    expect(userServiceSpy.resetPassword).toHaveBeenCalledWith(request);
    expect(userServiceSpy.updatePassword).not.toHaveBeenCalled();

    expect(routerSpy.navigate).not.toHaveBeenCalled();

    tick(1999);
    expect(routerSpy.navigate).not.toHaveBeenCalled();

    tick(1);
    expect(routerSpy.navigate).toHaveBeenCalledTimes(1);
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/login']);
  }));

  it('should reset the form and show success after resetting a password', fakeAsync(() => {
    const resetFormSpy = spyOn(
      component,
      'resetForm'
    ).and.callThrough();

    component.reset = true;

    component.form = formBuilder.group({
      token: ['valid-token'],
      password: ['new-password'],
      passwordConfirmation: ['new-password']
    });

    component.form.markAsDirty();

    component.updatePassword();

    expect(resetFormSpy).toHaveBeenCalledTimes(1);
    expect(component.updated).toBeTrue();
    expect(component.error).toBeNull();

    expect(component.form.value).toEqual({
      token: '',
      password: '',
      passwordConfirmation: ''
    });

    expect(component.form.pristine).toBeTrue();

    tick(2000);
  }));

  it('should store the error when resetting a password fails', () => {
    const resetError = {
      status: 400,
      message: 'Password-reset token has expired'
    };

    userServiceSpy.resetPassword.and.returnValue(
      throwError(resetError)
    );

    component.reset = true;

    component.form = formBuilder.group({
      token: ['expired-token'],
      password: ['new-password'],
      passwordConfirmation: ['new-password']
    });

    component.updated = true;
    component.error = 'Previous error';

    component.updatePassword();

    expect(component.updated).toBeFalse();
    expect(component.error).toEqual(resetError);
    expect(routerSpy.navigate).not.toHaveBeenCalled();
    expect(userServiceSpy.updatePassword).not.toHaveBeenCalled();
  });

  it('should clear previous state before updating the password', () => {
    component.updated = true;
    component.error = 'Previous error';

    component.form.setValue({
      oldPassword: 'old-password',
      password: 'new-password',
      passwordConfirmation: 'new-password'
    });

    component.updatePassword();

    expect(component.updated).toBeTrue();
    expect(component.error).toBeNull();
  });

  it('should reset the change-password form and mark controls pristine', () => {
    component.reset = false;

    component.form.setValue({
      oldPassword: 'old-password',
      password: 'new-password',
      passwordConfirmation: 'new-password'
    });

    Object.keys(component.form.controls).forEach(key => {
      component.form.controls[key].markAsDirty();
    });

    component.resetForm();

    expect(component.form.value).toEqual({
      oldPassword: '',
      password: '',
      passwordConfirmation: ''
    });

    Object.keys(component.form.controls).forEach(key => {
      expect(component.form.controls[key].pristine).toBeTrue();
    });
  });

  it('should reset the reset-password form and mark controls pristine', () => {
    component.reset = true;

    component.form = formBuilder.group({
      token: ['valid-token'],
      password: ['new-password'],
      passwordConfirmation: ['new-password']
    });

    Object.keys(component.form.controls).forEach(key => {
      component.form.controls[key].markAsDirty();
    });

    component.resetForm();

    expect(component.form.value).toEqual({
      token: '',
      password: '',
      passwordConfirmation: ''
    });

    Object.keys(component.form.controls).forEach(key => {
      expect(component.form.controls[key].pristine).toBeTrue();
    });
  });
});
