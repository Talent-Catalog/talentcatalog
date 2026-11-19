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
import {Component, forwardRef, Input, Pipe, PipeTransform, ChangeDetectionStrategy} from '@angular/core';
import {ComponentFixture, fakeAsync, TestBed, tick} from '@angular/core/testing';
import {By} from '@angular/platform-browser';
import {ControlValueAccessor, NG_VALUE_ACCESSOR, ReactiveFormsModule, UntypedFormBuilder, Validators} from '@angular/forms';
import {ActivatedRoute, convertToParamMap, ParamMap, Router} from '@angular/router';
import {BehaviorSubject, of, throwError} from 'rxjs';

import {ChangePasswordComponent} from './change-password.component';
import {UserService} from '../../../services/user.service';

@Pipe({ name: 'translate', standalone: false })
class TranslatePipeStub implements PipeTransform {
    transform(value: string): string {
        return value;
    }
}

@Component({ selector: 'app-error', template: '', changeDetection: ChangeDetectionStrategy.Eager, standalone: false })
class AppErrorStubComponent {
    @Input()
    error: any;
}

@Component({ selector: 'tc-loading', template: '', changeDetection: ChangeDetectionStrategy.Eager, standalone: false })
class TcLoadingStubComponent {
    @Input()
    loading: boolean;
}

@Component({
    selector: 'tc-alert',
    template: '<ng-content></ng-content>',
    changeDetection: ChangeDetectionStrategy.Eager,
    standalone: false
})
class TcAlertStubComponent {
    @Input()
    type: string;
}

@Component({
    selector: 'tc-field',
    template: '<ng-content></ng-content>',
    changeDetection: ChangeDetectionStrategy.Eager,
    standalone: false
})
class TcFieldStubComponent {
}

@Component({
    selector: 'tc-label',
    template: '<ng-content></ng-content>',
    changeDetection: ChangeDetectionStrategy.Eager,
    standalone: false
})
class TcLabelStubComponent {
    @Input()
    for: string;
}

@Component({
    selector: 'tc-input',
    template: '',
    providers: [{
            provide: NG_VALUE_ACCESSOR,
            useExisting: forwardRef(() => TcInputStubComponent),
            multi: true
        }],
    changeDetection: ChangeDetectionStrategy.Eager,
    standalone: false
})
class TcInputStubComponent implements ControlValueAccessor {
    @Input()
    id: string;
    @Input()
    type: string;

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
    template: '<ng-content></ng-content>',
    changeDetection: ChangeDetectionStrategy.Eager,
    standalone: false
})
class TcButtonStubComponent {
    @Input()
    disabled: boolean;
}

describe('ChangePasswordComponent', () => {
    let component: ChangePasswordComponent;
    let fixture: ComponentFixture<ChangePasswordComponent>;
    let formBuilder: UntypedFormBuilder;
    let userServiceSpy: MockedObject<UserService>;
    let routerSpy: MockedObject<Router>;
    let paramMapSubject: BehaviorSubject<ParamMap>;

    beforeEach(async () => {
        userServiceSpy = {
            checkPasswordResetToken: vi.fn().mockName("UserService.checkPasswordResetToken"),
            resetPassword: vi.fn().mockName("UserService.resetPassword"),
            updatePassword: vi.fn().mockName("UserService.updatePassword")
        } as unknown as MockedObject<UserService>;

        routerSpy = {
            navigate: vi.fn().mockName("Router.navigate")
        } as unknown as MockedObject<Router>;

        paramMapSubject = new BehaviorSubject<ParamMap>(convertToParamMap({}));

        userServiceSpy.checkPasswordResetToken.mockReturnValue(of(null));
        userServiceSpy.resetPassword.mockReturnValue(of(null));
        userServiceSpy.updatePassword.mockReturnValue(of(null));
        routerSpy.navigate.mockResolvedValue(true);

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
        const loading = fixture.debugElement.query(By.directive(TcLoadingStubComponent)).componentInstance as TcLoadingStubComponent;

        const fields = fixture.debugElement.queryAll(By.directive(TcFieldStubComponent));

        const button = fixture.debugElement.query(By.directive(TcButtonStubComponent)).componentInstance as TcButtonStubComponent;

        expect(loading.loading).toBe(false);
        expect(fields.length).toBe(3);
        expect(button.disabled).toBe(true);
    });

    it('should hide the old password field in reset mode', () => {
        component.reset = true;
        component.form = formBuilder.group({
            token: ['token'],
            password: [''],
            passwordConfirmation: ['']
        });

        fixture.detectChanges();

        const inputs = fixture.debugElement.queryAll(By.directive(TcInputStubComponent)).map(debugElement => debugElement.componentInstance as TcInputStubComponent);

        expect(inputs.some(input => input.id === 'oldPassword')).toBe(false);

        expect(inputs.length).toBe(2);
    });

    it('should show a success tc-alert after the password is updated', () => {
        component.updated = true;
        fixture.detectChanges();

        const alert = fixture.debugElement.query(By.directive(TcAlertStubComponent)).componentInstance as TcAlertStubComponent;

        expect(alert.type).toBe('success');
    });

    it('should create the component', () => {
        expect(component).toBeTruthy();
    });

    it('should initialize the default component state', () => {
        expect(component.loading).toBe(false);
        expect(component.reset).toBe(false);
        expect(component.error).toBeNull();
        expect(component.updated).toBe(false);
        expect(component.tokenInvalid).toBe(false);
    });

    it('should create a change-password form when no token exists', () => {
        expect(component.form).toBeDefined();
        expect(component.form.get('oldPassword')).not.toBeNull();
        expect(component.form.get('password')).not.toBeNull();
        expect(component.form.get('passwordConfirmation')).not.toBeNull();
        expect(component.form.get('token')).toBeNull();
        expect(component.form.invalid).toBe(true);
    });

    it('should require all fields in change-password mode', () => {
        const oldPassword = component.form.get('oldPassword');
        const password = component.form.get('password');
        const passwordConfirmation = component.form.get('passwordConfirmation');

        expect(oldPassword.hasError('required')).toBe(true);
        expect(password.hasError('required')).toBe(true);
        expect(passwordConfirmation.hasError('required')).toBe(true);

        oldPassword.setValue('old-password');
        password.setValue('new-password');
        passwordConfirmation.setValue('new-password');

        expect(component.form.valid).toBe(true);
    });

    it('should validate a password-reset token', () => {
        userServiceSpy.checkPasswordResetToken.mockClear();

        paramMapSubject.next(convertToParamMap({ token: 'valid-token' }));

        expect(userServiceSpy.checkPasswordResetToken).toHaveBeenCalledTimes(1);

        expect(userServiceSpy.checkPasswordResetToken).toHaveBeenCalledWith({
            token: 'valid-token'
        });

        expect(component.token).toBe('valid-token');
    });

    it('should create a reset-password form when the token is valid', () => {
        paramMapSubject.next(convertToParamMap({ token: 'valid-token' }));

        expect(component.loading).toBe(false);
        expect(component.reset).toBe(true);
        expect(component.tokenInvalid).toBe(false);

        expect(component.form.get('token')).not.toBeNull();
        expect(component.form.get('oldPassword')).toBeNull();
        expect(component.form.get('password')).not.toBeNull();

        expect(component.form.get('passwordConfirmation')).not.toBeNull();

        expect(component.form.get('token').value).toBe('valid-token');
        expect(component.form.invalid).toBe(true);
    });

    it('should require the reset-password fields', () => {
        paramMapSubject.next(convertToParamMap({ token: 'valid-token' }));

        const token = component.form.get('token');
        const password = component.form.get('password');
        const passwordConfirmation = component.form.get('passwordConfirmation');

        expect(token.hasError('required')).toBe(false);
        expect(password.hasError('required')).toBe(true);
        expect(passwordConfirmation.hasError('required')).toBe(true);

        password.setValue('new-password');
        passwordConfirmation.setValue('new-password');

        expect(component.form.valid).toBe(true);
    });

    it('should mark the token as invalid when token validation fails', () => {
        const validationError = {
            status: 400,
            message: 'Password-reset token is invalid'
        };

        userServiceSpy.checkPasswordResetToken.mockReturnValue(throwError(validationError));

        paramMapSubject.next(convertToParamMap({ token: 'invalid-token' }));

        expect(component.error).toEqual(validationError);
        expect(component.tokenInvalid).toBe(true);
        expect(component.reset).toBe(false);
    });

    it('should hide the form when the token is invalid', () => {
        userServiceSpy.checkPasswordResetToken.mockReturnValue(throwError('Invalid token'));

        paramMapSubject.next(convertToParamMap({ token: 'invalid-token' }));

        fixture.detectChanges();

        expect(fixture.debugElement.query(By.css('form'))).toBeNull();
    });

    it('should hide the form while loading', () => {
        component.loading = true;
        fixture.detectChanges();

        expect(fixture.debugElement.query(By.css('form'))).toBeNull();
    });

    it('should pass loading state to the loading component', () => {
        component.loading = true;
        fixture.detectChanges();

        const loading = fixture.debugElement.query(By.directive(TcLoadingStubComponent)).componentInstance as TcLoadingStubComponent;

        expect(loading.loading).toBe(true);
    });

    it('should pass an error to the error component', () => {
        component.error = 'Unable to change password';
        fixture.detectChanges();

        const error = fixture.debugElement.query(By.directive(AppErrorStubComponent)).componentInstance as AppErrorStubComponent;

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

        const button = fixture.debugElement.query(By.directive(TcButtonStubComponent)).componentInstance as TcButtonStubComponent;

        expect(component.form.valid).toBe(true);
        expect(component.form.dirty).toBe(true);
        expect(button.disabled).toBe(false);
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

        const button = fixture.debugElement.query(By.directive(TcButtonStubComponent));

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
        const resetFormSpy = vi.spyOn(component, 'resetForm');

        component.reset = false;

        component.form.setValue({
            oldPassword: 'old-password',
            password: 'new-password',
            passwordConfirmation: 'new-password'
        });

        component.form.markAsDirty();

        component.updatePassword();

        expect(resetFormSpy).toHaveBeenCalledTimes(1);
        expect(component.updated).toBe(true);
        expect(component.error).toBeNull();

        expect(component.form.value).toEqual({
            oldPassword: '',
            password: '',
            passwordConfirmation: ''
        });

        expect(component.form.pristine).toBe(true);
    });

    it('should store the error when changing a password fails', () => {
        const updateError = {
            status: 400,
            message: 'Current password is incorrect'
        };

        userServiceSpy.updatePassword.mockReturnValue(throwError(updateError));

        component.reset = false;

        component.form.setValue({
            oldPassword: 'incorrect-password',
            password: 'new-password',
            passwordConfirmation: 'new-password'
        });

        component.updated = true;
        component.error = 'Previous error';

        component.updatePassword();

        expect(component.updated).toBe(false);
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
        const resetFormSpy = vi.spyOn(component, 'resetForm');

        component.reset = true;

        component.form = formBuilder.group({
            token: ['valid-token'],
            password: ['new-password'],
            passwordConfirmation: ['new-password']
        });

        component.form.markAsDirty();

        component.updatePassword();

        expect(resetFormSpy).toHaveBeenCalledTimes(1);
        expect(component.updated).toBe(true);
        expect(component.error).toBeNull();

        expect(component.form.value).toEqual({
            token: '',
            password: '',
            passwordConfirmation: ''
        });

        expect(component.form.pristine).toBe(true);

        tick(2000);
    }));

    it('should store the error when resetting a password fails', () => {
        const resetError = {
            status: 400,
            message: 'Password-reset token has expired'
        };

        userServiceSpy.resetPassword.mockReturnValue(throwError(resetError));

        component.reset = true;

        component.form = formBuilder.group({
            token: ['expired-token'],
            password: ['new-password'],
            passwordConfirmation: ['new-password']
        });

        component.updated = true;
        component.error = 'Previous error';

        component.updatePassword();

        expect(component.updated).toBe(false);
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

        expect(component.updated).toBe(true);
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
            expect(component.form.controls[key].pristine).toBe(true);
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
            expect(component.form.controls[key].pristine).toBe(true);
        });
    });
});
