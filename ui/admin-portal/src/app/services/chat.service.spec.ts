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

import {TestBed} from '@angular/core/testing';
import {HttpClientTestingModule, HttpTestingController} from '@angular/common/http/testing';
import {of} from 'rxjs';
import {ChatService} from './chat.service';
import {RxStompService} from './rx-stomp.service';
import {AuthenticationService} from './authentication.service';
import {environment} from '../../environments/environment';
import {ChatPost, CreateChatRequest, JobChat, JobChatType, JobChatUserInfo} from '../model/chat';
import {Message} from '@stomp/stompjs';
import {MockUser} from "../MockData/MockUser";

describe('ChatService', () => {
  let service: ChatService;
  let httpMock: HttpTestingController;
  let rxStompServiceMock: jasmine.SpyObj<RxStompService>;
  let authenticationServiceMock: jasmine.SpyObj<AuthenticationService>;

  beforeEach(() => {
    const rxStompServiceSpy = jasmine.createSpyObj('RxStompService', ['watch', 'configure', 'activate', 'deactivate']);
    const authenticationServiceSpy = jasmine.createSpyObj('AuthenticationService', ['getLoggedInUser', 'getToken', 'logout']);

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        ChatService,
        { provide: RxStompService, useValue: rxStompServiceSpy },
        { provide: AuthenticationService, useValue: authenticationServiceSpy }
      ]
    });

    service = TestBed.inject(ChatService);
    httpMock = TestBed.inject(HttpTestingController);
    rxStompServiceMock = TestBed.inject(RxStompService) as jasmine.SpyObj<RxStompService>;
    authenticationServiceMock = TestBed.inject(AuthenticationService) as jasmine.SpyObj<AuthenticationService>;
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('create', () => {
    it('should create a new chat and return the chat object', () => {
      const request: CreateChatRequest = {
        type: JobChatType.AllJobCandidates,
        jobId:1,
        sourcePartnerId:1,
        candidateId: 1
      };
      const mockChat: JobChat = { id: 1, type: JobChatType.CandidateProspect };

      service.create(request).subscribe(chat => {
        expect(chat).toEqual(mockChat);
      });

      const req = httpMock.expectOne(`${environment.chatApiUrl}/chat`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(request);
      req.flush(mockChat);
    });
  });

  describe('getCandidateProspectChat', () => {
    it('should return the candidate prospect chat object', () => {
      const candidateId = 1;
      const mockChat: JobChat = { id: 1, type: JobChatType.CandidateProspect };

      service.getCandidateProspectChat(candidateId).subscribe(chat => {
        expect(chat).toEqual(mockChat);
      });

      const req = httpMock.expectOne(`${environment.chatApiUrl}/chat/${candidateId}/get-cp-chat`);
      expect(req.request.method).toBe('GET');
      req.flush(mockChat);
    });
  });

  describe('getOrCreate', () => {
    it('should return cached chat if already exists', () => {
      const request: CreateChatRequest = {
        type: JobChatType.AllJobCandidates,
        jobId:1,
        sourcePartnerId:1,
        candidateId: 1
      };
      const mockChat: JobChat = { id: 1, type: JobChatType.CandidateProspect };

      spyOn(service['http'], 'post').and.returnValue(of(mockChat));
      service.getOrCreate(request).subscribe(chat => {
        expect(chat).toEqual(mockChat);
      });

      const chat$ = service['chatByRequest$'].get(JSON.stringify(request));
      chat$.subscribe(chat => {
        expect(chat).toEqual(mockChat);
      });
    });

    it('should fetch and cache the chat if not already cached', () => {
      const request: CreateChatRequest = {
        type: JobChatType.AllJobCandidates,
        jobId: 1,
        sourcePartnerId: 1,
        candidateId: 1
      };
      const mockChat: JobChat = { id: 1, type: JobChatType.CandidateProspect };

      const chat$ = service.getOrCreate(request);

      chat$.subscribe(chat => {
        expect(chat).toEqual(mockChat);
      });

      const req = httpMock.expectOne(`${environment.chatApiUrl}/chat/get-or-create`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(request);
      req.flush(mockChat);
    });

  });

  describe('getChatPosts$', () => {
    it('should map messages to ChatPost objects', () => {
      const mockChat: JobChat = { id: 1, type: JobChatType.CandidateProspect};
      const mockMessage: Message = {
        body: JSON.stringify({ id: 1, content: 'Test' })
      } as Message;
      const mockPost: ChatPost = {
        id:1,
        content:'Test'
      } as ChatPost;

      rxStompServiceMock.watch.and.returnValue(of(mockMessage));
      spyOn(service as any, 'watchChat').and.returnValue(of(mockMessage));
      spyOn(service['http'], 'post').and.returnValue(of(mockChat));

      service.getChatPosts$(mockChat).subscribe(post => {
        expect(post).toEqual(mockPost);
       });
    });
  });

  describe('getJobChatUserInfo', () => {
    it('should return JobChatUserInfo object', () => {
      const mockChat: JobChat = { id: 1, type: JobChatType.CandidateProspect };
      const mockUserInfo: JobChatUserInfo = {
        numberUnreadChats: 1,
        lastReadPostId: 2,
        lastPostId: 3
      };
      const user = new MockUser();

      authenticationServiceMock.getLoggedInUser.and.returnValue(user);

      service.getJobChatUserInfo(mockChat).subscribe(userInfo => {
        expect(userInfo).toEqual(mockUserInfo);
      });

      const req = httpMock.expectOne(`${environment.chatApiUrl}/chat/${mockChat.id}/user/${user.id}/get-chat-user-info`);
      expect(req.request.method).toBe('GET');
      req.flush(mockUserInfo);
    });
  });

  describe('markChatAsRead', () => {
    it('should mark chat as read', () => {
      const mockChat: JobChat = { id: 1, type: JobChatType.CandidateProspect };

      spyOn(service as any, 'markAsReadUptoOnServer').and.returnValue(of(void 0));
      spyOn(service as any, 'changeChatReadStatus');

      service.markChatAsRead(mockChat);

      expect(service['markAsReadUptoOnServer']).toHaveBeenCalledWith(mockChat);
      expect(service['changeChatReadStatus']).toHaveBeenCalledWith(mockChat, true);
    });
  });

  describe('removeDuplicateChats', () => {
    it('should remove duplicate chats', () => {
      const chats: JobChat[] = [
        { id: 1, type: JobChatType.CandidateProspect },
        { id: 2, type: JobChatType.CandidateProspect },
        { id: 1, type: JobChatType.CandidateProspect }
      ];

      const result = service.removeDuplicateChats(chats);
      expect(result.length).toBe(2);
      expect(result[0].id).toBe(1);
      expect(result[1].id).toBe(2);
    });
  });
});
