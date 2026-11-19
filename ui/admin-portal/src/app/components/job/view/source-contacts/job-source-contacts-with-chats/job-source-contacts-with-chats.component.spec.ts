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
import {JobSourceContactsWithChatsComponent} from './job-source-contacts-with-chats.component';
import {AuthenticationService} from '../../../../../services/authentication.service';
import {AuthorizationService} from '../../../../../services/authorization.service';
import {ChatService} from '../../../../../services/chat.service';
import {Partner} from '../../../../../model/partner';
import {CreateChatRequest, JobChat, JobChatType} from '../../../../../model/chat';
import {Job} from '../../../../../model/job';
import {of} from 'rxjs';
import {ViewJobSourceContactsComponent} from "../view-job-source-contacts/view-job-source-contacts.component";
import {PartnerService} from "../../../../../services/partner.service";
import {provideHttpClientTesting} from "@angular/common/http/testing";
import {provideHttpClient, withInterceptorsFromDi, withXhr} from "@angular/common/http";
import {ViewPostComponent} from "../../../../chat/view-post/view-post.component";
import {ViewChatPostsComponent} from "../../../../chat/view-chat-posts/view-chat-posts.component";
import {CreateUpdatePostComponent} from "../../../../chat/create-update-post/create-update-post.component";
import {ReactiveFormsModule, UntypedFormBuilder} from "@angular/forms";
import {QuillModule} from "ngx-quill";
import {MockPartner} from "../../../../../MockData/MockPartner";
import {MockJobChat} from "../../../../../MockData/MockJobChat";
import {MockJob} from "../../../../../MockData/MockJob";
import {MockChatPost} from "../../../../../MockData/MockChatPost";
import {TranslateModule} from "@ngx-translate/core";
import {NgbModule} from "@ng-bootstrap/ng-bootstrap";
import {expect, vi} from 'vitest';

describe('JobSourceContactsWithChatsComponent', () => {
    let component: JobSourceContactsWithChatsComponent;
    let fixture: ComponentFixture<JobSourceContactsWithChatsComponent>;
    let partnerService: MockedObject<PartnerService>;
    let chatService: MockedObject<ChatService>;
    beforeEach(waitForAsync(() => {
        const authSpy = {
            getLoggedInUser: vi.fn().mockName("AuthenticationService.getLoggedInUser")
        };
        authSpy.getLoggedInUser.mockReturnValue(MockPartner);

        const authzSpy = {
            isViewingAsSource: vi.fn().mockName("AuthorizationService.isViewingAsSource"),
            isSourcePartner: vi.fn().mockName("AuthorizationService.isSourcePartner"),
            isDefaultSourcePartner: vi.fn().mockName("AuthorizationService.isDefaultSourcePartner"),
            isJobCreator: vi.fn().mockName("AuthorizationService.isJobCreator"),
            canViewChats: vi.fn().mockName("AuthorizationService.canViewChats")
        };
        authzSpy.isSourcePartner.mockReturnValue(true);
        authzSpy.isDefaultSourcePartner.mockReturnValue(false);
        authzSpy.canViewChats.mockReturnValue(true);

        chatService = ({
            getOrCreate: vi.fn().mockName("ChatService.getOrCreate"),
            getChatIsRead$: vi.fn().mockName("ChatService.getChatIsRead$"),
            getChatPosts$: vi.fn().mockName("ChatService.getChatPosts$"),
            markChatAsRead: vi.fn().mockName("ChatService.markChatAsRead")
        } as unknown as typeof chatService);
        chatService.getOrCreate.mockReturnValue(of(new MockJobChat()));
        chatService.getChatPosts$.mockReturnValue(of(new MockChatPost()));
        chatService.getChatIsRead$.mockReturnValue(of(false));
        partnerService = ({
            listSourcePartners: vi.fn().mockName("PartnerService.listSourcePartners")
        } as unknown as typeof partnerService);
        partnerService.listSourcePartners.mockReturnValue(of([])); // Mock listSourcePartners response

        // partnerService.listSourcePartners.and.returnValue();
        TestBed.configureTestingModule({
            declarations: [JobSourceContactsWithChatsComponent, ViewPostComponent, CreateUpdatePostComponent, ViewJobSourceContactsComponent, ViewChatPostsComponent],
            imports: [QuillModule, ReactiveFormsModule, TranslateModule.forRoot(), NgbModule],
            providers: [
                { provide: AuthenticationService, useValue: authSpy },
                { provide: AuthorizationService, useValue: authzSpy },
                { provide: ChatService, useValue: chatService },
                { provide: PartnerService, useValue: partnerService },
                { provide: UntypedFormBuilder },
                provideHttpClient(withXhr(), withInterceptorsFromDi()),
                provideHttpClientTesting(),

            ]
        })
            .compileComponents();

    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(JobSourceContactsWithChatsComponent);
        component = fixture.componentInstance;
        partnerService = TestBed.inject(PartnerService) as MockedObject<PartnerService>;
        component.job = MockJob;

        fixture.detectChanges();
    });

    it('should create', () => {

        expect(component).toBeTruthy();

    });

    it('should fetch job chat when source partner is selected', () => {
        const mockJob: Job = MockJob; // Create a mock job
        const mockPartner: Partner = new MockPartner(); // Create a mock partner
        component.job = mockJob;

        component.onSourcePartnerSelected(mockPartner);

        const expectedRequest: CreateChatRequest = {
            type: JobChatType.JobCreatorSourcePartner,
            jobId: mockJob.id,
            sourcePartnerId: mockPartner.id
        };
        expect(chatService.getOrCreate).toHaveBeenCalledWith(expectedRequest);
    });
    it('should update chat header based on user and selected partner', () => {
        const mockJob: Job = MockJob; // Create a mock job
        const mockPartner: Partner = new MockPartner(); // Create a mock partner
        mockPartner.name = 'XYZ';
        component.job = mockJob;
        component.onSourcePartnerSelected(mockPartner);
        expect(component.chatHeader).toBe('Chat with source partner: XYZ');
    });

    it('should mark chat as read when requested', () => {
        const mockChat: JobChat = new MockJobChat(); // Mock chat object
        component.selectedSourcePartnerChat = mockChat;

        component.onMarkChatAsRead();

        expect(chatService.markChatAsRead).toHaveBeenCalledWith(mockChat);
    });
});
