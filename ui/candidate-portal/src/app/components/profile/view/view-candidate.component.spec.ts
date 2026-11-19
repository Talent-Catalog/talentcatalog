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
import {CommonModule, Location} from '@angular/common';
import {NO_ERRORS_SCHEMA, Pipe, PipeTransform} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {ActivatedRoute} from '@angular/router';
import {NgbNavChangeEvent, NgbNavModule} from '@ng-bootstrap/ng-bootstrap';
import {of} from 'rxjs';

import {ViewCandidateComponent} from './view-candidate.component';
import {AuthorizationService} from '../../../services/authorization.service';
import {CandidateService} from '../../../services/candidate.service';
import {ChatService} from '../../../services/chat.service';
import {LinkedinService} from '../../../services/linkedin.service';
import {CasiPortalService} from '../../../services/casi-portal.service';
import {LocalStorageService} from '../../../services/local-storage.service';
import {AuthenticationService} from '../../../services/authentication.service';
import {environment} from '../../../../environments/environment';

@Pipe({ name: 'translate', standalone: false })
class TranslatePipeStub implements PipeTransform {
    transform(value: string): string {
        return value;
    }
}

describe('ViewCandidateComponent', () => {
    let component: ViewCandidateComponent;
    let fixture: ComponentFixture<ViewCandidateComponent>;

    let authorizationService: MockedObject<AuthorizationService>;
    let candidateService: MockedObject<CandidateService>;
    let chatService: MockedObject<ChatService>;
    let linkedinService: MockedObject<LinkedinService>;
    let casiPortalService: MockedObject<CasiPortalService>;
    let localStorageService: MockedObject<LocalStorageService>;
    let location: MockedObject<Location>;
    let authenticationService: MockedObject<AuthenticationService>;
    let originalEnvironmentName: string;

    const candidate = {
        id: 1,
        status: 'active',
        surveyType: {
            id: 1
        },
        taskAssignments: [],
        candidateOpportunities: []
    };

    beforeEach(async () => {
        originalEnvironmentName = environment.environmentName;
        authorizationService = {
            canViewChats: vi.fn().mockName("AuthorizationService.canViewChats")
        } as unknown as MockedObject<AuthorizationService>;

        candidateService = {
            getProfile: vi.fn().mockName("CandidateService.getProfile")
        } as unknown as MockedObject<CandidateService>;

        chatService = {
            getCandidateProspectChat: vi.fn().mockName("ChatService.getCandidateProspectChat"),
            getJobChatUserInfo: vi.fn().mockName("ChatService.getJobChatUserInfo"),
            getChatPosts$: vi.fn().mockName("ChatService.getChatPosts$"),
            getOrCreate: vi.fn().mockName("ChatService.getOrCreate"),
            removeDuplicateChats: vi.fn().mockName("ChatService.removeDuplicateChats"),
            markChatAsRead: vi.fn().mockName("ChatService.markChatAsRead")
        } as unknown as MockedObject<ChatService>;

        linkedinService = {
            isEligible: vi.fn().mockName("LinkedinService.isEligible")
        } as unknown as MockedObject<LinkedinService>;

        casiPortalService = {
            checkEligibility: vi.fn().mockName("CasiPortalService.checkEligibility")
        } as unknown as MockedObject<CasiPortalService>;

        localStorageService = {
            get: vi.fn().mockName("LocalStorageService.get"),
            set: vi.fn().mockName("LocalStorageService.set")
        } as unknown as MockedObject<LocalStorageService>;

        location = {
            path: vi.fn().mockName("Location.path"),
            replaceState: vi.fn().mockName("Location.replaceState")
        } as unknown as MockedObject<Location>;

        authenticationService = {
            isGrnInstance: vi.fn().mockName("AuthenticationService.isGrnInstance")
        } as unknown as MockedObject<AuthenticationService>;

        authorizationService.canViewChats.mockReturnValue(false);
        candidateService.getProfile.mockReturnValue(of(candidate as any));

        linkedinService.isEligible.mockReturnValue(of(false));
        casiPortalService.checkEligibility.mockReturnValue(of(false));

        localStorageService.get.mockReturnValue(null);

        location.path.mockReturnValue('/profile');

        authenticationService.isGrnInstance.mockReturnValue(false);

        await TestBed.configureTestingModule({
            imports: [
                CommonModule,
                NgbNavModule
            ],
            declarations: [
                ViewCandidateComponent,
                TranslatePipeStub
            ],
            providers: [
                {
                    provide: AuthorizationService,
                    useValue: authorizationService
                },
                {
                    provide: CandidateService,
                    useValue: candidateService
                },
                {
                    provide: ChatService,
                    useValue: chatService
                },
                {
                    provide: LinkedinService,
                    useValue: linkedinService
                },
                {
                    provide: CasiPortalService,
                    useValue: casiPortalService
                },
                {
                    provide: LocalStorageService,
                    useValue: localStorageService
                },
                {
                    provide: Location,
                    useValue: location
                },
                {
                    provide: ActivatedRoute,
                    useValue: {
                        queryParams: of({})
                    }
                },
                {
                    provide: AuthenticationService,
                    useValue: authenticationService
                }
            ],
            schemas: [NO_ERRORS_SCHEMA]
        }).compileComponents();

        fixture = TestBed.createComponent(ViewCandidateComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    afterEach(() => {
        environment.environmentName = originalEnvironmentName;
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should fetch the candidate profile', () => {
        expect(candidateService.getProfile).toHaveBeenCalled();
        expect(component.candidate).toEqual(candidate as any);
        expect(component.loading).toBe(false);
    });

    it('should use the profile tab by default when there is no cached tab', () => {
        expect(component.activeTabId).toBe('Profile');
        expect(location.replaceState).toHaveBeenCalledWith('/profile?tab=Profile');
    });

    it('should update the active tab when the tab changes', () => {
        const event = {
            nextId: 'Tasks'
        } as NgbNavChangeEvent;

        component.onTabChanged(event);

        expect(component.activeTabId).toBe('Tasks');
        expect(localStorageService.set).toHaveBeenCalledWith('CandidateLastTab', 'Tasks');
        expect(location.replaceState).toHaveBeenCalledWith('/profile?tab=Tasks');
    });

    it('should not show the jobs tab when there are no filtered opportunities', () => {
        expect(component.canSeeJobTab).toBe(false);
    });

    it('should not show the chat tab when the user cannot view chats', () => {
        expect(component.canSeeChatTab).toBe(false);
    });

    it('should expose PiFi eligibility', async () => {
        component.pifiEligible$.subscribe(value => {
            expect(value).toBe(false);
            ;
        });
    });

    it('should show the services tab when the candidate is eligible for PiFi', async () => {
        casiPortalService.checkEligibility.mockImplementation((provider: string) => of(provider === 'PIFI'));
        fixture = TestBed.createComponent(ViewCandidateComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();

        component.pifiEligible$.subscribe(pifiEligible => {
            expect(pifiEligible).toBe(true);
            component.showServicesTab$.subscribe(showServicesTab => {
                expect(showServicesTab).toBe(true);
                ;
            });
        });
    });

    it('should expose Verify+ eligibility for GRN local', async () => {
        environment.environmentName = 'local';
        authenticationService.isGrnInstance.mockReturnValue(true);
        fixture = TestBed.createComponent(ViewCandidateComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();

        component.verifyPlusEligible$.subscribe(verifyPlusEligible => {
            expect(verifyPlusEligible).toBe(true);
            ;
        });
    });

    it('should expose Verify+ eligibility for GRN staging', async () => {
        environment.environmentName = 'staging';
        authenticationService.isGrnInstance.mockReturnValue(true);
        fixture = TestBed.createComponent(ViewCandidateComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();

        component.verifyPlusEligible$.subscribe(verifyPlusEligible => {
            expect(verifyPlusEligible).toBe(true);
            ;
        });
    });

    it('should hide Verify+ eligibility for GRN prod', async () => {
        environment.environmentName = 'prod';
        authenticationService.isGrnInstance.mockReturnValue(true);
        fixture = TestBed.createComponent(ViewCandidateComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();

        component.verifyPlusEligible$.subscribe(verifyPlusEligible => {
            expect(verifyPlusEligible).toBe(false);
            ;
        });
    });

    it('should hide Verify+ eligibility for TBB local', async () => {
        environment.environmentName = 'local';
        authenticationService.isGrnInstance.mockReturnValue(false);
        fixture = TestBed.createComponent(ViewCandidateComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();

        component.verifyPlusEligible$.subscribe(verifyPlusEligible => {
            expect(verifyPlusEligible).toBe(false);
            ;
        });
    });

    it('should show the services tab when Verify+ is the only eligible service in staging GRN', async () => {
        environment.environmentName = 'staging';
        authenticationService.isGrnInstance.mockReturnValue(true);
        linkedinService.isEligible.mockReturnValue(of(false));
        casiPortalService.checkEligibility.mockReturnValue(of(false));
        fixture = TestBed.createComponent(ViewCandidateComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();

        component.showServicesTab$.subscribe(showServicesTab => {
            expect(showServicesTab).toBe(true);
            ;
        });
    });

    it('should hide the services tab when Verify+ is the only GRN service in prod', async () => {
        environment.environmentName = 'prod';
        authenticationService.isGrnInstance.mockReturnValue(true);
        linkedinService.isEligible.mockReturnValue(of(false));
        casiPortalService.checkEligibility.mockReturnValue(of(false));
        fixture = TestBed.createComponent(ViewCandidateComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();

        component.showServicesTab$.subscribe(showServicesTab => {
            expect(showServicesTab).toBe(false);
            ;
        });
    });
});
