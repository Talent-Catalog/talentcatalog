/*
 * Copyright (c) 2023 Talent Beyond Boundaries.
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
          next: (post) => this.addNewPost(post)
        }
      );

      //Fetch all existing posts for this chat
      this.loadPosts();

      //Listen for chat is read events, setting local variable reflecting current state.
      this.chatIsReadSubscription = this.chatService.getChatIsRead$(this.chat).subscribe({
          next: chatIsRead => this.chatIsRead = chatIsRead
        }
      )
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

  private addNewPost(post: ChatPost) {
    this.posts.push(post);
    this.groupedMessages = this.groupMessagesByDate(this.posts);
  }

  private updatePosts(posts: ChatPost[]) {
    if (this.posts.length > 0) {
      //We must have received a new post(s) as we were loading existing posts
      //The new posts may or may not be in the posts we have just received.
      //Go through newPosts adding any that are not already present (by checking the post unique id's)
      for (const newPost of this.posts) {
        if (!posts.find(p => p.id = newPost.id)) {
          posts.push(newPost);
        }
      }
    }
    this.groupedMessages = this.groupMessagesByDate(posts);
    this.posts = posts;
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

