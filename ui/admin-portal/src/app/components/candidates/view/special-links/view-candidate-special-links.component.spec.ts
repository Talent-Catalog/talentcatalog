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
import {ViewCandidateSpecialLinksComponent} from "./view-candidate-special-links.component";
import {CandidateService} from "../../../../services/candidate.service";
import {AuthorizationService} from "../../../../services/authorization.service";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {provideHttpClientTesting} from "@angular/common/http/testing";
import {provideHttpClient, withInterceptorsFromDi, withXhr} from "@angular/common/http";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";
import {Candidate} from "../../../../model/candidate";
import {MockCandidate} from "../../../../MockData/MockCandidate";
import {of, throwError} from "rxjs";
import {expect, vi} from 'vitest';

describe('ViewCandidateSpecialLinksComponent', () => {
    let component: ViewCandidateSpecialLinksComponent;
    let fixture: ComponentFixture<ViewCandidateSpecialLinksComponent>;
    let candidateService: MockedObject<CandidateService>;
    let authService: MockedObject<AuthorizationService>;
    let modalService: MockedObject<NgbModal>;
    const mockCandidate = new MockCandidate();
    beforeEach(async () => {
        const candidateServiceSpy = {
            createCandidateFolder: vi.fn().mockName("CandidateService.createCandidateFolder"),
            createUpdateLiveCandidate: vi.fn().mockName("CandidateService.createUpdateLiveCandidate"),
            updateCandidate: vi.fn().mockName("CandidateService.updateCandidate")
        };
        const authServiceSpy = {
            canAccessSalesforce: vi.fn().mockName("AuthorizationService.canAccessSalesforce"),
            canAccessGoogleDrive: vi.fn().mockName("AuthorizationService.canAccessGoogleDrive")
        };
        const modalServiceSpy = {
            open: vi.fn().mockName("NgbModal.open")
        };

        await TestBed.configureTestingModule({
            declarations: [ViewCandidateSpecialLinksComponent],
            imports: [FormsModule, ReactiveFormsModule, NgSelectModule],
            providers: [
                { provide: CandidateService, useValue: candidateServiceSpy },
                { provide: AuthorizationService, useValue: authServiceSpy },
                { provide: NgbModal, useValue: modalServiceSpy },
                provideHttpClient(withXhr(), withInterceptorsFromDi()),
                provideHttpClientTesting()
            ]
        }).compileComponents();

        candidateService = TestBed.inject(CandidateService) as MockedObject<CandidateService>;
        authService = TestBed.inject(AuthorizationService) as MockedObject<AuthorizationService>;
        modalService = TestBed.inject(NgbModal) as MockedObject<NgbModal>;
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(ViewCandidateSpecialLinksComponent);
        component = fixture.componentInstance;
        component.candidate = mockCandidate;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should initialize with input properties', () => {
        const candidate: Candidate = component.candidate = mockCandidate;
        component.editable = true;

        fixture.detectChanges();

        expect(component.candidate).toEqual(candidate);
        expect(component.editable).toBe(true);
    });

    it('should handle createCandidateFolder correctly', () => {
        const candidate: Candidate = mockCandidate;
        const updatedCandidate: Candidate = { ...candidate, folderlink: 'https://drive.google.com' };
        component.candidate = candidate;

        candidateService.createCandidateFolder.mockReturnValue(of(updatedCandidate));

        component.createCandidateFolder();

        fixture.detectChanges();
        expect(component.loading).toBe(false);
        expect(candidateService.updateCandidate).toHaveBeenCalled();
    });

    it('should handle createCandidateFolder error', () => {
        const candidate: Candidate = mockCandidate;
        const errorResponse = 'Error creating folder';
        component.candidate = candidate;

        candidateService.createCandidateFolder.mockReturnValue(throwError(errorResponse));

        component.createCandidateFolder();

        fixture.detectChanges();
        expect(component.loading).toBe(false);
        expect(component.error).toBe(errorResponse);
    });

    it('should handle createUpdateSalesforce correctly', () => {
        const candidate: Candidate = mockCandidate;
        const updatedCandidate: Candidate = { ...candidate, sflink: 'https://salesforce.com' };
        component.candidate = candidate;

        candidateService.createUpdateLiveCandidate.mockReturnValue(of(updatedCandidate));

        component.createUpdateSalesforce();

        fixture.detectChanges();
        expect(component.loading).toBe(false);
        expect(candidateService.updateCandidate).toHaveBeenCalled();
    });

    it('should handle createUpdateSalesforce error', () => {
        const candidate: Candidate = mockCandidate;
        const errorResponse = 'Error updating Salesforce';
        component.candidate = candidate;

        candidateService.createUpdateLiveCandidate.mockReturnValue(throwError(errorResponse));

        component.createUpdateSalesforce();

        fixture.detectChanges();
        expect(component.loading).toBe(false);
        expect(component.error).toBe(errorResponse);
    });


    it('should check Salesforce access', () => {
        authService.canAccessSalesforce.mockReturnValue(true);

        expect(component.canAccessSalesforce()).toBe(true);
        expect(authService.canAccessSalesforce).toHaveBeenCalled();
    });

    it('should check Google Drive access', () => {
        authService.canAccessGoogleDrive.mockReturnValue(true);

        expect(component.canAccessGoogleDrive()).toBe(true);
        expect(authService.canAccessGoogleDrive).toHaveBeenCalled();
    });
});
