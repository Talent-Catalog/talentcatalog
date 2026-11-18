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
import {ComponentFixture, TestBed, waitForAsync} from '@angular/core/testing';
import {ActivatedRoute, convertToParamMap} from '@angular/router';
import {of, Subject, throwError} from 'rxjs';
import {ViewCandidateOppFromUrlComponent} from './view-candidate-opp-from-url.component';
import {CandidateOpportunityService} from '../../../services/candidate-opportunity.service';
import {mockCandidateOpportunity} from "../../../MockData/MockCandidateOpportunity";
import {ViewCandidateOppComponent} from "../view-candidate-opp/view-candidate-opp.component";
import {provideHttpClientTesting} from "@angular/common/http/testing";
import {provideHttpClient, withInterceptorsFromDi, withXhr} from "@angular/common/http";
import {NgbNavModule} from "@ng-bootstrap/ng-bootstrap";
import {MockPartner} from "../../../MockData/MockPartner";
import {AuthenticationService} from "../../../services/authentication.service";
import {OpportunityStageNextStepComponent} from "../../util/opportunity-stage-next-step/opportunity-stage-next-step.component";
import {RouterLinkStubDirective} from "../../../../test-helpers/router-link-stub.directive";
import {CUSTOM_ELEMENTS_SCHEMA} from "@angular/core";
import {expect, vi} from 'vitest';

describe('ViewCandidateOppFromUrlComponent', () => {
    let component: ViewCandidateOppFromUrlComponent;
    let fixture: ComponentFixture<ViewCandidateOppFromUrlComponent>;
    let mockActivatedRoute: any;
    let mockOpportunityService: MockedObject<CandidateOpportunityService>;
    let mockAuthService: any;

    beforeEach(waitForAsync(() => {
        // Create a spy object for CandidateOpportunityService
        const spyOpportunityService = {
            get: vi.fn().mockName("CandidateOpportunityService.get")
        };
        mockAuthService = {
            getLoggedInUser: vi.fn().mockName("AuthenticationService.getLoggedInUser"),
            canViewChats: vi.fn().mockName("AuthenticationService.canViewChats"),
            loggedInUser$: new Subject<any>()
        };
        spyOpportunityService.get;

        TestBed.configureTestingModule({
            declarations: [ViewCandidateOppFromUrlComponent, OpportunityStageNextStepComponent, RouterLinkStubDirective, ViewCandidateOppComponent],
            imports: [NgbNavModule],
            providers: [
                { provide: ActivatedRoute, useValue: { paramMap: of(convertToParamMap({ id: '1' })) } },
                { provide: CandidateOpportunityService, useValue: spyOpportunityService },
                { provide: AuthenticationService, useValue: mockAuthService },
                provideHttpClient(withXhr(), withInterceptorsFromDi()),
                provideHttpClientTesting(),

            ],
            schemas: [CUSTOM_ELEMENTS_SCHEMA]
        })
            .compileComponents();

        mockActivatedRoute = TestBed.inject(ActivatedRoute);
        mockOpportunityService = TestBed.inject(CandidateOpportunityService) as MockedObject<CandidateOpportunityService>;
        mockAuthService.getLoggedInUser.mockReturnValue({ partner: new MockPartner() });
        mockOpportunityService.get.mockReturnValue(of(mockCandidateOpportunity));

    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(ViewCandidateOppFromUrlComponent);
        component = fixture.componentInstance;
        component.opp = mockCandidateOpportunity;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should load opportunity on init', () => {
        mockOpportunityService.get.mockReturnValue(of(mockCandidateOpportunity));

        component.ngOnInit();

        expect(component.loading).toBe(false); // Ensure loading is set to false after data is fetched
        expect(component.error).toBeNull(); // Ensure error is null after successful data fetch
        expect(component.opp).toEqual(mockCandidateOpportunity); // Ensure opportunity data is set correctly
    });

    it('should handle error when loading opportunity', () => {
        const errorMessage = 'Error fetching opportunity';
        mockOpportunityService.get.mockReturnValue(throwError(errorMessage));

        component.ngOnInit();

        expect(component.loading).toBe(false); // Ensure loading is set to false after error
        expect(component.error).toEqual(errorMessage); // Ensure error message is set correctly
        expect(component.opp).toBeNull(); // Ensure opportunity data is null after error
    });
});
