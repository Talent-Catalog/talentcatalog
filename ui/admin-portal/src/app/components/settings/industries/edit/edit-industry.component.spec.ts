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
import {IndustryService} from "../../../../services/industry.service";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {ReactiveFormsModule, UntypedFormBuilder} from "@angular/forms";
import {EditIndustryComponent} from "./edit-industry.component";
import {ComponentFixture, fakeAsync, TestBed, tick} from "@angular/core/testing";
import {NgSelectModule} from "@ng-select/ng-select";
import {provideHttpClientTesting} from "@angular/common/http/testing";
import {provideHttpClient, withInterceptorsFromDi, withXhr} from "@angular/common/http";
import {of, throwError} from "rxjs";
import {Industry} from "../../../../model/industry";
import {expect, vi} from 'vitest';

describe('EditIndustryComponent', () => {
    let component: EditIndustryComponent;
    let fixture: ComponentFixture<EditIndustryComponent>;
    let industryServiceSpy: MockedObject<IndustryService>;
    let ngbActiveModalSpy: MockedObject<NgbActiveModal>;
    let formBuilder: UntypedFormBuilder;
    // @ts-expect-error
    const industry: Industry = { name: 'Test Industry', status: 'active' };

    beforeEach(async () => {
        const industryServiceSpyObj = {
            get: vi.fn().mockName("IndustryService.get"),
            update: vi.fn().mockName("IndustryService.update")
        };
        const ngbActiveModalSpyObj = {
            close: vi.fn().mockName("NgbActiveModal.close"),
            dismiss: vi.fn().mockName("NgbActiveModal.dismiss")
        };

        await TestBed.configureTestingModule({
            declarations: [EditIndustryComponent],
            imports: [ReactiveFormsModule, NgSelectModule],
            providers: [
                { provide: IndustryService, useValue: industryServiceSpyObj },
                { provide: NgbActiveModal, useValue: ngbActiveModalSpyObj },
                provideHttpClient(withXhr(), withInterceptorsFromDi()),
                provideHttpClientTesting()
            ]
        }).compileComponents();

        industryServiceSpy = TestBed.inject(IndustryService) as MockedObject<IndustryService>;
        ngbActiveModalSpy = TestBed.inject(NgbActiveModal) as MockedObject<NgbActiveModal>;
        formBuilder = TestBed.inject(UntypedFormBuilder);
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(EditIndustryComponent);
        component = fixture.componentInstance;
        component.industryId = 1; // Set industryId for testing
        industryServiceSpy.get.mockReturnValue(of(industry));
        industryServiceSpy.update.mockReturnValue(of(industry));

        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should initialize form with industry data', fakeAsync(() => {

        component.ngOnInit();
        tick(); // Waiting for async operation to complete

        expect(industryServiceSpy.get).toHaveBeenCalledWith(1);
        expect(component.industryForm.value).toEqual(industry);
        expect(component.loading).toBe(false);
    }));

    it('should call onSave and close modal when industry is successfully updated', fakeAsync(() => {
        component.industryForm.patchValue({ name: 'Updated Industry', status: 'inactive' });

        component.onSave();
        tick(); // Waiting for async operation to complete

        expect(industryServiceSpy.update).toHaveBeenCalledWith(1, { name: 'Updated Industry', status: 'inactive' });
        expect(ngbActiveModalSpy.close).toHaveBeenCalledWith(industry);
        expect(component.saving).toBe(false);
    }));

    it('should handle error when updating industry fails', fakeAsync(() => {
        const errorResponse = { status: 500, message: 'Internal Server Error' };
        industryServiceSpy.update.mockReturnValue(throwError(errorResponse));

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
