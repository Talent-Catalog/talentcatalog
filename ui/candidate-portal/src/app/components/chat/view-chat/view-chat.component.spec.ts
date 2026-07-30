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

import {Component, Input, Pipe, PipeTransform} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {By} from '@angular/platform-browser';

import {JobChat} from '../../../model/chat';
import {ChatService} from '../../../services/chat.service';
import {ViewChatComponent} from './view-chat.component';

@Pipe({
  name: 'translate'
})
class TranslatePipeStub implements PipeTransform {
  transform(value: string): string {
    return value;
  }
}

@Component({
  selector: 'app-chat-read-status',
  template: ''
})
class ChatReadStatusStubComponent {
  @Input() chats: JobChat[];
}

describe('ViewChatComponent', () => {
  let component: ViewChatComponent;
  let fixture: ComponentFixture<ViewChatComponent>;
  let chatServiceSpy: jasmine.SpyObj<ChatService>;
  let chat: JobChat;

  beforeEach(async () => {
    chatServiceSpy = jasmine.createSpyObj<ChatService>(
      'ChatService',
      [
        'getChatInfoParticipantsKey',
        'getChatInfoPurposeKey'
      ]
    );

    chatServiceSpy.getChatInfoParticipantsKey.and.returnValue(
      'CHAT_INFO.PARTICIPANTS.TEST'
    );

    chatServiceSpy.getChatInfoPurposeKey.and.returnValue(
      'CHAT_INFO.PURPOSE.TEST'
    );

    await TestBed.configureTestingModule({
      imports: [CommonModule],
      declarations: [
        ViewChatComponent,
        TranslatePipeStub,
        ChatReadStatusStubComponent
      ],
      providers: [
        {
          provide: ChatService,
          useValue: chatServiceSpy
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ViewChatComponent);
    component = fixture.componentInstance;

    chat = {
      id: 1,
      type: 'Job'
    } as unknown as JobChat;

    component.chat = chat;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialise without changing the supplied chat', () => {
    expect(() => component.ngOnInit()).not.toThrow();
    expect(component.chat).toBe(chat);
  });

  it('should return the participants translation key', () => {
    const result = component.chatParticipantsKey;

    expect(result).toBe(
      'CHAT_INFO.PARTICIPANTS.TEST'
    );

    expect(
      chatServiceSpy.getChatInfoParticipantsKey
    ).toHaveBeenCalledOnceWith(chat.type);
  });

  it('should return the purpose translation key', () => {
    const result = component.chatPurposeKey;

    expect(result).toBe(
      'CHAT_INFO.PURPOSE.TEST'
    );

    expect(
      chatServiceSpy.getChatInfoPurposeKey
    ).toHaveBeenCalledOnceWith(chat.type);
  });

  it('should render the chat information when chat is supplied', () => {
    fixture.detectChanges();

    const container = fixture.debugElement.query(
      By.css('.d-flex')
    );

    expect(container).toBeTruthy();
  });

  it('should render the participant and purpose labels', () => {
    fixture.detectChanges();

    const paragraphs = fixture.debugElement.queryAll(
      By.css('p')
    );

    expect(paragraphs.length).toBe(2);

    const participantsText =
      paragraphs[0].nativeElement.textContent
      .replace(/\s+/g, ' ')
      .trim();

    const purposeText =
      paragraphs[1].nativeElement.textContent
      .replace(/\s+/g, ' ')
      .trim();

    expect(participantsText).toContain(
      'CHAT_INFO.LABEL.PARTICIPANTS'
    );

    expect(participantsText).toContain(
      'CHAT_INFO.PARTICIPANTS.TEST'
    );

    expect(purposeText).toContain(
      'CHAT_INFO.LABEL.PURPOSE'
    );

    expect(purposeText).toContain(
      'CHAT_INFO.PURPOSE.TEST'
    );
  });

  it('should request both translation keys using the chat type', () => {
    fixture.detectChanges();

    expect(
      chatServiceSpy.getChatInfoParticipantsKey
    ).toHaveBeenCalledWith(chat.type);

    expect(
      chatServiceSpy.getChatInfoPurposeKey
    ).toHaveBeenCalledWith(chat.type);
  });

  it('should pass the current chat to the read-status component', () => {
    fixture.detectChanges();

    const readStatusElement = fixture.debugElement.query(
      By.directive(ChatReadStatusStubComponent)
    );

    expect(readStatusElement).toBeTruthy();

    const readStatusComponent =
      readStatusElement.componentInstance as
        ChatReadStatusStubComponent;

    expect(readStatusComponent.chats).toEqual([chat]);
    expect(readStatusComponent.chats[0]).toBe(chat);
  });

  it('should not render chat information when chat is null', () => {
    component.chat = null as any;

    fixture.detectChanges();

    expect(
      fixture.debugElement.query(By.css('.d-flex'))
    ).toBeNull();

    expect(
      fixture.debugElement.query(
        By.directive(ChatReadStatusStubComponent)
      )
    ).toBeNull();

    expect(
      chatServiceSpy.getChatInfoParticipantsKey
    ).not.toHaveBeenCalled();

    expect(
      chatServiceSpy.getChatInfoPurposeKey
    ).not.toHaveBeenCalled();
  });
});
