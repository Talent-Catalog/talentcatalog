import type { MockedObject } from "vitest";
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
import { Component, forwardRef, Input, Pipe, PipeTransform, ChangeDetectionStrategy } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { ControlValueAccessor, NG_VALUE_ACCESSOR, ReactiveFormsModule } from '@angular/forms';
import { of, throwError } from 'rxjs';

import { ResetPasswordComponent } from './reset-password.component';
import { UserService } from '../../../services/user.service';

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

@Component({ selector: 'app-loading', template: '', changeDetection: ChangeDetectionStrategy.Eager, standalone: false })
class AppLoadingStubComponent {
    @Input()
    loading: boolean;
}

@Component({ selector: 'tc-alert', template: '<ng-content></ng-content>', changeDetection: ChangeDetectionStrategy.Eager, standalone: false })
class TcAlertStubComponent {
    @Input()
    type: string;
}

@Component({ selector: 'tc-field', template: '<ng-content></ng-content>', changeDetection: ChangeDetectionStrategy.Eager, standalone: false })
class TcFieldStubComponent {
}

@Component({ selector: 'tc-label', template: '<ng-content></ng-content>', changeDetection: ChangeDetectionStrategy.Eager, standalone: false })
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
    @Input()
    invalid: boolean;

    writeValue(): void {
    }

    registerOnChange(): void {
    }

    registerOnTouched(): void {
    }

    setDisabledState(): void {
    }
}

@Component({ selector: 'tc-button', template: '<ng-content></ng-content>', changeDetection: ChangeDetectionStrategy.Eager, standalone: false })
class TcButtonStubComponent {
    @Input()
    disabled: boolean;
}

describe('ResetPasswordComponent', () => {
    let component: ResetPasswordComponent;
    let fixture: ComponentFixture<ResetPasswordComponent>;
    let userService: MockedObject<UserService>;

    beforeEach(async () => {
        userService = {
            sendResetPassword: vi.fn().mockName("UserService.sendResetPassword")
        } as unknown as MockedObject<UserService>;
        userService.sendResetPassword.mockReturnValue(of(null));

        await TestBed.configureTestingModule({
            imports: [ReactiveFormsModule],
            declarations: [
                ResetPasswordComponent,
                TranslatePipeStub,
                AppErrorStubComponent,
                AppLoadingStubComponent,
                TcAlertStubComponent,
                TcFieldStubComponent,
                TcLabelStubComponent,
                TcInputStubComponent,
                TcButtonStubComponent
            ],
            providers: [
                { provide: UserService, useValue: userService }
            ]
        }).compileComponents();

        fixture = TestBed.createComponent(ResetPasswordComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should render one tc field and a disabled submit button while the form is invalid', () => {
        const fields = fixture.debugElement.queryAll(By.directive(TcFieldStubComponent));
        const button = fixture.debugElement.query(By.directive(TcButtonStubComponent)).componentInstance as TcButtonStubComponent;

        expect(fields.length).toBe(1);
        expect(button.disabled).toBe(true);
    });

    it('should show a success tc-alert after reset email has been sent', () => {
        component.updated = true;
        fixture.detectChanges();

        const alert = fixture.debugElement.query(By.directive(TcAlertStubComponent)).componentInstance as TcAlertStubComponent;

        expect(alert.type).toBe('success');
    });

    it('should create the component', () => {
        expect(component).toBeTruthy();
    });

    it('should initialize the component state and form', () => {
        expect(component.loading).toBe(false);
        expect(component.error).toBeNull();
        expect(component.updated).toBe(false);
        expect(component.form).toBeDefined();
        expect(component.form.get('email')).toBeDefined();
        expect(component.form.get('email').value).toBe('');
        expect(component.form.invalid).toBe(true);
    });

    it('should return the email value from the form', () => {
        component.form.get('email').setValue('candidate@example.com');

        expect(component.email).toBe('candidate@example.com');
    });

    it('should require an email address', () => {
        const emailControl = component.form.get('email');

        emailControl.setValue('');

        expect(emailControl.hasError('required')).toBe(true);
        expect(component.form.invalid).toBe(true);

        emailControl.setValue('candidate@example.com');

        expect(emailControl.hasError('required')).toBe(false);
        expect(component.form.valid).toBe(true);
    });

    it('should enable the submit button when the form is valid and dirty', () => {
        const emailControl = component.form.get('email');

        emailControl.setValue('candidate@example.com');
        emailControl.markAsDirty();
        fixture.detectChanges();

        const button = fixture.debugElement.query(By.directive(TcButtonStubComponent)).componentInstance as TcButtonStubComponent;

        expect(component.form.valid).toBe(true);
        expect(component.form.dirty).toBe(true);
        expect(button.disabled).toBe(false);
    });

    it('should keep the submit button disabled while loading', () => {
        const emailControl = component.form.get('email');

        emailControl.setValue('candidate@example.com');
        emailControl.markAsDirty();
        component.loading = true;
        fixture.detectChanges();

        const button = fixture.debugElement.query(By.directive(TcButtonStubComponent)).componentInstance as TcButtonStubComponent;

        expect(button.disabled).toBe(true);
    });

    it('should reset the email and mark the form control as pristine', () => {
        const emailControl = component.form.get('email');

        emailControl.setValue('candidate@example.com');
        emailControl.markAsDirty();

        expect(emailControl.dirty).toBe(true);

        component.resetForm();

        expect(emailControl.value).toBe('');
        expect(emailControl.pristine).toBe(true);
        expect(component.form.pristine).toBe(true);
    });

    it('should send a reset-password request with the email and null token', () => {
        component.form.get('email').setValue('candidate@example.com');

        component.sendResetEmail();

        expect(userService.sendResetPassword).toHaveBeenCalledTimes(1);
        expect(userService.sendResetPassword).toHaveBeenCalledWith(expect.objectContaining({
            email: 'candidate@example.com',
            reCaptchaV3Token: null
        }));
    });

    it('should clear previous state before sending the reset request', () => {
        component.updated = true;
        component.error = { message: 'Previous error' };
        component.form.get('email').setValue('candidate@example.com');

        component.sendResetEmail();

        expect(component.error).toBeNull();
    });

    it('should reset the form and set updated after a successful request', () => {
        const resetFormSpy = vi.spyOn(component, 'resetForm');
        const emailControl = component.form.get('email');

        emailControl.setValue('candidate@example.com');
        emailControl.markAsDirty();

        component.sendResetEmail();

        expect(userService.sendResetPassword).toHaveBeenCalled();
        expect(resetFormSpy).toHaveBeenCalledTimes(1);
        expect(emailControl.value).toBe('');
        expect(emailControl.pristine).toBe(true);
        expect(component.updated).toBe(true);
        expect(component.error).toBeNull();
    });

    it('should hide the form and display the success alert after a successful request', () => {
        component.form.get('email').setValue('candidate@example.com');

        component.sendResetEmail();
        fixture.detectChanges();

        const form = fixture.debugElement.query(By.css('form'));
        const alert = fixture.debugElement.query(By.directive(TcAlertStubComponent));

        expect(component.updated).toBe(true);
        expect(form).toBeNull();
        expect(alert).not.toBeNull();
    });

    it('should store the error when sending the reset request fails', () => {
        const serviceError = {
            status: 500,
            message: 'Unable to send reset email'
        };

        userService.sendResetPassword.mockReturnValue(throwError(serviceError));

        component.form.get('email').setValue('candidate@example.com');

        component.sendResetEmail();

        expect(userService.sendResetPassword).toHaveBeenCalledTimes(1);
        expect(component.error).toEqual(serviceError);
        expect(component.updated).toBe(false);
    });

    it('should keep the form visible when sending the reset request fails', () => {
        userService.sendResetPassword.mockReturnValue(throwError(() => new Error('Request failed')));

        component.form.get('email').setValue('candidate@example.com');

        component.sendResetEmail();
        fixture.detectChanges();

        const form = fixture.debugElement.query(By.css('form'));
        const alert = fixture.debugElement.query(By.directive(TcAlertStubComponent));

        expect(form).not.toBeNull();
        expect(alert).toBeNull();
        expect(component.updated).toBe(false);
    });
});
