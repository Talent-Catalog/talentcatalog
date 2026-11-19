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
import {EditOccupationComponent} from "./edit-occupation.component";
import {ComponentFixture, fakeAsync, TestBed, tick} from "@angular/core/testing";
import {OccupationService} from "../../../../services/occupation.service";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {ReactiveFormsModule, UntypedFormBuilder} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";
import {provideHttpClientTesting} from "@angular/common/http/testing";
import {provideHttpClient, withInterceptorsFromDi, withXhr} from "@angular/common/http";
import {Occupation} from "../../../../model/occupation";
import {of, throwError} from "rxjs";
import {expect, vi} from 'vitest';

describe('EditOccupationComponent', () => {
    let component: EditOccupationComponent;
    let fixture: ComponentFixture<EditOccupationComponent>;
    let occupationServiceSpy: MockedObject<OccupationService>;
    let ngbActiveModalSpy: MockedObject<NgbActiveModal>;
    let formBuilder: UntypedFormBuilder;
    // @ts-expect-error
    const occupationData: Occupation = { id: 1, name: 'Engineer', status: 'active' };
    // @ts-expect-error
    const updatedOccupation: Occupation = { id: 1, name: 'Scientist', status: 'active' };

    beforeEach(async () => {
        const occupationServiceSpyObj = {
            get: vi.fn().mockName("OccupationService.get"),
            update: vi.fn().mockName("OccupationService.update")
        };
        const ngbActiveModalSpyObj = {
            close: vi.fn().mockName("NgbActiveModal.close"),
            dismiss: vi.fn().mockName("NgbActiveModal.dismiss")
        };

        await TestBed.configureTestingModule({
            declarations: [EditOccupationComponent],
            imports: [ReactiveFormsModule, NgSelectModule],
            providers: [
                { provide: OccupationService, useValue: occupationServiceSpyObj },
                { provide: NgbActiveModal, useValue: ngbActiveModalSpyObj },
                provideHttpClient(withXhr(), withInterceptorsFromDi()),
                provideHttpClientTesting()
            ]
        }).compileComponents();

        occupationServiceSpy = TestBed.inject(OccupationService) as MockedObject<OccupationService>;
        ngbActiveModalSpy = TestBed.inject(NgbActiveModal) as MockedObject<NgbActiveModal>;
        formBuilder = TestBed.inject(UntypedFormBuilder);
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(EditOccupationComponent);
        component = fixture.componentInstance;
        component.occupationId = 1; // Assuming an occupation ID is provided
        occupationServiceSpy.get.mockReturnValue(of(occupationData));
        occupationServiceSpy.update.mockReturnValue(of(updatedOccupation));
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should load occupation data on initialization', fakeAsync(() => {

        component.ngOnInit();
        tick(); // Waiting for async operation to complete

        expect(occupationServiceSpy.get).toHaveBeenCalledWith(1);
        expect(component.occupationForm.value).toEqual({ name: 'Engineer', status: 'active' });
        expect(component.loading).toBe(false);
    }));

    it('should call onSave and close modal when occupation is successfully updated', fakeAsync(() => {
        component.occupationForm.patchValue({ name: 'Scientist', status: 'active' });

        component.onSave();
        tick(); // Waiting for async operation to complete

        expect(occupationServiceSpy.update).toHaveBeenCalledWith(1, { name: 'Scientist', status: 'active' });
        expect(ngbActiveModalSpy.close).toHaveBeenCalledWith(updatedOccupation);
        expect(component.saving).toBe(false);
    }));

    it('should handle error when updating occupation fails', fakeAsync(() => {
        const errorResponse = { status: 500, message: 'Internal Server Error' };
        occupationServiceSpy.update.mockReturnValue(throwError(errorResponse));

        component.onSave();
        tick(); // Waiting for async operation to complete

        expect(component.error).toEqual(errorResponse);
        expect(component.saving).toBe(false);
    }));

    it('should dismiss modal when dismiss is called', () => {
        component.dismiss();
        expect(ngbActiveModalSpy.dismiss).toHaveBeenCalledWith(false);
    });
});
