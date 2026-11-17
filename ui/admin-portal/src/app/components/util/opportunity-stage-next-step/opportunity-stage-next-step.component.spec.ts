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
import {OpportunityStageNextStepComponent} from "./opportunity-stage-next-step.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {AuthorizationService} from "../../../services/authorization.service";
import {JobService} from "../../../services/job.service";
import {NgbModal, NgbModule} from "@ng-bootstrap/ng-bootstrap";
import {CandidateOpportunityService} from "../../../services/candidate-opportunity.service";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {provideHttpClientTesting} from "@angular/common/http/testing";
import {provideHttpClient, withInterceptorsFromDi, withXhr} from "@angular/common/http";
import {By} from "@angular/platform-browser";
import {mockCandidateOpportunity} from "../../../MockData/MockCandidateOpportunity";
import {OpportunityProgressParams} from "../../../model/opportunity";
import {EditOppComponent} from "../../opportunity/edit-opp/edit-opp.component";
import {of, throwError} from "rxjs";
import {expect, vi} from 'vitest';

describe('OpportunityStageNextStepComponent', () => {
    let component: OpportunityStageNextStepComponent;
    let fixture: ComponentFixture<OpportunityStageNextStepComponent>;
    let authServiceSpy: MockedObject<AuthorizationService>;
    let candidateOpportunityServiceSpy: MockedObject<CandidateOpportunityService>;
    let jobServiceSpy: MockedObject<JobService>;
    let modalServiceSpy: MockedObject<NgbModal>;

    beforeEach(async () => {
        const authSpy = {
            canEditCandidateOpp: vi.fn().mockName("AuthorizationService.canEditCandidateOpp"),
            canChangeJobStage: vi.fn().mockName("AuthorizationService.canChangeJobStage")
        };
        const candidateOppSpy = {
            updateCandidateOpportunity: vi.fn().mockName("CandidateOpportunityService.updateCandidateOpportunity")
        };
        const jobSpy = {
            update: vi.fn().mockName("JobService.update")
        };
        const modalSpy = {
            open: vi.fn().mockName("NgbModal.open")
        };

        await TestBed.configureTestingModule({
            declarations: [OpportunityStageNextStepComponent],
            imports: [FormsModule, ReactiveFormsModule, NgbModule],
            providers: [
                { provide: AuthorizationService, useValue: authSpy },
                { provide: CandidateOpportunityService, useValue: candidateOppSpy },
                { provide: JobService, useValue: jobSpy },
                { provide: NgbModal, useValue: modalSpy },
                provideHttpClient(withXhr(), withInterceptorsFromDi()),
                provideHttpClientTesting()
            ]
        }).compileComponents();

        authServiceSpy = TestBed.inject(AuthorizationService) as MockedObject<AuthorizationService>;
        candidateOpportunityServiceSpy = TestBed.inject(CandidateOpportunityService) as MockedObject<CandidateOpportunityService>;
        jobServiceSpy = TestBed.inject(JobService) as MockedObject<JobService>;
        modalServiceSpy = TestBed.inject(NgbModal) as MockedObject<NgbModal>;
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(OpportunityStageNextStepComponent);
        component = fixture.componentInstance;
        component.opp = mockCandidateOpportunity;
        fixture.detectChanges();
    });
    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should display progress card when opp is provided', () => {
        component.opp = mockCandidateOpportunity;
        fixture.detectChanges();
        const card = fixture.debugElement.query(By.css('tc-card'));
        expect(card).toBeTruthy();
    });

    it('should display tc-loading indicator when updating', () => {
        component.updating = true;
        authServiceSpy.canEditCandidateOpp.mockReturnValue(true);
        fixture.detectChanges();
        const loadingIndicator = fixture.debugElement.query(By.css('tc-loading'));
        expect(loadingIndicator).toBeTruthy();
    });

    it('should display error message when error is set', () => {
        component.error = 'Some error';
        fixture.detectChanges();
        const errorElement = fixture.debugElement.query(By.css('.alert-danger'));
        expect(errorElement).toBeTruthy();
        expect(errorElement.nativeElement.textContent).toContain('Some error');
    });

    it('should call editOppProgress when edit button is clicked', () => {
        component.opp = mockCandidateOpportunity;
        vi.spyOn(component, 'editOppProgress').mockReturnValue(undefined);
        component.updating = true;
        authServiceSpy.canEditCandidateOpp.mockReturnValue(true);
        fixture.detectChanges();
        const button = fixture.debugElement.query(By.css('.edit-opp-progress-btn'));
        button.triggerEventHandler('onClick', null);

        fixture.detectChanges();
        expect(component.editOppProgress).toHaveBeenCalled();
    });

    it('should open EditOppComponent modal on editOppProgress', () => {
        component.opp = mockCandidateOpportunity;
        const modalRef = {
            componentInstance: { opp: null },
            result: Promise.resolve({} as OpportunityProgressParams)
        };
        modalServiceSpy.open.mockReturnValue(modalRef as any);

        component.editOppProgress();
        expect(modalServiceSpy.open).toHaveBeenCalledWith(EditOppComponent, { size: 'lg' });
    });

    it('should update opportunity on successful modal result', async () => {
        component.opp = mockCandidateOpportunity;
        const modalRef = {
            componentInstance: { opp: null },
            result: Promise.resolve({} as OpportunityProgressParams)
        };
        modalServiceSpy.open.mockReturnValue(modalRef as any);
        vi.spyOn(component as any, 'doUpdate').mockReturnValue(undefined); // Use 'any' type to access private method

        component.editOppProgress();
        await modalRef.result;

        expect((component as any).doUpdate).toHaveBeenCalledWith({});
    });

    it('should set updating and error on failed doUpdate', () => {
        component.opp = mockCandidateOpportunity;
        candidateOpportunityServiceSpy.updateCandidateOpportunity.mockReturnValue(throwError('Error'));

        (component as any).doUpdate({} as OpportunityProgressParams);
        expect(component.updating).toBe(false);
        expect(component.error).toBe('Error');
    });

    it('should emit oppProgressUpdated on successful update', () => {
        component.opp = mockCandidateOpportunity;
        const updatedOpp = mockCandidateOpportunity;
        updatedOpp.id = 2;
        candidateOpportunityServiceSpy.updateCandidateOpportunity.mockReturnValue(of(updatedOpp));
        vi.spyOn(component.oppProgressUpdated, 'emit').mockReturnValue(undefined);

        (component as any).doUpdate({} as OpportunityProgressParams);
        expect(component.oppProgressUpdated.emit).toHaveBeenCalledWith(updatedOpp);
        expect(component.updating).toBe(false);
    });

    it('should determine editable based on permissions', () => {
        component.opp = mockCandidateOpportunity;
        authServiceSpy.canEditCandidateOpp.mockReturnValue(true);
        authServiceSpy.canChangeJobStage.mockReturnValue(false);

        expect(component.editable).toBe(true);
    });
});
