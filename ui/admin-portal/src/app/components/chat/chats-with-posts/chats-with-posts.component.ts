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

import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {MainSidePanelBase} from "../../util/split/MainSidePanelBase";
import {JobChat} from "../../../model/chat";
import {ChatService} from "../../../services/chat.service";
import {AuthorizationService} from "../../../services/authorization.service";

@Component({
  selector: 'app-chats-with-posts',
  templateUrl: './chats-with-posts.component.html',
  styleUrls: ['./chats-with-posts.component.scss']
})
export class ChatsWithPostsComponent extends MainSidePanelBase implements OnInit {
  @Input() chats: JobChat[];
  @Input() fromUrl: boolean;
  @Output() chatSelection = new EventEmitter();

  error: any;
  selectedChat: JobChat;

  constructor(
    private chatService: ChatService,
    private authorizationService: AuthorizationService
  ) {
    super(6);
  }

  ngOnInit(): void {
    /** If this component is viewed from the side panel (not from a URL view) then we want to stack the panels so that
     * the chat and table have full width for better UI experience.
     */
    if (!this.fromUrl) {
      this.mainPanelColWidth = 12;
      this.sidePanelColWidth = 12;
    }
  }

  onChatSelected(chat: JobChat) {
    this.selectedChat = chat;
    this.chatSelection.emit(chat);
  }

  onMarkChatAsRead() {
    if (this.selectedChat) {
      this.chatService.markChatAsRead(this.selectedChat);
    }
  }

  isReadOnlyUser() {
    return this.authorizationService.isReadOnly();
  }
}
