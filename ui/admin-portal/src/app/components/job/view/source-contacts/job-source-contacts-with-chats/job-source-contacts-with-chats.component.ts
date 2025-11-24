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

import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {MainSidePanelBase} from "../../../../util/split/MainSidePanelBase";
import {CreateChatRequest, JobChat, JobChatType} from "../../../../../model/chat";
import {Partner} from "../../../../../model/partner";
import {Job} from "../../../../../model/job";
import {ChatService} from "../../../../../services/chat.service";
import {AuthorizationService} from "../../../../../services/authorization.service";
import {AuthenticationService} from "../../../../../services/authentication.service";

@Component({
  selector: 'app-job-source-contacts-with-chats',
  templateUrl: './job-source-contacts-with-chats.component.html',
  styleUrls: ['./job-source-contacts-with-chats.component.scss']
})
export class JobSourceContactsWithChatsComponent extends MainSidePanelBase
  implements OnInit, OnChanges {

  @Input() job: Job;
  @Input() editable: boolean;
  @Input() fromUrl: boolean;

  chatHeader: string;
  error: any;
  selectable: boolean = true;
  selectedSourcePartner: Partner;
  selectedSourcePartnerChat: JobChat;

  constructor(
      private authenticationService: AuthenticationService,
      private authorizationService: AuthorizationService,
      private chatService: ChatService,
  ) {
    super(5 );
  }

  ngOnInit(): void {
    this.computeChatHeader();

    if (this.authorizationService.isViewingAsSource()) {
      //Source partners auto select and can only
      //display their chat with the destination partner associated with the job.
      this.selectedSourcePartner = this.authenticationService.getLoggedInUser().partner;
      this.displayChat();

      //Selection can't change
      this.selectable = false;
    }

    /** If this component is viewed from the side panel (not from a URL view) then we want to stack the panels so that
     * the chat and table have full width for better UI experience.
     */
    if (!this.fromUrl) {
      this.mainPanelColWidth = 12;
      this.sidePanelColWidth = 12;
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (this.job && this.selectedSourcePartner) {
      this.displayChat();
    }
  }

  private displayChat() {
    this.fetchJobChat();
    this.computeChatHeader();
  }

  private fetchJobChat() {
    const request: CreateChatRequest = {
      type: JobChatType.JobCreatorSourcePartner,
      jobId: this.job?.id,
      sourcePartnerId: this.selectedSourcePartner?.id
    }

    this.error = null;
    this.chatService.getOrCreate(request).subscribe(
      (chat) => {this.selectedSourcePartnerChat = chat},
      (error) => {this.error = error}
    )
  }

  onSourcePartnerSelected(sourcePartner: Partner) {
    this.selectedSourcePartner = sourcePartner;
    this.fetchJobChat();
    this.computeChatHeader();
  }

  private computeChatHeader() {
    let name: string = "";

    if (this.authorizationService.isViewingAsSource()) {
      if (this.job) {
        name = "recruiter (" + this.job.jobCreator?.name + ")";
      }
    } else {
      if (this.selectedSourcePartner) {
        name = "source partner: " + this.selectedSourcePartner.name;
      } else {
        name = ": Select partner to display chat with them"
      }
    }
    this.chatHeader = "Chat with " + name;
  }

  onMarkChatAsRead() {
    if (this.selectedSourcePartnerChat) {
      this.chatService.markChatAsRead(this.selectedSourcePartnerChat);
    }

  }

  isReadOnlyUser() {
    return this.authorizationService.isReadOnly();
  }
}
