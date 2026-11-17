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
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {provideHttpClientTesting} from '@angular/common/http/testing';
import {provideHttpClient, withInterceptorsFromDi, withXhr} from '@angular/common/http';
import {ChangePasswordComponent} from './change-password.component';
import {ReactiveFormsModule, UntypedFormBuilder} from '@angular/forms';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {UserService} from '../../../services/user.service';
import {of} from 'rxjs';
import {config_test} from "../../../../config-test";
import {MockUser} from "../../../MockData/MockUser";
import {expect, vi} from 'vitest';

describe('ChangePasswordComponent', () => {
    let component: ChangePasswordComponent;
    let fixture: ComponentFixture<ChangePasswordComponent>;
    let userService: MockedObject<UserService>;
    let activeModal: MockedObject<NgbActiveModal>;

    beforeEach(async () => {
        const userServiceSpy = {
            updatePassword: vi.fn().mockName("UserService.updatePassword")
        };
        const activeModalSpy = {
            close: vi.fn().mockName("NgbActiveModal.close"),
            dismiss: vi.fn().mockName("NgbActiveModal.dismiss")
        };

        await TestBed.configureTestingModule({
            imports: [
                ReactiveFormsModule
            ],
            declarations: [
                ChangePasswordComponent
            ],
            providers: [
                UntypedFormBuilder,
                { provide: UserService, useValue: userServiceSpy },
                { provide: NgbActiveModal, useValue: activeModalSpy },
                provideHttpClient(withXhr(), withInterceptorsFromDi()),
                provideHttpClientTesting()
            ]
        }).compileComponents();

        userService = TestBed.inject(UserService) as MockedObject<UserService>;
        activeModal = TestBed.inject(NgbActiveModal) as MockedObject<NgbActiveModal>;
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(ChangePasswordComponent);
        component = fixture.componentInstance;
        component.user = new MockUser();
        fixture.detectChanges();
    });

    it('should initialize the form with empty password and passwordConfirmation fields', () => {
        const formValue = component.form.value;
        expect(formValue.password).toEqual('');
        expect(formValue.passwordConfirmation).toEqual('');
    });

    it('should call updatePassword() function and close modal on successful password update', () => {
        // Arrange
        component.form.patchValue({
            password: config_test.credentials.password,
            passwordConfirmation: config_test.credentials.password
        });

        userService.updatePassword.mockReturnValue(of(true));

        // Act
        component.updatePassword();

        // Assert
        expect(userService.updatePassword).toHaveBeenCalledWith(1, { password: config_test.credentials.password, passwordConfirmation: config_test.credentials.password } as any);
        expect(activeModal.close).toHaveBeenCalledWith(component.user);
    });

    it('should dismiss modal without changes when closeModal() is called', () => {
        // Act
        component.dismiss();
        // Assert
        expect(activeModal.dismiss).toHaveBeenCalledWith(false);
    });
});
