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
import {MockCandidate} from "../../../MockData/MockCandidate";
import {CandidatesWithChatComponent} from './candidates-with-chat.component';
import {Candidate} from "../../../model/candidate";
import {ChatService} from "../../../services/chat.service";
import {MockJobChat} from "../../../MockData/MockJobChat";
import {of} from "rxjs";
import {AuthorizationService} from "../../../services/authorization.service";
import {ShowCandidatesWithChatComponent} from "../show-candidates-with-chat/show-candidates-with-chat.component";
import {provideHttpClientTesting} from "@angular/common/http/testing";
import {provideHttpClient, withInterceptorsFromDi, withXhr} from "@angular/common/http";
import {ReactiveFormsModule, UntypedFormBuilder} from "@angular/forms";
import {SortedByComponent} from "../../util/sort/sorted-by.component";
import {NgbPagination} from "@ng-bootstrap/ng-bootstrap";
import {ViewChatPostsComponent} from "../view-chat-posts/view-chat-posts.component";
import {MockChatPost} from "../../../MockData/MockChatPost";
import {Component, Input} from "@angular/core";
import {ChatPost} from "../../../model/chat";
import {expect, vi} from 'vitest';

describe('CandidatesWithChatComponent', () => {
    let component: CandidatesWithChatComponent;
    let fixture: ComponentFixture<CandidatesWithChatComponent>;
    let chatService: MockedObject<ChatService>;
    let authorizationService: MockedObject<AuthorizationService>;
    const mockJobChat = new MockJobChat();
    const mockCandidate: Candidate = new MockCandidate();

    chatService = ({
        getCandidateProspectChat: vi.fn().mockName("ChatService.getCandidateProspectChat"),
        getChatPosts$: vi.fn().mockName("ChatService.getChatPosts$"),
        getChatIsRead$: vi.fn().mockName("ChatService.getChatIsRead$")
    } as unknown as typeof chatService);
    chatService.getCandidateProspectChat.mockReturnValue(of(mockJobChat));
    chatService.getChatPosts$.mockReturnValue(of(new MockChatPost()));
    chatService.getChatIsRead$.mockReturnValue(of(false));

    authorizationService = ({
        isReadOnly: vi.fn().mockName("AuthorizationService.isReadOnly")
    } as unknown as typeof authorizationService);
    authorizationService.isReadOnly.mockReturnValue(true);

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [CandidatesWithChatComponent, ShowCandidatesWithChatComponent,
                SortedByComponent, ViewChatPostsComponent, MockViewPostComponent],
            imports: [ReactiveFormsModule, NgbPagination],
            providers: [
                { provide: ChatService, useValue: chatService },
                { provide: AuthorizationService, useValue: authorizationService },
                { provide: UntypedFormBuilder },
                provideHttpClient(withXhr(), withInterceptorsFromDi()),
                provideHttpClientTesting()
            ]
        })
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(CandidatesWithChatComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should initialize with default values and configuration', () => {
        // Check if component initializes with default values
        expect(component.error).toBeUndefined();
        expect(component.loading).toBeFalsy();
        expect(component.selectedCandidate).toBeUndefined();
        expect(component.selectedCandidateChat).toBeUndefined();
        expect(component.chatHeader).toBe('');
    });

    it('should assign selected candidate when a candidate is selected', () => {
        component.onCandidateSelected(mockCandidate);

        expect(component.selectedCandidate).toEqual(mockCandidate);
    });

    it('should attempt to fetch the chat with the right parameters when a candidate is selected', () => {
        component.onCandidateSelected(mockCandidate);

        expect(chatService.getCandidateProspectChat).toHaveBeenCalledWith(component.selectedCandidate.id);
    });

    it('should update selectedCandidateChat when the candidate chat is successfully fetched', () => {
        component.onCandidateSelected(mockCandidate);

        expect(component.selectedCandidateChat).toBe(mockJobChat);
    });

    it('should compute chat header when a candidate is selected', () => {
        mockCandidate.user.firstName = 'John';
        mockCandidate.user.lastName = 'Doe';

        component.onCandidateSelected(mockCandidate);

        expect(component.chatHeader).toBe('Chat with John Doe');
    });

    it('should check whether user is read only when candidate is selected and chat fetched', () => {
        component.selectedCandidate = mockCandidate;
        component.selectedCandidateChat = mockJobChat;

        fixture.detectChanges();

        expect(authorizationService.isReadOnly).toHaveBeenCalled();
    });

    // Using a mock component here resolves an error caused by the real version's ngOnInit method
    @Component({
        selector: "app-view-post",
        template: "",
        standalone: false
    })
    class MockViewPostComponent {
        @Input()
        post: ChatPost;
        @Input()
        currentPost: ChatPost;
        @Input()
        readOnly = false;
    }

});
