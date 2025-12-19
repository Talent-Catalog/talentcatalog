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

import {Component, Input, OnInit} from '@angular/core';
import {JobChat} from "../../../model/chat";
import {ChatService} from "../../../services/chat.service";

@Component({
  selector: 'app-view-chat',
  templateUrl: './view-chat.component.html',
  styleUrls: ['./view-chat.component.scss']
})
export class ViewChatComponent implements OnInit {

  @Input() chat: JobChat

  constructor(private chatService: ChatService) { }

  ngOnInit(): void {
  }

  get chatParticipantsKey(): string {
    return this.chatService.getChatInfoParticipantsKey(this.chat.type);
  }

  get chatPurposeKey(): string {
    return this.chatService.getChatInfoPurposeKey(this.chat.type);
  }
}
