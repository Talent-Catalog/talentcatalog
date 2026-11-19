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
import {CreateIndustryComponent} from "./create-industry.component";
import {ComponentFixture, fakeAsync, TestBed, tick} from "@angular/core/testing";
import {IndustryService} from "../../../../services/industry.service";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {ReactiveFormsModule, UntypedFormBuilder} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";
import {provideHttpClientTesting} from "@angular/common/http/testing";
import {provideHttpClient, withInterceptorsFromDi, withXhr} from "@angular/common/http";
import {Industry} from "../../../../model/industry";
import {of, throwError} from "rxjs";
import {expect, vi} from 'vitest';

describe('CreateIndustryComponent', () => {
    let component: CreateIndustryComponent;
    let fixture: ComponentFixture<CreateIndustryComponent>;
    let industryServiceSpy: MockedObject<IndustryService>;
    let ngbActiveModalSpy: MockedObject<NgbActiveModal>;
    let formBuilder: UntypedFormBuilder;
    const industry: Industry = { id: 1, name: 'Test Industry', status: 'active' };

    beforeEach(async () => {
        const industryServiceSpyObj = {
            create: vi.fn().mockName("IndustryService.create")
        };
        const ngbActiveModalSpyObj = {
            close: vi.fn().mockName("NgbActiveModal.close"),
            dismiss: vi.fn().mockName("NgbActiveModal.dismiss")
        };

        await TestBed.configureTestingModule({
            declarations: [CreateIndustryComponent],
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
        fixture = TestBed.createComponent(CreateIndustryComponent);
        component = fixture.componentInstance;
        industryServiceSpy.create.mockReturnValue(of(industry));

        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should initialize form with empty values', () => {
        expect(component.industryForm).toBeDefined();
        expect(component.industryForm.get('name').value).toBeNull();
        expect(component.industryForm.get('status').value).toBeNull();
    });

    it('should call onSave and close modal when industry is successfully created', fakeAsync(() => {
        component.industryForm.patchValue({ name: 'Test Industry', status: 'active' });

        component.onSave();
        tick(); // Waiting for async operation to complete

        expect(industryServiceSpy.create).toHaveBeenCalledWith({ name: 'Test Industry', status: 'active' });
        expect(ngbActiveModalSpy.close).toHaveBeenCalledWith(industry);
        expect(component.saving).toBe(false);
    }));

    it('should handle error when creating industry fails', fakeAsync(() => {
        const errorResponse = { status: 500, message: 'Internal Server Error' };
        industryServiceSpy.create.mockReturnValue(throwError(errorResponse));

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
