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
import {ViewChatPostsComponent} from "./view-chat-posts.component";
import {ChatService} from "../../../services/chat.service";
import {ChatPostService} from "../../../services/chat-post.service";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {NO_ERRORS_SCHEMA} from "@angular/core";
import {CreateChatRequest, JobChat, JobChatType} from "../../../model/chat";
import {CreateUpdatePostComponent} from "../create-update-post/create-update-post.component";
import {ViewPostComponent} from "../view-post/view-post.component";
import {Partner} from "../../../model/partner";
import {of} from "rxjs";
import {MockChatPost} from "../../../MockData/MockChatPost";
import {expect, vi} from 'vitest';

describe('ViewChatPostsComponent', () => {
    let component: ViewChatPostsComponent;
    let fixture: ComponentFixture<ViewChatPostsComponent>;
    let chatService: MockedObject<ChatService>;
    let chatPostService: MockedObject<ChatPostService>;

    beforeEach(async () => {
        const chatServiceSpy = {
            getChatIsRead$: vi.fn().mockName("ChatService.getChatIsRead$"),
            getChatPosts$: vi.fn().mockName("ChatService.getChatPosts$"),
            list: vi.fn().mockName("ChatService.list"),
            markChatAsRead: vi.fn().mockName("ChatService.markChatAsRead")
        };
        const chatPostServiceSpy = {
            listPosts: vi.fn().mockName("ChatPostService.listPosts"),
            create: vi.fn().mockName("ChatPostService.create"),
            update: vi.fn().mockName("ChatPostService.update")
        };

        await TestBed.configureTestingModule({
            declarations: [ViewChatPostsComponent],
            providers: [
                { provide: ChatService, useValue: chatServiceSpy },
                { provide: ChatPostService, useValue: chatPostServiceSpy }
            ],
            schemas: [NO_ERRORS_SCHEMA]
        }).compileComponents();

        fixture = TestBed.createComponent(ViewChatPostsComponent);
        component = fixture.componentInstance;
        chatService = TestBed.inject(ChatService) as MockedObject<ChatService>;
        chatPostService = TestBed.inject(ChatPostService) as MockedObject<ChatPostService>;
        chatService.getChatPosts$.mockReturnValue(of(new MockChatPost()));
        chatPostService.listPosts.mockReturnValue(of([new MockChatPost()]));
        chatService.getChatIsRead$.mockReturnValue(of(true));
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should initialize correctly', () => {
        component.ngOnInit();
        expect(component.loading).toBeUndefined();
        expect(component.error).toBeUndefined();
    });

    it('should handle chat input changes and call onNewChat', () => {
        const mockChat: JobChat = { id: 1, type: JobChatType.CandidateProspect, name: 'Test Chat' };
        const mockChats: JobChat[] = [mockChat];
        chatService.list.mockReturnValue(of(mockChats));

        component.chat = mockChat;
        component.ngOnChanges({ chat: { currentValue: mockChat, firstChange: true, isFirstChange: () => true, previousValue: undefined } });

        fixture.detectChanges();

        expect(component.chatIsRead).toEqual(true);
        expect(component.loading).toBe(false);
    });

    it('should fetch job chat if chat input does not change', () => {
        const mockRequest: CreateChatRequest = {
            type: JobChatType.AllJobCandidates,
            candidateId: 1,
            jobId: 2,
            sourcePartnerId: 3
        };

        component.jobChatType = JobChatType.AllJobCandidates;
        component.candidateId = 1;
        component.jobId = 2;
        component.sourcePartner = { id: 3 } as Partner;
        // Mock requestJobChat method directly
        component['requestJobChat'] = (request: CreateChatRequest) => {
            expect(request).toEqual(mockRequest);
        };

        component.ngOnChanges({ candidateId: { currentValue: 1, firstChange: true, isFirstChange: () => true, previousValue: undefined } });
    });


    it('should call markChatAsRead on chatService when onMarkChatAsRead is called', () => {
        const mockChat: JobChat = { id: 1, type: JobChatType.CandidateProspect, name: 'Test Chat' };
        component.chat = mockChat;

        component.onMarkChatAsRead();
        expect(chatService.markChatAsRead).toHaveBeenCalledWith(mockChat);
    });

    it('should close post reaction picker if click is on emoji picker button', () => {
        const mockPostComponent = { reactionPickerVisible: true } as ViewPostComponent;
        component.viewPostComponents = { find: () => mockPostComponent } as any;
        const event = {
            target: {
                closest: vi.fn().mockName('closest').mockImplementation((selector) => {
                    if (selector === 'section') {
                        return { classList: ['emoji-picker-class'] };
                    }
                    else if (selector === 'button') {
                        return { id: 'emojiBtn' };
                    }
                    return null;
                })
            }
        };
        component.editor = { emojiPickerVisible: true } as CreateUpdatePostComponent;
        component.documentClick(event as any);
        expect(mockPostComponent.reactionPickerVisible).toBe(false);
    });

    it('should handle no emoji pickers open correctly', () => {
        component.editor = { emojiPickerVisible: false } as CreateUpdatePostComponent;
        const mockPostComponent = { reactionPickerVisible: false } as ViewPostComponent;
        component.viewPostComponents = { find: () => mockPostComponent } as any;
        const event = {
            target: {
                closest: vi.fn().mockName('closest').mockImplementation((selector) => {
                    if (selector === 'section') {
                        return { classList: ['emoji-picker-class'] };
                    }
                    else if (selector === 'button') {
                        return { id: 'emojiBtn' };
                    }
                    return null;
                })
            }
        };
        component.documentClick(event);
        expect(component.editor.emojiPickerVisible).toBe(false);
        expect(mockPostComponent.reactionPickerVisible).toBe(false);
    });
});
