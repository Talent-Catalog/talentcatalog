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
import {ReactiveFormsModule, UntypedFormBuilder} from '@angular/forms';
import {EnglishThresholdComponent} from './english-threshold.component';
import {CandidateVisaCheckService} from '../../../../services/candidate-visa-check.service';
import {provideHttpClientTesting} from "@angular/common/http/testing";
import {provideHttpClient, withInterceptorsFromDi, withXhr} from "@angular/common/http";
import {NgSelectModule} from "@ng-select/ng-select";
import {AutosaveStatusComponent} from "../../../util/autosave-status/autosave-status.component";
import {expect, vi} from 'vitest';

describe('EnglishThresholdComponent', () => {
    let component: EnglishThresholdComponent;
    let fixture: ComponentFixture<EnglishThresholdComponent>;
    let candidateVisaCheckService: MockedObject<CandidateVisaCheckService>;

    beforeEach(async () => {
        const candidateVisaCheckServiceSpy = {
            someMethod: vi.fn().mockName("CandidateVisaCheckService.someMethod")
        };

        await TestBed.configureTestingModule({
            imports: [ReactiveFormsModule, NgSelectModule],
            declarations: [EnglishThresholdComponent, AutosaveStatusComponent],
            providers: [
                UntypedFormBuilder,
                { provide: CandidateVisaCheckService, useValue: candidateVisaCheckServiceSpy },
                provideHttpClient(withXhr(), withInterceptorsFromDi()),
                provideHttpClientTesting()
            ]
        }).compileComponents();

        candidateVisaCheckService = TestBed.inject(CandidateVisaCheckService) as MockedObject<CandidateVisaCheckService>;
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(EnglishThresholdComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('hasNotes should return false when visaEnglishThreshold is not "Yes" or "No"', () => {
        component.form.controls['visaEnglishThreshold'].setValue('Maybe');
        expect(component.hasNotes).toBe(false);

        component.form.controls['visaEnglishThreshold'].setValue(null);
        expect(component.hasNotes).toBe(false);

        component.form.controls['visaEnglishThreshold'].setValue('Not sure');
        expect(component.hasNotes).toBe(false);
    });

    // Optional: Add tests for 'Yes' and 'No' values to ensure completeness
    it('hasNotes should return true when visaEnglishThreshold is "Yes"', () => {
        component.form.controls['visaEnglishThreshold'].setValue('Yes');
        expect(component.hasNotes).toBe(true);
    });

    it('hasNotes should return true when visaEnglishThreshold is "No"', () => {
        component.form.controls['visaEnglishThreshold'].setValue('No');
        expect(component.hasNotes).toBe(true);
    });
});
