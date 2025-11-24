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

import { Component, ElementRef, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { Subscription } from 'rxjs';
import { ChatbotService } from '../../services/chatbot.service';
import { ChatbotMessage } from '../../model/chatbot-message';

@Component({
  selector: 'app-chatbot',
  templateUrl: './chatbot.component.html',
  styleUrls: ['./chatbot.component.scss']
})
export class ChatbotComponent implements OnInit, OnDestroy {

  @ViewChild('messageContainer', { static: false }) messageContainer!: ElementRef;
  @ViewChild('messageInput', { static: false }) messageInput!: ElementRef;

  isOpen = false;
  messages: ChatbotMessage[] = [];
  messageText = '';
  isUnavailable = false;

  private subscriptions: Subscription[] = [];
  private touchStartY = 0;
  private touchStartX = 0;
  private readonly SWIPE_THRESHOLD = 100; // Minimum distance to trigger swipe

  constructor(private chatbotService: ChatbotService) { }

  ngOnInit(): void {
    // Subscribe to chatbot open/closed state
    this.subscriptions.push(
      this.chatbotService.isOpen$.subscribe(isOpen => {
        this.isOpen = isOpen;
        if (isOpen) {
          // Focus input when chatbot opens
          setTimeout(() => {
            if (this.messageInput) {
              this.messageInput.nativeElement.focus();
            }
          }, 100);
        }
      })
    );

    // Subscribe to messages
    this.subscriptions.push(
      this.chatbotService.messages$.subscribe(messages => {
        this.messages = messages;
        // Auto-scroll to bottom when new messages arrive
        setTimeout(() => {
          this.scrollToBottom();
        }, 100);
      })
    );

    // Subscribe to availability status
    this.subscriptions.push(
      this.chatbotService.isUnavailable$.subscribe(isUnavailable => {
        this.isUnavailable = isUnavailable;
      })
    );
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach(sub => sub.unsubscribe());
  }

  toggleChatbot(): void {
    this.chatbotService.toggleChatbot();
  }

  closeChatbot(): void {
    this.chatbotService.closeChatbot();
  }

  sendMessage(): void {
    if (this.messageText.trim()) {
      this.chatbotService.sendMessage(this.messageText);
      this.messageText = '';
    }
  }

  onKeyPress(event: KeyboardEvent): void {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.sendMessage();
    }
  }

  private scrollToBottom(): void {
    if (this.messageContainer) {
      const element = this.messageContainer.nativeElement;
      element.scrollTop = element.scrollHeight;
    }
  }

  formatMessageText(text: string): string {
    // Convert line breaks to HTML breaks for proper display
    return text.replace(/\n/g, '<br>');
  }

  formatTime(timestamp: Date): string {
    const date = new Date(timestamp);
    return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  }

  onTouchStart(event: TouchEvent): void {
    if (event.touches.length > 0) {
      this.touchStartY = event.touches[0].clientY;
      this.touchStartX = event.touches[0].clientX;
    }
  }

  onTouchMove(event: TouchEvent): void {
    // Allow default scrolling behavior if user is scrolling within the messages container
    const target = event.target as HTMLElement;
    if (target.closest('.chatbot-messages')) {
      return;
    }
  }

  onTouchEnd(event: TouchEvent): void {
    if (event.changedTouches.length > 0 && this.touchStartY > 0) {
      const touchEndY = event.changedTouches[0].clientY;
      const touchEndX = event.changedTouches[0].clientX;
      const deltaY = touchEndY - this.touchStartY;
      const deltaX = Math.abs(touchEndX - this.touchStartX);

      // Check if it's a downward swipe (not a horizontal swipe)
      // Only close if swiping down more than threshold and vertical movement is greater than horizontal
      if (deltaY > this.SWIPE_THRESHOLD && deltaY > deltaX) {
        this.closeChatbot();
      }

      // Reset touch start position
      this.touchStartY = 0;
      this.touchStartX = 0;
    }
  }
}
