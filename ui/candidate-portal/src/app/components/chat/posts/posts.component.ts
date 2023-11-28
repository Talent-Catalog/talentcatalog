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

import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {ChatPost, JobChat} from "../../../model/chat";
import {ChatPostService} from "../../../services/chat-post.service";
import {Message} from "@stomp/stompjs";
import {ChatService} from "../../../services/chat.service";
import {Subscription} from "rxjs";

@Component({
  selector: 'app-posts',
  templateUrl: './posts.component.html',
  styleUrls: ['./posts.component.scss']
})
export class PostsComponent implements OnInit, OnChanges {

  @Input() chat: JobChat;

  currentPost: ChatPost;
  posts: ChatPost[];
  loading: boolean;
  error;
  private chatSubscription: Subscription;

  constructor(
      private chatService: ChatService,
      private chatPostService: ChatPostService,
  ) {}

  ngOnInit(): void {
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (this.chat) {
      //Get rid of any existing subscription to previous chat
      if (this.chatSubscription) {
        this.chatSubscription.unsubscribe();
      }

      //Clear existing posts
      this.posts = [];

      //Subscribe for updates on new chat
      this.chatSubscription = this.chatService.subscribe(this.chat)
      .subscribe((message: Message) => {
        const payload: ChatPost = JSON.parse(message.body);
        this.addNewPost(payload);
      });

      //Fetch all existing posts for this chat
      this.loadPosts();
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

  selectCurrent(post: ChatPost) {
    this.currentPost = post;
  }

  private addNewPost(post: ChatPost) {
    this.posts.push(post);
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
    this.posts = posts;
  }
}
