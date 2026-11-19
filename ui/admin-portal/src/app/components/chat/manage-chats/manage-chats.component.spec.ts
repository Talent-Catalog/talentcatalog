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
import {ManageChatsComponent} from "./manage-chats.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {ChatService} from "../../../services/chat.service";
import {JobChat} from "../../../model/chat";
import {MockJobChat} from "../../../MockData/MockJobChat";
import {NO_ERRORS_SCHEMA} from "@angular/core";
import {of, throwError} from "rxjs";
import {expect, vi} from 'vitest';

describe('ManageChatsComponent', () => {
    let component: ManageChatsComponent;
    let fixture: ComponentFixture<ManageChatsComponent>;
    let chatService: MockedObject<ChatService>;

    const mockChats: JobChat[] = [new MockJobChat()];

    beforeEach(async () => {
        const chatServiceSpy = {
            list: vi.fn().mockName("ChatService.list"),
            create: vi.fn().mockName("ChatService.create")
        };

        await TestBed.configureTestingModule({
            declarations: [ManageChatsComponent],
            providers: [
                { provide: ChatService, useValue: chatServiceSpy }
            ],
            schemas: [NO_ERRORS_SCHEMA] // To ignore subcomponent and directive errors
        }).compileComponents();

        fixture = TestBed.createComponent(ManageChatsComponent);
        component = fixture.componentInstance;
        chatService = TestBed.inject(ChatService) as MockedObject<ChatService>;
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should load chats on init', () => {
        chatService.list.mockReturnValue(of(mockChats));

        fixture.detectChanges();

        expect(component.chats).toEqual(mockChats);
        expect(component.loading).toBe(false);
        expect(component.error).toBeNull();
    });

    it('should handle error on loading chats', () => {
        const errorResponse = { message: 'Error loading chats' };
        chatService.list.mockReturnValue(throwError(errorResponse));

        fixture.detectChanges();

        expect(component.chats.length).toBe(0);
        expect(component.loading).toBe(false);
        expect(component.error).toEqual(errorResponse);
    });

    it('should create a new chat', () => {
        const newChat: JobChat = mockChats[0];
        chatService.create.mockReturnValue(of(newChat));

        component.doNewChat();

        expect(chatService.create).toHaveBeenCalledWith({});
        expect(component.chats.length).toBe(1);
        expect(component.chats[0]).toEqual(newChat);
        expect(component.loading).toBe(false);
        expect(component.error).toBeNull();
    });

    it('should handle error on creating a new chat', () => {
        const errorResponse = { message: 'Error creating chat' };
        chatService.create.mockReturnValue(throwError(errorResponse));

        component.doNewChat();

        expect(component.chats.length).toBe(0);
        expect(component.loading).toBe(false);
        expect(component.error).toEqual(errorResponse);
    });

    it('should set selected chat on chat selection', () => {
        const selectedChat: JobChat = mockChats[0];

        component.onChatSelected(selectedChat);

        expect(component['selectedChat']).toEqual(selectedChat);
    });
});
