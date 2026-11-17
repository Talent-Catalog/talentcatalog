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
import {SfJoblinkComponent, SfJoblinkValidationEvent} from "./sf-joblink.component";
import {ComponentFixture, TestBed, waitForAsync} from "@angular/core/testing";
import {SalesforceService} from "../../../services/salesforce.service";
import {FormsModule, ReactiveFormsModule, UntypedFormBuilder} from "@angular/forms";
import {provideHttpClientTesting} from "@angular/common/http/testing";
import {provideHttpClient, withInterceptorsFromDi, withXhr} from "@angular/common/http";
import {NgSelectModule} from "@ng-select/ng-select";
import {of, throwError} from "rxjs";
import {mockCandidateOpportunity} from "../../../MockData/MockCandidateOpportunity";
import {expect, vi} from 'vitest';

describe('SfJoblinkComponent', () => {
    let component: SfJoblinkComponent;
    let fixture: ComponentFixture<SfJoblinkComponent>;
    let salesforceServiceSpy: MockedObject<SalesforceService>;

    beforeEach(waitForAsync(() => {
        // TODO: vitest-migration: Cannot transform jasmine.createSpyObj with a dynamic property map. Please migrate this manually. See: https://vitest.dev/api/vi.html#vi-fn
        const spy = {
            getOpportunity: vi.fn().mockName("SalesforceService.getOpportunity")
        };

        TestBed.configureTestingModule({
            declarations: [SfJoblinkComponent],
            imports: [ReactiveFormsModule, FormsModule, NgSelectModule],
            providers: [
                UntypedFormBuilder,
                { provide: SalesforceService, useValue: spy },
                provideHttpClient(withXhr(), withInterceptorsFromDi()),
                provideHttpClientTesting()
            ]
        }).compileComponents();

        salesforceServiceSpy = TestBed.inject(SalesforceService) as MockedObject<SalesforceService>;
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(SfJoblinkComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should initialize form with sfJoblink control', () => {
        expect(component.form.contains('sfJoblink')).toBe(true);
    });

    it('should show error message for invalid URL pattern', () => {
        const input = component.form.controls['sfJoblink'];
        input.setValue('invalid-url');
        input.markAsTouched();
        fixture.detectChanges();

        const errorMessage = fixture.nativeElement.querySelector('tc-alert');
        expect(errorMessage).toBeTruthy();
    });

    it('should validate empty URL as valid', async () => {
        component.ngOnInit();
        const control = component.form.controls['sfJoblink'];
        control.setValue('');

        (component as any).sfJoblinkValidator()(control).subscribe(result => {
            expect(result).toBeNull();
            ;
        });
    });


    it('should validate valid salesforce URL', async () => {
        salesforceServiceSpy.getOpportunity.mockReturnValue(of(mockCandidateOpportunity));
        vi.spyOn(component.sfJoblinkValidation, 'emit');
        component.ngOnInit();
        const control = component.form.controls['sfJoblink'];
        control.setValue('https://valid.salesforce.com');

        (component as any).sfJoblinkValidator()(control).subscribe(result => {
            expect(result).toBeNull();
            expect(component.sfJoblinkValidation.emit).toHaveBeenCalledWith(expect.any(SfJoblinkValidationEvent));
            expect(salesforceServiceSpy.getOpportunity).toHaveBeenCalled();
            ;
        });
    });


    it('should invalidate URL with null opportunity', async () => {
        salesforceServiceSpy.getOpportunity.mockReturnValue(of(null));

        component.ngOnInit();
        const control = component.form.controls['sfJoblink'];
        control.setValue('https://invalid.salesforce.com');

        (component as any).sfJoblinkValidator()(control).subscribe(result => {
            expect(result).toEqual({ 'invalidSfJoblink': true });
            ;
        });
    });

    it('should handle error during validation', async () => {
        const error = new Error('Server error');
        salesforceServiceSpy.getOpportunity.mockReturnValue(throwError(error));
        vi.spyOn(component.updateError, 'emit');
        component.ngOnInit();
        const control = component.form.controls['sfJoblink'];
        control.setValue('https://error.salesforce.com');

        (component as any).sfJoblinkValidator()(control).subscribe(result => {
            expect(result).toBeNull();
            expect(component.updateError.emit).toHaveBeenCalledWith(error);
            ;
        });
    });
});
