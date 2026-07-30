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

import {CommonModule} from '@angular/common';
import {SimpleChange} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {By} from '@angular/platform-browser';
import {of, Subject, throwError} from 'rxjs';

import {JobChat} from '../../../model/chat';
import {ChatService} from '../../../services/chat.service';
import {ChatReadStatusComponent} from './chat-read-status.component';

describe('ChatReadStatusComponent', () => {
  let component: ChatReadStatusComponent;
  let fixture: ComponentFixture<ChatReadStatusComponent>;
  let chatServiceSpy: jasmine.SpyObj<ChatService>;

  const chatOne = {id: 1} as JobChat;
  const chatTwo = {id: 2} as JobChat;

  beforeEach(async () => {
    chatServiceSpy = jasmine.createSpyObj<ChatService>(
      'ChatService',
      ['getChatIsRead$', 'combineChatReadStatuses']
    );

    await TestBed.configureTestingModule({
      imports: [CommonModule],
      declarations: [ChatReadStatusComponent],
      providers: [
        {
          provide: ChatService,
          useValue: chatServiceSpy
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ChatReadStatusComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should subscribe to the status of a single chat', () => {
    chatServiceSpy.getChatIsRead$.and.returnValue(of(false));
    component.chats = [chatOne];

    component.ngOnChanges({
      chats: new SimpleChange(undefined, component.chats, true)
    });

    expect(chatServiceSpy.getChatIsRead$)
    .toHaveBeenCalledOnceWith(chatOne);
    expect(component.unreadIndicator).toBe('*');
  });

  it('should combine the statuses of multiple chats', () => {
    const consoleSpy = spyOn(console, 'log');
    const chats = [chatOne, chatTwo];

    chatServiceSpy.combineChatReadStatuses.and.returnValue(of(true));
    component.chats = chats;

    component.ngOnChanges({
      chats: new SimpleChange(undefined, chats, true)
    });

    expect(chatServiceSpy.combineChatReadStatuses)
    .toHaveBeenCalledOnceWith(chats);
    expect(consoleSpy).toHaveBeenCalledOnceWith(
      'ChatReadStatus subscribing to chats 1,2'
    );
    expect(component.unreadIndicator).toBe('');
  });

  it('should not subscribe when the chat array is empty', () => {
    component.chats = [];

    component.ngOnChanges({
      chats: new SimpleChange(undefined, [], true)
    });

    expect(chatServiceSpy.getChatIsRead$).not.toHaveBeenCalled();
    expect(chatServiceSpy.combineChatReadStatuses)
    .not.toHaveBeenCalled();
  });

  it('should handle a null chat collection', () => {
    component.chats = null;

    expect(() => {
      component.ngOnChanges({
        chats: new SimpleChange(undefined, null, true)
      });
    }).not.toThrow();

    expect(chatServiceSpy.getChatIsRead$).not.toHaveBeenCalled();
  });

  it('should show unread when the supplied observable emits false', () => {
    component.observable = of(false);

    component.ngOnChanges({
      observable: new SimpleChange(
        undefined,
        component.observable,
        true
      )
    });

    expect(component.unreadIndicator).toBe('*');
  });

  it('should show no indicator when the observable emits true', () => {
    component.observable = of(true);

    component.ngOnChanges({
      observable: new SimpleChange(
        undefined,
        component.observable,
        true
      )
    });

    expect(component.unreadIndicator).toBe('');
  });

  it('should show unknown status when the observable emits null', () => {
    component.observable = of(null);

    component.ngOnChanges({
      observable: new SimpleChange(
        undefined,
        component.observable,
        true
      )
    });

    expect(component.unreadIndicator).toBe('?');
  });

  it('should show unknown status when the observable fails', () => {
    component.observable = throwError('Unable to read status');

    component.ngOnChanges({
      observable: new SimpleChange(
        undefined,
        component.observable,
        true
      )
    });

    expect(component.unreadIndicator).toBe('?');
  });

  it('should handle an undefined observable', () => {
    component.observable = undefined;

    expect(() => {
      component.ngOnChanges({
        observable: new SimpleChange(undefined, undefined, true)
      });
    }).not.toThrow();
  });

  it('should unsubscribe from the previous observable when changed', () => {
    const firstStatus$ = new Subject<boolean>();
    const secondStatus$ = new Subject<boolean>();

    component.observable = firstStatus$;
    component.ngOnChanges({
      observable: new SimpleChange(undefined, firstStatus$, true)
    });

    firstStatus$.next(false);
    expect(component.unreadIndicator).toBe('*');

    component.observable = secondStatus$;
    component.ngOnChanges({
      observable: new SimpleChange(
        firstStatus$,
        secondStatus$,
        false
      )
    });

    firstStatus$.next(true);
    expect(component.unreadIndicator).toBe('*');

    secondStatus$.next(true);
    expect(component.unreadIndicator).toBe('');
  });

  it('should unsubscribe when destroyed', () => {
    const status$ = new Subject<boolean>();

    component.observable = status$;
    component.ngOnChanges({
      observable: new SimpleChange(undefined, status$, true)
    });

    const subscription = (component as any).subscription;

    expect(subscription.closed).toBeFalse();

    component.ngOnDestroy();

    expect(subscription.closed).toBeTrue();
    expect((component as any).subscription).toBeNull();
  });

  it('should safely destroy without an active subscription', () => {
    expect(() => component.ngOnDestroy()).not.toThrow();
  });

  it('should render the notification dot for unread chats', () => {
    component.unreadIndicator = '*';

    fixture.detectChanges();

    expect(
      fixture.debugElement.query(By.css('.notification-dot'))
    ).toBeTruthy();
  });

  it('should render a question mark for unknown status', () => {
    component.unreadIndicator = '?';

    fixture.detectChanges();

    const element = fixture.debugElement.query(
      By.css('span span')
    );

    expect(element.nativeElement.textContent.trim()).toBe('?');
  });

  it('should render no status element when all chats are read', () => {
    component.unreadIndicator = '';

    fixture.detectChanges();

    expect(
      fixture.debugElement.query(By.css('span'))
    ).toBeNull();
  });
});
