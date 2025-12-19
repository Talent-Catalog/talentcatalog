/*
 * Copyright (c) 2024 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

import {ManageChatsComponent} from "./manage-chats.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {ChatService} from "../../../services/chat.service";
import {JobChat} from "../../../model/chat";
import {MockJobChat} from "../../../MockData/MockJobChat";
import {NO_ERRORS_SCHEMA} from "@angular/core";
import {of, throwError} from "rxjs";

describe('ManageChatsComponent', () => {
  let component: ManageChatsComponent;
  let fixture: ComponentFixture<ManageChatsComponent>;
  let chatService: jasmine.SpyObj<ChatService>;

  const mockChats: JobChat[] = [new MockJobChat()];

  beforeEach(async () => {
    const chatServiceSpy = jasmine.createSpyObj('ChatService', ['list', 'create']);

    await TestBed.configureTestingModule({
      declarations: [ManageChatsComponent],
      providers: [
        { provide: ChatService, useValue: chatServiceSpy }
      ],
      schemas: [NO_ERRORS_SCHEMA]  // To ignore subcomponent and directive errors
    }).compileComponents();

    fixture = TestBed.createComponent(ManageChatsComponent);
    component = fixture.componentInstance;
    chatService = TestBed.inject(ChatService) as jasmine.SpyObj<ChatService>;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load chats on init', () => {
    chatService.list.and.returnValue(of(mockChats));

    fixture.detectChanges();

    expect(component.chats).toEqual(mockChats);
    expect(component.loading).toBeFalse();
    expect(component.error).toBeNull();
  });

  it('should handle error on loading chats', () => {
    const errorResponse = { message: 'Error loading chats' };
    chatService.list.and.returnValue(throwError(errorResponse));

    fixture.detectChanges();

    expect(component.chats.length).toBe(0);
    expect(component.loading).toBeFalse();
    expect(component.error).toEqual(errorResponse);
  });

  it('should create a new chat', () => {
    const newChat: JobChat = mockChats[0]
    chatService.create.and.returnValue(of(newChat));

    component.doNewChat();

    expect(chatService.create).toHaveBeenCalledWith({});
    expect(component.chats.length).toBe(1);
    expect(component.chats[0]).toEqual(newChat);
    expect(component.loading).toBeFalse();
    expect(component.error).toBeNull();
  });

  it('should handle error on creating a new chat', () => {
    const errorResponse = { message: 'Error creating chat' };
    chatService.create.and.returnValue(throwError(errorResponse));

    component.doNewChat();

    expect(component.chats.length).toBe(0);
    expect(component.loading).toBeFalse();
    expect(component.error).toEqual(errorResponse);
  });

  it('should set selected chat on chat selection', () => {
    const selectedChat: JobChat = mockChats[0];

    component.onChatSelected(selectedChat);

    expect(component['selectedChat']).toEqual(selectedChat);
  });
});
