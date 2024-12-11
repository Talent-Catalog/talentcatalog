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

import {
  Component,
  HostListener,
  Input,
  OnChanges,
  OnDestroy,
  OnInit,
  QueryList,
  SimpleChanges,
  ViewChild,
  ViewChildren
} from '@angular/core';
import {CreateChatRequest, JobChatType} from "../../../model/chat";
import {Partner} from "../../../model/partner";
import {ChatService} from "../../../services/chat.service";
import {PostsComponentBase} from "../../util/chat/PostsComponentBase";
import {ChatPostService} from "../../../services/chat-post.service";
import {CreateUpdatePostComponent} from "../create-update-post/create-update-post.component";
import {ViewPostComponent} from "../view-post/view-post.component";

@Component({
  selector: 'app-view-chat-posts',
  templateUrl: './view-chat-posts.component.html',
  styleUrls: ['./view-chat-posts.component.scss']
})
export class ViewChatPostsComponent extends PostsComponentBase
  implements OnInit, OnChanges, OnDestroy {
  @Input() candidateId: number;
  @Input() jobId: number;
  @Input() jobChatType: JobChatType;
  @Input() sourcePartner: Partner;
  @Input() readOnly: boolean = false;

  @ViewChild(CreateUpdatePostComponent) editor: CreateUpdatePostComponent;
  @ViewChildren(ViewPostComponent) viewPostComponents: QueryList<ViewPostComponent>;

  constructor(
    chatService: ChatService,
    chatPostService: ChatPostService
  ) {
    super(chatService, chatPostService)
  }

  ngOnInit(): void {
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.chat) {
      if (this.chat) {
        this.onNewChat(this.chat);
      }
    } else {
      this.fetchJobChat();
    }
  }

  private fetchJobChat() {
    const request: CreateChatRequest = {
      type: this.jobChatType,
      candidateId: this.candidateId,
      jobId: this.jobId,
      sourcePartnerId: this.sourcePartner?.id
    }

    this.requestJobChat(request);
  }

  onMarkChatAsRead() {
    if (this.chat) {
      this.chatService.markChatAsRead(this.chat);
    }
  }

// Ensures that reaction and editor emoji pickers are not open at the same time, and that clicks
  // anywhere on the DOM outside an open picker will close that picker. Reaction pickers are
  // attached to instances of ViewPostComponent (reacting to posts) and the editor picker is
  // attached to the Quill toolbar (writing posts).
  @HostListener('document:click', ['$event'])
  documentClick(event) {
    // Identify the post with an open reaction picker, if any
    const postWithOpenPicker =
        this.viewPostComponents.find(
            (post) => post.reactionPickerVisible)

    // Check if any picker is open
    if(this.editor.emojiPickerVisible || postWithOpenPicker != null) {

      // Generate value to check if click was within any emoji picker
      const sectionClass: string =
          event.target.closest('section') ?
              event.target.closest('section').classList[0] : "";

      // Generate value to check if click was on an emoji picker toggle button (smiley emoji icon)
      const clickedElementId: string =
        event.target.closest('button') ?
          event.target.closest('button').id : "";

      if (clickedElementId.includes('reactionBtn') && this.editor.emojiPickerVisible) {
        // If click was on reaction picker toggle button, close the editor picker
        this.editor.emojiPickerVisible = false
      } else if (clickedElementId.includes('emojiBtn') && postWithOpenPicker != null) {
        // If click was on editor picker toggle button, close the post reaction picker
        postWithOpenPicker.reactionPickerVisible = false;
      }
      else if (!sectionClass.includes('emoji') &&
          !clickedElementId.includes('reactionBtn') &&
          !clickedElementId.includes('emojiBtn')) {
        // If click was not on any emoji picker toggle button or emoji picker, close any open picker
        this.editor.emojiPickerVisible = false;
        if (postWithOpenPicker) {
          postWithOpenPicker.reactionPickerVisible = false;
        }
      }
    }
  }
}
