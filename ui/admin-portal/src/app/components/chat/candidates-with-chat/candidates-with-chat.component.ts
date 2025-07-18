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
import {ChatService} from "../../../services/chat.service";
import {MainSidePanelBase} from "../../util/split/MainSidePanelBase";
import {Candidate, UpdateCandidateMutedRequest} from "../../../model/candidate";
import {JobChat} from "../../../model/chat";
import {BehaviorSubject} from "rxjs";
import {AuthorizationService} from "../../../services/authorization.service";
import {CandidateService} from "../../../services/candidate.service";

@Component({
  selector: 'app-candidates-with-chat',
  templateUrl: './candidates-with-chat.component.html',
  styleUrls: ['./candidates-with-chat.component.scss']
})
export class CandidatesWithChatComponent extends MainSidePanelBase implements OnInit {

  /**
   * This is passed in from a higher level component which tracks whether the overall read status
   * of all the chats that it manages.
   * <p/>
   * This component can call next on this subject if it knows that some of the chats it manages
   * are unread. The fact that it is a BehaviorSubject means that you can query the current status
   * of the higher level component.
   */
  @Input() chatsRead$: BehaviorSubject<boolean>;

  error: any;
  loading: boolean;
  selectedCandidate: Candidate;
  selectedCandidateChat: JobChat;
  chatHeader: string = "";

  constructor(
    private chatService: ChatService,
    private candidateService: CandidateService,
    private authorizationService: AuthorizationService
  ) { super(5); }

  ngOnInit(): void { }

  public onCandidateSelected(candidate: Candidate) {
    this.selectedCandidate = candidate;
    if (candidate) {
      this.displayChat();
    }
    window.scrollTo(0,0); // When user clicks candidate at bottom of page, returns them to editor
  }

  private displayChat() {
    this.fetchCandidateChat();
    this.computeChatHeader();
  }

  private fetchCandidateChat() {
    this.error = null;

    this.chatService.getCandidateProspectChat(this.selectedCandidate.id).subscribe(
      (chat) => {this.selectedCandidateChat = chat},
      (error) => {this.error = error}
    )
  }

  private computeChatHeader() {
    this.chatHeader =
      "Chat with " + this.selectedCandidate.user.firstName + " " +
      this.selectedCandidate.user.lastName;
  }

  isReadOnlyUser() {
    return this.authorizationService.isReadOnly();
  }

}
