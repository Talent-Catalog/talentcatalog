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
import {ConfirmationComponent} from "./confirmation.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {expect, vi} from 'vitest';

describe('ConfirmationComponent', () => {
    let component: ConfirmationComponent;
    let fixture: ComponentFixture<ConfirmationComponent>;
    let activeModalSpy: MockedObject<NgbActiveModal>;

    beforeEach(async () => {
        const spy = {
            close: vi.fn().mockName("NgbActiveModal.close"),
            dismiss: vi.fn().mockName("NgbActiveModal.dismiss")
        };

        await TestBed.configureTestingModule({
            declarations: [ConfirmationComponent],
            providers: [
                { provide: NgbActiveModal, useValue: spy }
            ]
        })
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(ConfirmationComponent);
        component = fixture.componentInstance;
        activeModalSpy = TestBed.inject(NgbActiveModal) as MockedObject<NgbActiveModal>;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should have default title as "Confirmation"', () => {
        expect(component.title).toBe('Confirmation');
    });

    it('should call dismiss method of NgbActiveModal when dismiss is called', () => {
        component.dismiss();
        expect(activeModalSpy.dismiss).toHaveBeenCalledWith(false);
    });

    it('should call close method of NgbActiveModal when close is called', () => {
        component.close();
        expect(activeModalSpy.close).toHaveBeenCalledWith(true);
    });

    it('should display the message if provided', () => {
        component.message = 'Test message';
        fixture.detectChanges();
        const compiled = fixture.nativeElement;
        expect(compiled.querySelector('p').textContent).toContain('Test message');
    });

    it('should display "Are you sure?" if message is not provided', () => {
        component.message = null;
        fixture.detectChanges();
        const compiled = fixture.nativeElement;
        expect(compiled.querySelector('p').textContent).toContain('Are you sure?');
    });
});
