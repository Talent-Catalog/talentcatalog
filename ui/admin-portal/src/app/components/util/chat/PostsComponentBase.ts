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
import {Directive, EventEmitter, Input, OnDestroy, Output} from "@angular/core";
import {ChatService} from "../../../services/chat.service";
import {ChatPostService} from "../../../services/chat-post.service";
import {Subscription} from "rxjs";
import {ChatPost, CreateChatRequest, JobChat, GroupedMessages} from "../../../model/chat";

/**
 * This provides underlying common support for components which display chat posts.
 *
 *
 */
@Directive()
export abstract class PostsComponentBase implements OnDestroy{
  @Input() chat: JobChat;
  @Output() fetchedChat = new EventEmitter<JobChat>();

  /**
   * True if chat is read
   */
  chatIsRead: boolean;

  private chatSubscription: Subscription;
  private chatIsReadSubscription: Subscription;

  currentPost: ChatPost;

  posts: ChatPost[];
  groupedMessages: GroupedMessages[] = [];

  loading: boolean;
  error;

  constructor(
    protected chatService: ChatService,
    protected chatPostService: ChatPostService,
  ) {}

  ngOnDestroy(): void {
    this.unsubscribe();
  }

  protected requestJobChat(request: CreateChatRequest) {
    if (request) {
      this.error = null;
      this.chatService.getOrCreate(request).subscribe(
        (chat) => {
          this.onNewChat(chat)
        },
        (error) => {
          this.error = error
        }
      )
    }
  }

  protected onNewChat(chat: JobChat) {
    this.chat = chat;
    //Notify that chat has been fetched.
    this.fetchedChat.emit(this.chat);

    //Get rid of any existing subscription to previous chat
    this.unsubscribe();

    //Clear existing posts
    this.posts = [];

    if (this.chat) {
      // console.log('Subscribing for posts on chat ' + chat.id)
      //Subscribe for updates on new chat
      this.chatSubscription = this.chatService.getChatPosts$(this.chat).subscribe({
          next: (post) => this.handleIncomingPost(post) // Handle new posts and post updates
        }
      );

      //Fetch all existing posts for this chat
      this.loadPosts();

      //Listen for chat is read events, setting local variable reflecting current state.
      this.chatIsReadSubscription = this.chatService.getChatIsRead$(this.chat).subscribe({
          next: chatIsRead => this.chatIsRead = chatIsRead
        }
      );
    }
  }

  private loadPosts() {
    if (this.chat) {
      this.loading = false;
      this.error = null;
      this.chatPostService.listPosts(this.chat.id).subscribe(
        posts => {
          this.updatePosts(posts);
          this.loading = false;
        },
        error => {
          this.error = error;
          this.loading = false;
        });
      this.selectCurrent(null);
    }
  }

  /**
   * Handle both new posts and post updates. Determines whether the incoming post is new or an
   * update to an existing post, and updates the posts array accordingly.
   */
  private handleIncomingPost(incomingPost: ChatPost) {
    const existingPostIndex = this.posts.findIndex(p => p.id === incomingPost.id);
    if (existingPostIndex !== -1) {
      // If the post already exists, update it
      this.posts[existingPostIndex] = incomingPost;
      this.groupedMessages = this.groupMessagesByDate(this.posts);
    } else {
      // If it's a new post, add it to the posts array
      this.posts.push(incomingPost);
      this.groupedMessages = this.groupMessagesByDate(this.posts);
    }
  }

  private updatePosts(posts: ChatPost[]) {
    if (this.posts.length > 0) {
      //We must have received a new post(s) as we were loading existing posts
      //The new posts may or may not be in the posts we have just received.
      //Go through newPosts adding any that are not already present (by checking the post unique id's)
      for (const newPost of this.posts) {
        const existingPostIndex = posts.findIndex(p => p.id === newPost.id);

        if (existingPostIndex !== -1) {
          // If the post already exists, replace it with the updated post
          console.log('Replacing post ' + newPost.id);
          posts[existingPostIndex] = newPost;
        } else {
          // If the post is new, add it to the list
          posts.push(newPost);
        }
      }
    }
    this.posts = posts;
    this.groupedMessages = this.groupMessagesByDate(posts);
  }

  selectCurrent(post: ChatPost) {
    this.currentPost = post;
  }

  private unsubscribe() {
    if (this.chatSubscription) {
      this.chatSubscription.unsubscribe();
      this.chatSubscription = null;
    }
    if (this.chatIsReadSubscription) {
      this.chatIsReadSubscription.unsubscribe();
      this.chatIsReadSubscription = null;
    }
  }

  private groupMessagesByDate(messages: any[]): GroupedMessages[] {
    const grouped = messages.reduce((acc, message) => {
      const date = new Date(message.createdDate).toLocaleDateString('en-US', {
        weekday: 'long',
        month: 'short',
        day: 'numeric',
        year: 'numeric'
      });
      if (!acc[date]) {
        acc[date] = [];
      }
      acc[date].push(message);
      return acc;
    }, {} as { [key: string]: any[] });

    // Transform the object into an array for easier rendering
    return Object.keys(grouped).map(date => ({
      date,
      messages: grouped[date]
    }));
  }
}

