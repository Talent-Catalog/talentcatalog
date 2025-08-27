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

import {ComponentFixture, TestBed} from '@angular/core/testing';
import {MockCandidate} from "../../../MockData/MockCandidate";
import {CandidatesWithChatComponent} from './candidates-with-chat.component';
import {Candidate} from "../../../model/candidate";
import {ChatService} from "../../../services/chat.service";
import {MockJobChat} from "../../../MockData/MockJobChat";
import {of} from "rxjs";
import {AuthorizationService} from "../../../services/authorization.service";
import {
  ShowCandidatesWithChatComponent
} from "../show-candidates-with-chat/show-candidates-with-chat.component";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {ReactiveFormsModule, UntypedFormBuilder} from "@angular/forms";
import {SortedByComponent} from "../../util/sort/sorted-by.component";
import {NgbPagination} from "@ng-bootstrap/ng-bootstrap";
import {ViewChatPostsComponent} from "../view-chat-posts/view-chat-posts.component";
import {MockChatPost} from "../../../MockData/MockChatPost";
import {Component, Input} from "@angular/core";
import {ChatPost} from "../../../model/chat";

describe('CandidatesWithChatComponent', () => {
  let component: CandidatesWithChatComponent;
  let fixture: ComponentFixture<CandidatesWithChatComponent>;
  let chatService: jasmine.SpyObj<ChatService>;
  let authorizationService: jasmine.SpyObj<AuthorizationService>;
  const mockJobChat = new MockJobChat();
  const mockCandidate: Candidate = new MockCandidate();

  chatService = jasmine.createSpyObj('ChatService',
    ['getCandidateProspectChat','getChatPosts$','getChatIsRead$']);
  chatService.getCandidateProspectChat.and.returnValue(of(mockJobChat));
  chatService.getChatPosts$.and.returnValue(of(new MockChatPost()));
  chatService.getChatIsRead$.and.returnValue(of(false));

  authorizationService = jasmine.createSpyObj('AuthorizationService', ['isReadOnly']);
  authorizationService.isReadOnly.and.returnValue(true);

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [CandidatesWithChatComponent, ShowCandidatesWithChatComponent,
        SortedByComponent, ViewChatPostsComponent, MockViewPostComponent],
      imports: [HttpClientTestingModule, ReactiveFormsModule, NgbPagination],
      providers: [
        { provide: ChatService, useValue: chatService },
        { provide: AuthorizationService, useValue: authorizationService },
        { provide: UntypedFormBuilder }
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
  })

  it('should assign selected candidate when a candidate is selected', () => {
    component.onCandidateSelected(mockCandidate);

    expect(component.selectedCandidate).toEqual(mockCandidate);
  })

  it('should attempt to fetch the chat with the right parameters when a candidate is selected', () => {
    component.onCandidateSelected(mockCandidate);

    expect(chatService.getCandidateProspectChat).toHaveBeenCalledWith(component.selectedCandidate.id);
  });

  it('should update selectedCandidateChat when the candidate chat is successfully fetched', () => {
    component.onCandidateSelected(mockCandidate);

    expect(component.selectedCandidateChat).toBe(mockJobChat)
  })

  it('should compute chat header when a candidate is selected', () => {
    mockCandidate.user.firstName = 'John'
    mockCandidate.user.lastName = 'Doe'

    component.onCandidateSelected(mockCandidate);

    expect(component.chatHeader).toBe('Chat with John Doe');
  });

  it('should check whether user is read only when candidate is selected and chat fetched',
    () => {
    component.selectedCandidate = mockCandidate;
    component.selectedCandidateChat = mockJobChat;

    fixture.detectChanges()

    expect(authorizationService.isReadOnly).toHaveBeenCalled();
  })

  // Using a mock component here resolves an error caused by the real version's ngOnInit method
  @Component({
    selector: "app-view-post",
    template: "",
  })
  class MockViewPostComponent {
    @Input() post: ChatPost;
    @Input() currentPost: ChatPost;
    @Input() readOnly = false;
  }

});
