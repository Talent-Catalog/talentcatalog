import type {MockedObject} from "vitest";
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {VerifyEmailToastComponent} from './verify-email-toast.component';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {VerifyEmailComponent} from '../verify-email/verify-email.component';
import {NO_ERRORS_SCHEMA} from '@angular/core';
import {expect, vi} from 'vitest';

describe('VerifyEmailToastComponent', () => {
    let component: VerifyEmailToastComponent;
    let fixture: ComponentFixture<VerifyEmailToastComponent>;
    let modalServiceSpy: MockedObject<NgbModal>;

    beforeEach(async () => {
        const modalSpy = {
            open: vi.fn().mockName("NgbModal.open")
        };

        await TestBed.configureTestingModule({
            declarations: [VerifyEmailToastComponent],
            providers: [
                { provide: NgbModal, useValue: modalSpy }
            ],
            // schemas: [NO_ERRORS_SCHEMA]  // Ignore external components/templates
        }).compileComponents();

        fixture = TestBed.createComponent(VerifyEmailToastComponent);
        component = fixture.componentInstance;
        modalServiceSpy = TestBed.inject(NgbModal) as MockedObject<NgbModal>;
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should have default showToast as false', () => {
        expect(component.showToast).toBe(false);
    });

    it('should hide toast when hideToast() is called', () => {
        component.showToast = true;
        component.hideToast();
        expect(component.showToast).toBe(false);
    });

    it('should open modal and pass userEmail', () => {
        const modalRefMock = {
            componentInstance: {
                userEmail: ''
            }
        };
        modalServiceSpy.open.mockReturnValue(modalRefMock as any);

        component.userEmail = 'test@example.com';
        component.openModal();

        expect(modalServiceSpy.open).toHaveBeenCalledWith(VerifyEmailComponent, { centered: true });
        expect(modalRefMock.componentInstance.userEmail).toBe('test@example.com');
    });
});
