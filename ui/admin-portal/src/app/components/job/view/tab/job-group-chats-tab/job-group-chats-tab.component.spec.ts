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
import {JobGroupChatsTabComponent} from './job-group-chats-tab.component';
import {ChatService} from '../../../../../services/chat.service';
import {AuthorizationService} from '../../../../../services/authorization.service';
import {Job} from '../../../../../model/job';
import {of, throwError} from 'rxjs';
import {ChatsWithPostsComponent} from "../../../../chat/chats-with-posts/chats-with-posts.component";
import {ChatsComponent} from "../../../../chat/chats/chats.component";
import {MockJob} from "../../../../../MockData/MockJob";
import {MockJobChat} from "../../../../../MockData/MockJobChat";
import {expect, vi} from 'vitest';

describe('JobGroupChatsTabComponent', () => {
    let component: JobGroupChatsTabComponent;
    let fixture: ComponentFixture<JobGroupChatsTabComponent>;
    let chatServiceSpy: MockedObject<ChatService>;
    let authorizationServiceSpy: MockedObject<AuthorizationService>;
    const job: Job = { ...MockJob };
    const allCandidatesChatResponse = new MockJobChat(); // mock response for allCandidatesChat
    const allSourcePartnersChatResponse = new MockJobChat(); // mock response for allSourcePartnersChat

    beforeEach(async () => {
        const chatServiceSpyObj = {
            getOrCreate: vi.fn().mockName("ChatService.getOrCreate"),
            markChatAsRead: vi.fn().mockName("ChatService.markChatAsRead")
        };
        const authorizationServiceSpyObj = {
            isSourcePartner: vi.fn().mockName("AuthorizationService.isSourcePartner")
        };

        await TestBed.configureTestingModule({
            declarations: [JobGroupChatsTabComponent, ChatsWithPostsComponent, ChatsComponent],
            providers: [
                { provide: ChatService, useValue: chatServiceSpyObj },
                { provide: AuthorizationService, useValue: authorizationServiceSpyObj }
            ]
        }).compileComponents();

        chatServiceSpy = TestBed.inject(ChatService) as MockedObject<ChatService>;
        authorizationServiceSpy = TestBed.inject(AuthorizationService) as MockedObject<AuthorizationService>;
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(JobGroupChatsTabComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should fetch job chats', () => {

        setupChatServiceSpies();
        authorizationServiceSpy.isSourcePartner.mockReturnValue(true); // mock isSourcePartner() to return true

        component.job = job;
        component.ngOnChanges({ job: { currentValue: job } as any });

        expect(component.loading).toBeFalsy();
        expect(chatServiceSpy.getOrCreate).toHaveBeenCalledTimes(2);
        expect(authorizationServiceSpy.isSourcePartner).toHaveBeenCalled();
        expect(component.chats.length).toBe(2);
        expect(component.chats[0].name).toBe("All associated with job plus candidates who have accepted job offers");
        expect(component.chats[1].name).toBe("XYZ Partner and all source partners");

    });

    it('should handle error while fetching job chats', () => {
        const errorMessage = 'Error fetching job chats';
        chatServiceSpy.getOrCreate.mockReturnValue(throwError(errorMessage));

        component.job = job;
        component.ngOnChanges({ job: { currentValue: job } as any });

        expect(component.loading).toBeFalsy();
        expect(component.error).toEqual(errorMessage);
    });

    function setupChatServiceSpies(): void {
        chatServiceSpy.getOrCreate.mockImplementation((() => {
      const objectResponses = [of(allCandidatesChatResponse), of(allSourcePartnersChatResponse)];
      return ((request: unknown) =>
        request !== null && typeof request === 'object'
          ? objectResponses.shift()
          : undefined) as typeof chatServiceSpy.getOrCreate;
    })());
    }
});
