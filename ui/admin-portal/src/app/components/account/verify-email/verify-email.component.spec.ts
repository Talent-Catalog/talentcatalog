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
import {VerifyEmailComponent} from './verify-email.component';
import {UserService} from '../../../services/user.service';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {of, throwError} from 'rxjs';
import {By} from '@angular/platform-browser';
import {expect, vi} from 'vitest';

describe('VerifyEmailComponent', () => {
    let component: VerifyEmailComponent;
    let fixture: ComponentFixture<VerifyEmailComponent>;
    let userServiceSpy: MockedObject<UserService>;
    let activeModalSpy: MockedObject<NgbActiveModal>;

    beforeEach(async () => {
        // Create spies for services
        userServiceSpy = ({
            sendVerifyEmail: vi.fn().mockName("UserService.sendVerifyEmail")
        } as unknown as typeof userServiceSpy);
        activeModalSpy = ({
            close: vi.fn().mockName("NgbActiveModal.close")
        } as unknown as typeof activeModalSpy);

        await TestBed.configureTestingModule({
            declarations: [VerifyEmailComponent],
            providers: [
                { provide: UserService, useValue: userServiceSpy },
                { provide: NgbActiveModal, useValue: activeModalSpy }
            ]
        }).compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(VerifyEmailComponent);
        component = fixture.componentInstance;
        component.userEmail = 'test@example.com';
        fixture.detectChanges();
    });

    it('should create the component', () => {
        expect(component).toBeTruthy();
    });

    it('should display the correct email in the modal', () => {
        const emailText = fixture.debugElement.query(By.css('p')).nativeElement.textContent;
        expect(emailText).toContain('test@example.com');
    });

    it('should update state to "emailSent" on success', () => {
        userServiceSpy.sendVerifyEmail.mockReturnValue(of({}));
        component.sendVerifyEmail();
        fixture.detectChanges();
        expect(component.state).toBe('emailSent');
        expect(component.emailSent).toBe(true);
    });

    it('should update state to "error" on failure', () => {
        userServiceSpy.sendVerifyEmail.mockReturnValue(throwError(() => new Error('Email failed')));
        component.sendVerifyEmail();
        fixture.detectChanges();
        expect(component.state).toBe('error');
    });

    it('should close modal when closeModal is called', () => {
        component.closeModal();
        expect(activeModalSpy.close).toHaveBeenCalled();
    });
});
