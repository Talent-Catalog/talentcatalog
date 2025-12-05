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

import { Injectable, OnDestroy } from '@angular/core';
import { BehaviorSubject, Observable, Subject, of } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { catchError, map, tap } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { ChatbotMessage } from '../model/chatbot-message';

/**
 * Service for managing chatbot UI state and communication with backend.
 * Handles chatbot open/close state, message management, and HTTP communication.
 */
@Injectable({
  providedIn: 'root'
})
export class ChatbotService implements OnDestroy {

  private apiUrl = environment.host ? `http://${environment.host}/api/chatbot` : '/api/chatbot';
  private readonly SESSION_STORAGE_KEY = 'chatbot_session_id';

  // UI State Management
  private isOpenSubject = new BehaviorSubject<boolean>(false);
  public isOpen$ = this.isOpenSubject.asObservable();

  // Message Management
  private messagesSubject = new BehaviorSubject<ChatbotMessage[]>([]);
  public messages$ = this.messagesSubject.asObservable();

  // Loading state for API calls
  private isLoadingSubject = new BehaviorSubject<boolean>(false);
  public isLoading$ = this.isLoadingSubject.asObservable();

  // Error handling
  private errorSubject = new Subject<string>();
  public error$ = this.errorSubject.asObservable();

  // Availability tracking
  private isUnavailableSubject = new BehaviorSubject<boolean>(false);
  public isUnavailable$ = this.isUnavailableSubject.asObservable();

  constructor(private http: HttpClient) {
    // Initialize with welcome message
    this.initializeChat();
  }

  ngOnDestroy(): void {
    this.isOpenSubject.complete();
    this.messagesSubject.complete();
    this.isLoadingSubject.complete();
    this.errorSubject.complete();
    this.isUnavailableSubject.complete();
  }

  /**
   * Toggles the chatbot open/closed state
   */
  toggleChatbot(): void {
    const currentState = this.isOpenSubject.value;
    this.isOpenSubject.next(!currentState);
  }

  /**
   * Opens the chatbot
   */
  openChatbot(): void {
    this.isOpenSubject.next(true);
  }

  /**
   * Closes the chatbot
   */
  closeChatbot(): void {
    this.isOpenSubject.next(false);
  }

  /**
   * Sends a message to the chatbot and handles the response
   * @param message The user's message
   */
  sendMessage(message: string): void {
    if (!message.trim() || this.isUnavailableSubject.value) {
      return;
    }

    // Client-side validation: check message length
    const trimmedMessage = message.trim();
    if (trimmedMessage.length > 2000) {
      this.errorSubject.next('Message cannot exceed 2000 characters.');
      return;
    }

    const sessionId = this.getSessionId();

    // Add user message immediately to UI
    const userMessage: ChatbotMessage = {
      id: this.generateId(),
      message: trimmedMessage,
      sender: 'user',
      timestamp: new Date()
    };

    this.addMessage(userMessage);
    this.isLoadingSubject.next(true);

    // Send to backend with session ID
    this.http.post<ChatbotMessage>(`${this.apiUrl}/send`, { 
      message: trimmedMessage,
      sessionId: sessionId
    })
      .pipe(
        tap(response => {
          // Add bot response to UI
          this.addMessage(response);
          this.isLoadingSubject.next(false);
        }),
        catchError(error => {
          console.error('Error sending message:', error);
          this.errorSubject.next('Failed to send message. Please try again.');
          this.isLoadingSubject.next(false);
          
          // Add error message to chat
          const errorMessage: ChatbotMessage = {
            id: this.generateId(),
            message: 'Sorry, I encountered an error. Please try again.',
            sender: 'bot',
            timestamp: new Date()
          };
          this.addMessage(errorMessage);
          
          // Return empty observable to complete the stream without error
          return of();
        })
      )
      .subscribe();
  }

  /**
   * Gets welcome message from backend
   */
  getWelcomeMessage(): void {
    this.http.get<{ message: string }>(`${this.apiUrl}/welcome`)
      .pipe(
        tap(response => {
          const welcomeMessage: ChatbotMessage = {
            id: this.generateId(),
            message: response.message,
            sender: 'bot',
            timestamp: new Date()
          };
          this.addMessage(welcomeMessage);
        }),
        catchError(error => {
          console.error('Error getting welcome message:', error);
          // Mark chatbot as unavailable
          this.isUnavailableSubject.next(true);
          // Show unavailable message to user
          const fallbackMessage: ChatbotMessage = {
            id: this.generateId(),
            message: 'Sorry, the chatbot is currently unavailable. Please try again later.',
            sender: 'bot',
            timestamp: new Date()
          };
          this.addMessage(fallbackMessage);
          this.errorSubject.next('Chatbot backend is unavailable');
          // Return empty observable to complete the stream without error
          return of();
        })
      )
      .subscribe();
  }

  /**
   * Adds a message to the current message list
   * @param message The message to add
   */
  private addMessage(message: ChatbotMessage): void {
    const currentMessages = this.messagesSubject.value;
    this.messagesSubject.next([...currentMessages, message]);
  }

  /**
   * Initializes the chat with welcome message
   */
  private initializeChat(): void {
    // Check if we have existing messages, if not, get welcome message
    if (this.messagesSubject.value.length === 0) {
      this.getWelcomeMessage();
    }
  }

  /**
   * Gets or creates a browser session ID for tracking conversations.
   * The session ID persists in localStorage across page refreshes.
   * @returns The session ID for this browser session
   */
  private getSessionId(): string {
    let sessionId = localStorage.getItem(this.SESSION_STORAGE_KEY);
    if (!sessionId) {
      // Generate a new unique session ID
      sessionId = this.generateSessionId();
      localStorage.setItem(this.SESSION_STORAGE_KEY, sessionId);
    }
    return sessionId;
  }

  /**
   * Generates a unique session ID as a UUID.
   * Uses crypto.randomUUID() if available (modern browsers), otherwise falls back to manual generation.
   * @returns A UUID string
   */
  private generateSessionId(): string {
    // Use crypto.randomUUID() if available (Chrome 92+, Firefox 95+, Safari 15.4+)
    if (typeof crypto !== 'undefined' && crypto.randomUUID) {
      return crypto.randomUUID();
    }
    
    // Fallback for older browsers: generate UUID v4 manually
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
      const r = Math.random() * 16 | 0;
      const v = c === 'x' ? r : (r & 0x3 | 0x8);
      return v.toString(16);
    });
  }

  /**
   * Generates a unique ID for messages
   * @returns A unique string ID
   */
  private generateId(): string {
    return Date.now().toString(36) + Math.random().toString(36).substr(2);
  }

  /**
   * Gets the current open state
   * @returns Current open state
   */
  get isOpen(): boolean {
    return this.isOpenSubject.value;
  }

  /**
   * Gets the current messages
   * @returns Current messages array
   */
  get messages(): ChatbotMessage[] {
    return this.messagesSubject.value;
  }

  /**
   * Gets the current loading state
   * @returns Current loading state
   */
  get isLoading(): boolean {
    return this.isLoadingSubject.value;
  }
}
