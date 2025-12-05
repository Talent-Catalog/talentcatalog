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

import { ComponentFixture, fakeAsync, TestBed, tick } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { BehaviorSubject, Subject } from 'rxjs';
import { ChatbotComponent } from './chatbot.component';
import { ChatbotService } from '../../services/chatbot.service';
import { ChatbotMessage } from '../../model/chatbot-message';

describe('ChatbotComponent', () => {
  let component: ChatbotComponent;
  let fixture: ComponentFixture<ChatbotComponent>;
  let chatbotServiceSpy: jasmine.SpyObj<ChatbotService>;
  
  // Mock observables
  let isOpenSubject: BehaviorSubject<boolean>;
  let messagesSubject: BehaviorSubject<ChatbotMessage[]>;
  let isUnavailableSubject: BehaviorSubject<boolean>;
  let isLoadingSubject: BehaviorSubject<boolean>;
  let errorSubject: Subject<string>;

  beforeEach(async () => {
    // Initialize mock observables
    isOpenSubject = new BehaviorSubject<boolean>(false);
    messagesSubject = new BehaviorSubject<ChatbotMessage[]>([]);
    isUnavailableSubject = new BehaviorSubject<boolean>(false);
    isLoadingSubject = new BehaviorSubject<boolean>(false);
    errorSubject = new Subject<string>();

    // Create spy object
    const spy = jasmine.createSpyObj('ChatbotService', [
      'toggleChatbot',
      'openChatbot',
      'closeChatbot',
      'sendMessage'
    ]);
    
    // Assign observables to spy
    spy.isOpen$ = isOpenSubject.asObservable();
    spy.messages$ = messagesSubject.asObservable();
    spy.isUnavailable$ = isUnavailableSubject.asObservable();
    spy.isLoading$ = isLoadingSubject.asObservable();
    spy.error$ = errorSubject.asObservable();

    await TestBed.configureTestingModule({
      declarations: [ChatbotComponent],
      imports: [FormsModule],
      providers: [
        { provide: ChatbotService, useValue: spy }
      ],
      schemas: [NO_ERRORS_SCHEMA] // Ignore unknown elements like fa-icon
    }).compileComponents();

    chatbotServiceSpy = TestBed.inject(ChatbotService) as jasmine.SpyObj<ChatbotService>;
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ChatbotComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('Integration with ChatbotService', () => {
    it('should open when service isOpen$ emits true', fakeAsync(() => {
      expect(component.isOpen).toBe(false);
      
      isOpenSubject.next(true);
      tick();
      
      expect(component.isOpen).toBe(true);
      
      // Flush any pending timers from component (focus input, scroll)
      tick(100);
    }));

    it('should display messages when service messages$ emits', fakeAsync(() => {
      const mockMessages: ChatbotMessage[] = [
        {
          id: '1',
          message: 'Hello',
          sender: 'user',
          timestamp: new Date()
        },
        {
          id: '2',
          message: 'Hi there!',
          sender: 'bot',
          timestamp: new Date()
        }
      ];

      messagesSubject.next(mockMessages);
      tick();

      expect(component.messages).toEqual(mockMessages);
      expect(component.messages.length).toBe(2);
      
      // Flush scroll timer
      tick(100);
    }));

    it('should set unavailable state when service isUnavailable$ emits true', fakeAsync(() => {
      expect(component.isUnavailable).toBe(false);
      
      isUnavailableSubject.next(true);
      tick();
      
      expect(component.isUnavailable).toBe(true);
    }));
  });

  describe('User Interactions', () => {
    it('should call service sendMessage when sendMessage is called with text', () => {
      component.messageText = 'Test message';
      component.sendMessage();

      expect(chatbotServiceSpy.sendMessage).toHaveBeenCalledWith('Test message');
      expect(component.messageText).toBe('');
    });

    it('should not send empty messages', () => {
      component.messageText = '';
      component.sendMessage();

      expect(chatbotServiceSpy.sendMessage).not.toHaveBeenCalled();
    });

    it('should not send whitespace-only messages', () => {
      component.messageText = '   ';
      component.sendMessage();

      expect(chatbotServiceSpy.sendMessage).not.toHaveBeenCalled();
    });

    it('should call toggleChatbot on service when toggleChatbot is called', () => {
      component.toggleChatbot();
      expect(chatbotServiceSpy.toggleChatbot).toHaveBeenCalled();
    });

    it('should call closeChatbot on service when closeChatbot is called', () => {
      component.closeChatbot();
      expect(chatbotServiceSpy.closeChatbot).toHaveBeenCalled();
    });
  });

  describe('Keyboard Events', () => {
    it('should send message when Enter key is pressed without Shift', () => {
      component.messageText = 'Test message';
      const event = new KeyboardEvent('keydown', { key: 'Enter', shiftKey: false });
      spyOn(event, 'preventDefault');
      
      component.onKeyPress(event);

      expect(event.preventDefault).toHaveBeenCalled();
      expect(chatbotServiceSpy.sendMessage).toHaveBeenCalledWith('Test message');
    });

    it('should not send message when Enter key is pressed with Shift', () => {
      component.messageText = 'Test message';
      const event = new KeyboardEvent('keydown', { key: 'Enter', shiftKey: true });
      spyOn(event, 'preventDefault');
      
      component.onKeyPress(event);

      expect(event.preventDefault).not.toHaveBeenCalled();
      expect(chatbotServiceSpy.sendMessage).not.toHaveBeenCalled();
    });

    it('should not send message when other keys are pressed', () => {
      component.messageText = 'Test message';
      const event = new KeyboardEvent('keydown', { key: 'a' });
      
      component.onKeyPress(event);

      expect(chatbotServiceSpy.sendMessage).not.toHaveBeenCalled();
    });
  });

  describe('Utility Methods', () => {
    it('should format message text by converting newlines to <br>', () => {
      const text = 'Line 1\nLine 2\nLine 3';
      const formatted = component.formatMessageText(text);
      
      expect(formatted).toBe('Line 1<br>Line 2<br>Line 3');
    });

    it('should format timestamp correctly', () => {
      const timestamp = new Date('2024-01-15T14:30:00');
      const formatted = component.formatTime(timestamp);
      
      // Format will depend on locale, but should contain time components
      expect(formatted).toContain(':');
      expect(formatted.length).toBeGreaterThan(0);
    });
  });

  describe('Touch Gestures', () => {
    it('should capture touch start position', () => {
      const mockTouchEvent = {
        touches: [{ clientX: 100, clientY: 200 }]
      } as unknown as TouchEvent;

      component.onTouchStart(mockTouchEvent);

      expect(component['touchStartX']).toBe(100);
      expect(component['touchStartY']).toBe(200);
    });

    it('should close chatbot on downward swipe exceeding threshold', () => {
      // Set touch start position
      component['touchStartY'] = 100;
      component['touchStartX'] = 150;

      // Create touch end event with downward movement > 100px
      const mockTouchEvent = {
        changedTouches: [{ clientX: 150, clientY: 250 }]
      } as unknown as TouchEvent;

      component.onTouchEnd(mockTouchEvent);

      expect(chatbotServiceSpy.closeChatbot).toHaveBeenCalled();
    });

    it('should not close chatbot on small downward movement', () => {
      // Set touch start position
      component['touchStartY'] = 100;
      component['touchStartX'] = 150;

      // Create touch end event with small downward movement
      const mockTouchEvent = {
        changedTouches: [{ clientX: 150, clientY: 150 }]
      } as unknown as TouchEvent;

      component.onTouchEnd(mockTouchEvent);

      expect(chatbotServiceSpy.closeChatbot).not.toHaveBeenCalled();
    });

    it('should not close chatbot on horizontal swipe', () => {
      // Set touch start position
      component['touchStartY'] = 100;
      component['touchStartX'] = 100;

      // Create touch end event with horizontal movement
      const mockTouchEvent = {
        changedTouches: [{ clientX: 250, clientY: 120 }]
      } as unknown as TouchEvent;

      component.onTouchEnd(mockTouchEvent);

      expect(chatbotServiceSpy.closeChatbot).not.toHaveBeenCalled();
    });

    it('should not close chatbot on upward swipe', () => {
      // Set touch start position
      component['touchStartY'] = 200;
      component['touchStartX'] = 150;

      // Create touch end event with upward movement
      const mockTouchEvent = {
        changedTouches: [{ clientX: 150, clientY: 50 }]
      } as unknown as TouchEvent;

      component.onTouchEnd(mockTouchEvent);

      expect(chatbotServiceSpy.closeChatbot).not.toHaveBeenCalled();
    });

    it('should reset touch positions after touch end', () => {
      component['touchStartY'] = 100;
      component['touchStartX'] = 150;

      const mockTouchEvent = {
        changedTouches: [{ clientX: 150, clientY: 250 }]
      } as unknown as TouchEvent;

      component.onTouchEnd(mockTouchEvent);

      expect(component['touchStartY']).toBe(0);
      expect(component['touchStartX']).toBe(0);
    });
  });

  describe('Component Lifecycle', () => {
    it('should unsubscribe from all subscriptions on destroy', () => {
      const mockSubscription = jasmine.createSpyObj('Subscription', ['unsubscribe']);
      component['subscriptions'] = [mockSubscription, mockSubscription, mockSubscription];

      component.ngOnDestroy();

      expect(mockSubscription.unsubscribe).toHaveBeenCalledTimes(3);
    });
  });
});
