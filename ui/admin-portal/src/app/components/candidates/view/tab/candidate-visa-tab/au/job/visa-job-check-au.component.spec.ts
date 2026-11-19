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
import {CandidateEducationService} from "../../../../../../../services/candidate-education.service";
import {VisaJobCheckAuComponent} from "./visa-job-check-au.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {OccupationService} from "../../../../../../../services/occupation.service";
import {CandidateOccupationService} from "../../../../../../../services/candidate-occupation.service";
import {NO_ERRORS_SCHEMA} from "@angular/core";
import {mockCandidateIntakeData} from "../../../candidate-intake-tab/candidate-intake-tab.component.spec";
import {MockCandidate} from "../../../../../../../MockData/MockCandidate";
import {CandidateVisa, CandidateVisaJobCheck} from "../../../../../../../model/candidate";
import {of} from "rxjs";
import {provideHttpClientTesting} from "@angular/common/http/testing";
import {provideHttpClient, withInterceptorsFromDi, withXhr} from "@angular/common/http";
import {provideRouter} from "@angular/router";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";
import {CandidateService} from "../../../../../../../services/candidate.service";
import {UpdatedByComponent} from "../../../../../../util/user/updated-by/updated-by.component";
import {TcAccordionComponent} from "../../../../../../../shared/components/accordion/tc-accordion.component";
import {TcAccordionItemComponent} from "../../../../../../../shared/components/accordion/accordion-item/tc-accordion-item.component";
import {MockCandidateVisaJobCheck} from "../../../../../../../MockData/MockCandidateVisaCheck";
import {By} from "@angular/platform-browser";
import {expect, vi} from 'vitest';

describe('VisaJobCheckAuComponent', () => {
    let component: VisaJobCheckAuComponent;
    let fixture: ComponentFixture<VisaJobCheckAuComponent>;
    let candidateEducationService: MockedObject<CandidateEducationService>;
    let candidateOccupationService: MockedObject<CandidateOccupationService>;
    let occupationService: MockedObject<OccupationService>;
    let candidateService: MockedObject<CandidateService>;
    const mockCandidate = new MockCandidate();

    beforeEach(async () => {
        const candidateServiceSpy = {
            get: vi.fn().mockName("CandidateService.get")
        };
        const candidateEducationServiceSpy = {
            list: vi.fn().mockName("CandidateEducationService.list")
        };
        const candidateOccupationServiceSpy = {
            get: vi.fn().mockName("CandidateOccupationService.get")
        };
        const occupationServiceSpy = {
            listOccupations: vi.fn().mockName("OccupationService.listOccupations")
        };
        await TestBed.configureTestingModule({
            imports: [FormsModule, ReactiveFormsModule, NgSelectModule,
                TcAccordionComponent,
                TcAccordionItemComponent
            ],
            declarations: [VisaJobCheckAuComponent, UpdatedByComponent],
            providers: [
                provideRouter([]),
                { provide: CandidateEducationService, useValue: candidateEducationServiceSpy },
                { provide: CandidateOccupationService, useValue: candidateOccupationServiceSpy },
                { provide: OccupationService, useValue: occupationServiceSpy },
                { provide: CandidateService, useValue: candidateServiceSpy },
                provideHttpClient(withXhr(), withInterceptorsFromDi()),
                provideHttpClientTesting(),
            ],
            schemas: [NO_ERRORS_SCHEMA]
        })
            .compileComponents();

        candidateEducationService = TestBed.inject(CandidateEducationService) as MockedObject<CandidateEducationService>;
        candidateOccupationService = TestBed.inject(CandidateOccupationService) as MockedObject<CandidateOccupationService>;
        occupationService = TestBed.inject(OccupationService) as MockedObject<OccupationService>;
        candidateService = TestBed.inject(CandidateService) as MockedObject<CandidateService>;
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(VisaJobCheckAuComponent);
        component = fixture.componentInstance;
        component.selectedJobCheck = MockCandidateVisaJobCheck;
        component.candidate = mockCandidate;
        component.candidateIntakeData = mockCandidateIntakeData;
        component.visaCheckRecord = { country: { id: 1 }, candidateVisaJobChecks: [{ id: 1, occupation: { id: 1 } } as CandidateVisaJobCheck] } as CandidateVisa;
        // component.visaJobAus =
        candidateEducationService.list.mockReturnValue(of([]));
        candidateOccupationService.get.mockReturnValue(of(mockCandidate.candidateOccupations));
        occupationService.listOccupations.mockReturnValue(of());
        candidateService.get.mockReturnValue(of(mockCandidate));
        component.candOccupations = mockCandidate.candidateOccupations;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should open all panels after view init', () => {
        fixture.detectChanges(); // ngOnInit() gets called here

        // Get the accordion component instance
        const accordionDebugElement = fixture.debugElement.query(By.directive(TcAccordionComponent));
        expect(accordionDebugElement).toBeTruthy();

        // The accordion items should be open (this is handled by the accordion component's allOpen input)
        const accordionComponent = accordionDebugElement.componentInstance as TcAccordionComponent;
        expect(accordionComponent.allOpen).toBe(true);
    });

    // it('should fetch candidate occupations, qualifications, and occupations on init', () => {
    //   expect(candidateEducationService.list).toHaveBeenCalledWith(1);
    //   expect(candidateOccupationService.get).toHaveBeenCalledWith(1);
    //   expect(occupationService.listOccupations).toHaveBeenCalled();
    // });
    //
    //
    // it('should calculate IELTS score type correctly', () => {
    //   expect(component.ieltsScoreType).toBe('Estimated'); // Adjust based on your logic
    // });
});
