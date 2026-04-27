/*
 * Copyright (c) 2025 Talent Catalog.
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

import { ComponentFixture, TestBed } from '@angular/core/testing';
import { VerifyEmailComponent } from './verify-email.component';
import { UserService } from '../../../services/user.service';
import { NgbActiveModal } from "@ng-bootstrap/ng-bootstrap";
import { of, throwError } from 'rxjs';
import { By } from '@angular/platform-browser';

describe('VerifyEmailComponent', () => {
  let component: VerifyEmailComponent;
  let fixture: ComponentFixture<VerifyEmailComponent>;
  let userServiceSpy: jasmine.SpyObj<UserService>;
  let activeModalSpy: jasmine.SpyObj<NgbActiveModal>;

  beforeEach(async () => {
    // Create spies for services
    userServiceSpy = jasmine.createSpyObj('UserService', ['sendVerifyEmail']);
    activeModalSpy = jasmine.createSpyObj('NgbActiveModal', ['close']);

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
    userServiceSpy.sendVerifyEmail.and.returnValue(of({}));
    component.sendVerifyEmail();
    fixture.detectChanges();
    expect(component.state).toBe('emailSent');
    expect(component.emailSent).toBeTrue();
  });

  it('should update state to "error" on failure', () => {
    userServiceSpy.sendVerifyEmail.and.returnValue(throwError(() => new Error('Email failed')));
    component.sendVerifyEmail();
    fixture.detectChanges();
    expect(component.state).toBe('error');
  });

  it('should close modal when closeModal is called', () => {
    component.closeModal();
    expect(activeModalSpy.close).toHaveBeenCalled();
  });
});
