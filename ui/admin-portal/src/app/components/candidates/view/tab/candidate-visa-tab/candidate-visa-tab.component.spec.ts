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
import {CandidateVisaTabComponent} from "./candidate-visa-tab.component";
import {CandidateService} from "../../../../../services/candidate.service";
import {ComponentFixture, fakeAsync, TestBed, tick} from "@angular/core/testing";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {AuthorizationService} from "../../../../../services/authorization.service";
import {CandidateVisaCheckService} from "../../../../../services/candidate-visa-check.service";
import {CountryService} from "../../../../../services/country.service";
import {MockCandidate} from "../../../../../MockData/MockCandidate";
import {HasNameSelectorComponent} from "../../../../util/has-name-selector/has-name-selector.component";
import {FormsModule, ReactiveFormsModule, UntypedFormBuilder} from "@angular/forms";
import {provideHttpClientTesting} from "@angular/common/http/testing";
import {provideHttpClient, withInterceptorsFromDi, withXhr} from "@angular/common/http";
import {NgSelectModule} from "@ng-select/ng-select";
import {of} from "rxjs";
import {MockJob} from "../../../../../MockData/MockJob";
import {CandidateVisa} from "../../../../../model/candidate";
import {Country} from "../../../../../model/country";
import {PrintPageComponent} from "../../../../util/export-pdf/print-page.component";
import {expect, vi} from 'vitest';

describe('CandidateVisaTabComponent', () => {
    let component: CandidateVisaTabComponent;
    let fixture: ComponentFixture<CandidateVisaTabComponent>;
    let candidateServiceMock: MockedObject<CandidateService>;
    let countryServiceMock: MockedObject<CountryService>;
    let candidateVisaCheckServiceMock: MockedObject<CandidateVisaCheckService>;
    let modalServiceMock: MockedObject<NgbModal>;
    let authServiceMock: MockedObject<AuthorizationService>;
    const mockCandidate = new MockCandidate();
    beforeEach(async () => {
        candidateServiceMock = ({
            getIntakeData: vi.fn().mockName("CandidateService.getIntakeData")
        } as unknown as typeof candidateServiceMock);
        countryServiceMock = ({
            listTCDestinations: vi.fn().mockName("CountryService.listTCDestinations")
        } as unknown as typeof countryServiceMock);
        candidateVisaCheckServiceMock = ({
            create: vi.fn().mockName("CandidateVisaCheckService.create"),
            list: vi.fn().mockName("CandidateVisaCheckService.list")
        } as unknown as typeof candidateVisaCheckServiceMock);
        modalServiceMock = ({
            open: vi.fn().mockName("NgbModal.open")
        } as unknown as typeof modalServiceMock);
        authServiceMock = ({
            isSystemAdminOnly: vi.fn().mockName("AuthorizationService.isSystemAdminOnly"),
            isEditableCandidate: vi.fn().mockName("AuthorizationService.isEditableCandidate")
        } as unknown as typeof authServiceMock);

        await TestBed.configureTestingModule({
            imports: [FormsModule, ReactiveFormsModule, NgSelectModule],
            declarations: [CandidateVisaTabComponent, PrintPageComponent, HasNameSelectorComponent],
            providers: [
                UntypedFormBuilder,
                { provide: CandidateService, useValue: candidateServiceMock },
                { provide: CountryService, useValue: countryServiceMock },
                { provide: CandidateVisaCheckService, useValue: candidateVisaCheckServiceMock },
                { provide: NgbModal, useValue: modalServiceMock },
                // { provide: LocalStorageService, useValue: {} },
                { provide: AuthorizationService, useValue: authServiceMock },
                provideHttpClient(withXhr(), withInterceptorsFromDi()),
                provideHttpClientTesting(),
            ]
        }).compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(CandidateVisaTabComponent);
        component = fixture.componentInstance;
        component.candidate = mockCandidate;
        component.tabIsActive = true;
        component.form = TestBed.inject(UntypedFormBuilder).group({
            visaCountry: [0]
        });
        candidateServiceMock.getIntakeData.mockReturnValue(of({}));
        countryServiceMock.listTCDestinations.mockReturnValue(of([MockJob.country]));
        candidateVisaCheckServiceMock.list.mockReturnValue(of([]));
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should create a visa check when a country is selected', fakeAsync(() => {
        const country: Country = MockJob.country;
        const newVisaCheck: CandidateVisa = { id: 1, country: country } as CandidateVisa;
        component.visaChecks = [];

        // Mock the modal result
        const modalRef = {
            result: Promise.resolve(country),
            componentInstance: {}
        };
        modalServiceMock.open.mockReturnValue(modalRef as any);

        // Mock the create service call
        candidateVisaCheckServiceMock.create.mockReturnValue(of(newVisaCheck));
        candidateVisaCheckServiceMock.list.mockReturnValue(of([newVisaCheck]));

        // Simulate button click to add a country
        component.addRecord();
        tick();

        fixture.detectChanges();

        // Verify the service call and the update to visaChecks
        expect(candidateVisaCheckServiceMock.create).toHaveBeenCalledWith(1, { countryId: country.id });
        expect(component.visaChecks.length).toBe(1);
        expect(component.visaChecks[0]).toEqual(newVisaCheck);
    }));
});
