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

describe('ViewChatPostsComponent', () => {
  let component: ViewChatPostsComponent;
  let fixture: ComponentFixture<ViewChatPostsComponent>;
  let chatService: jasmine.SpyObj<ChatService>;
  let chatPostService: jasmine.SpyObj<ChatPostService>;

  beforeEach(async () => {
    const chatServiceSpy = jasmine.createSpyObj('ChatService', ['getChatIsRead$','getChatPosts$','list', 'markChatAsRead']);
    const chatPostServiceSpy = jasmine.createSpyObj('ChatPostService', ['listPosts','create', 'update']);

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
    chatService = TestBed.inject(ChatService) as jasmine.SpyObj<ChatService>;
    chatPostService = TestBed.inject(ChatPostService) as jasmine.SpyObj<ChatPostService>;
    chatService.getChatPosts$.and.returnValue(of(new MockChatPost()));
    chatPostService.listPosts.and.returnValue(of([new MockChatPost()]));
    chatService.getChatIsRead$.and.returnValue(of(true));
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
    chatService.list.and.returnValue(of(mockChats));

    component.chat = mockChat;
    component.ngOnChanges({ chat: { currentValue: mockChat, firstChange: true, isFirstChange: () => true, previousValue: undefined } });

    fixture.detectChanges();

    expect(component.chatIsRead).toEqual(true);
    expect(component.loading).toBeFalse();
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
        closest: jasmine.createSpy('closest').and.callFake((selector) => {
          if (selector === 'section') {
            return { classList: ['emoji-picker-class'] };
          } else if (selector === 'button') {
            return { id: 'emojiBtn' };
          }
          return null;
        })
      }
    };
    component.editor = { emojiPickerVisible: true } as CreateUpdatePostComponent;
    component.documentClick(event as any);
    expect(mockPostComponent.reactionPickerVisible).toBeFalse();
  });

  it('should handle no emoji pickers open correctly', () => {
    component.editor = { emojiPickerVisible: false } as CreateUpdatePostComponent;
    const mockPostComponent = { reactionPickerVisible: false } as ViewPostComponent;
    component.viewPostComponents = { find: () => mockPostComponent } as any;
    const event = {
      target: {
        closest: jasmine.createSpy('closest').and.callFake((selector) => {
          if (selector === 'section') {
            return { classList: ['emoji-picker-class'] };
          } else if (selector === 'button') {
            return { id: 'emojiBtn' };
          }
          return null;
        })
      }
    };
    component.documentClick(event);
    expect(component.editor.emojiPickerVisible).toBeFalse();
    expect(mockPostComponent.reactionPickerVisible).toBeFalse();
  });
});
