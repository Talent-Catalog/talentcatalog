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
import {ViewCandidateOppComponent} from "./view-candidate-opp.component";
import {ComponentFixture, fakeAsync, TestBed, tick} from "@angular/core/testing";
import {NgbModal, NgbNavModule} from "@ng-bootstrap/ng-bootstrap";
import {FileSelectorComponent} from "../../util/file-selector/file-selector.component";
import {CandidateOpportunityService} from "../../../services/candidate-opportunity.service";
import {AuthenticationService} from "../../../services/authentication.service";
import {ChatService} from "../../../services/chat.service";
import {of} from "rxjs";
import {provideHttpClientTesting} from "@angular/common/http/testing";
import {provideHttpClient, withInterceptorsFromDi, withXhr} from "@angular/common/http";
import {mockCandidateOpportunity} from "../../../MockData/MockCandidateOpportunity";
import {RouterLinkStubDirective} from "../../../../test-helpers/router-link-stub.directive";
import {MockPartner} from "../../../MockData/MockPartner";
import {OpportunityStageNextStepComponent} from "../../util/opportunity-stage-next-step/opportunity-stage-next-step.component";
import {MockJobChat} from "../../../MockData/MockJobChat";
import {ChatReadStatusComponent} from "../../chat/chat-read-status/chat-read-status.component";
import {CUSTOM_ELEMENTS_SCHEMA} from "@angular/core";
import {AuthorizationService} from "../../../services/authorization.service";
import {expect, vi} from 'vitest';

describe('ViewCandidateOppComponent', () => {
    let component: ViewCandidateOppComponent;
    let fixture: ComponentFixture<ViewCandidateOppComponent>;
    let mockModalService: any;
    let mockCandidateOpportunityService: MockedObject<CandidateOpportunityService>;
    let mockAuthService: any;
    let chatService: MockedObject<ChatService>;
    beforeEach(async () => {
        mockModalService = {
            open: vi.fn().mockName("NgbModal.open")
        };
        mockCandidateOpportunityService = ({
            uploadOffer: vi.fn().mockName("CandidateOpportunityService.uploadOffer")
        } as unknown as typeof mockCandidateOpportunityService);
        mockAuthService = {
            getLoggedInUser: vi.fn().mockName("AuthenticationService.getLoggedInUser")
        };
        const authorizationSpy = {
            canEditCandidateOpp: vi.fn().mockName("AuthorizationService.canEditCandidateOpp"),
            canViewChats: vi.fn().mockName("AuthorizationService.canViewChats")
        };
        chatService = ({
            combineChatReadStatuses: vi.fn().mockName("ChatService.combineChatReadStatuses"),
            getOrCreate: vi.fn().mockName("ChatService.getOrCreate"),
            getChatIsRead$: vi.fn().mockName("ChatService.getChatIsRead$")
        } as unknown as typeof chatService);

        await TestBed.configureTestingModule({
            declarations: [ViewCandidateOppComponent, ChatReadStatusComponent, RouterLinkStubDirective, OpportunityStageNextStepComponent],
            imports: [NgbNavModule],
            providers: [
                { provide: NgbModal, useValue: mockModalService },
                { provide: CandidateOpportunityService, useValue: mockCandidateOpportunityService },
                { provide: AuthenticationService, useValue: mockAuthService },
                { provide: AuthorizationService, useValue: authorizationSpy },
                { provide: ChatService, useValue: chatService },
                provideHttpClient(withXhr(), withInterceptorsFromDi()),
                provideHttpClientTesting()
            ],
            schemas: [CUSTOM_ELEMENTS_SCHEMA]
        }).compileComponents();
        mockAuthService.getLoggedInUser.mockReturnValue({ partner: new MockPartner() });
        authorizationSpy.canViewChats.mockReturnValue(true);
        chatService.getOrCreate.mockReturnValue(of(new MockJobChat()));
        mockCandidateOpportunityService.uploadOffer.mockReturnValue(of(mockCandidateOpportunity));
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(ViewCandidateOppComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should fetch chats when opp input changes', fakeAsync(() => {
        const changes = {
            opp: {
                currentValue: mockCandidateOpportunity,
                previousValue: null,
                firstChange: true,
                isFirstChange: () => true
            }
        };
        component.opp = mockCandidateOpportunity;
        component.ngOnChanges(changes);
        tick();
        // Manually trigger change detection
        fixture.detectChanges();
        expect(component.loading).toBeFalsy();
    }));
    it('should initialize with default values and fetch chats', fakeAsync(() => {
        const changes = {
            opp: {
                currentValue: mockCandidateOpportunity,
                previousValue: null,
                firstChange: true,
                isFirstChange: () => true
            }
        };
        component.opp = mockCandidateOpportunity;
        component.ngOnChanges(changes);
        // Wait for asynchronous operations to complete
        tick();
        // Expectations
        expect(component.error).toBe(null);
        expect(component.candidateChats.length).toEqual(3);
        expect(component.nonCandidateChats.length).toEqual(2);
    }));

    it('should upload job offer successfully', fakeAsync(() => {
        // Mock file
        const mockFile = new File([''], 'job_offer.pdf');
        component.opp = mockCandidateOpportunity;
        const modalRef = {
            componentInstance: {
                maxFiles: 1,
            },
            result: Promise.resolve([mockFile]) // Mock result property to return a resolved promise
        };
        mockModalService.open.mockReturnValue(modalRef);

        // Call the component method
        component.uploadOffer();
        tick();

        // Expectations
        expect(mockModalService.open).toHaveBeenCalledWith(FileSelectorComponent, expect.any(Object));
        expect(mockCandidateOpportunityService.uploadOffer).toHaveBeenCalledWith(mockCandidateOpportunity.id, expect.any(FormData));
        expect(component.saving).toBe(false);
    }));

    it('should update active tab ID when the user changes tabs', () => {
        const activeTabId: string = 'Progress';
        // Trigger tab change
        component.onTabChanged(activeTabId);
        // Expectations
        expect(component.activeTabId).toBe(activeTabId);
    });

    it('should not upload offer if modal is dismissed', fakeAsync(() => {
        const modalRef = {
            componentInstance: { maxFiles: 1 },
            result: Promise.reject('Modal dismissed')
        };
        mockModalService.open.mockReturnValue(modalRef);

        component.uploadOffer();
        tick();

        expect(mockCandidateOpportunityService.uploadOffer).not.toHaveBeenCalled();
        expect(component.saving).toBe(undefined);
    }));


    it('should not crash if opp is null in ngOnChanges', fakeAsync(() => {
        component.opp = null!;
        component.ngOnChanges({
            opp: {
                currentValue: null,
                previousValue: mockCandidateOpportunity,
                firstChange: false,
                isFirstChange: () => false
            }
        });
        tick();
        expect(component.loading).toBe(false);
    }));

});
