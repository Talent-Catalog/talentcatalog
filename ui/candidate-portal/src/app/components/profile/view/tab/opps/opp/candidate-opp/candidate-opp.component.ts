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
  EventEmitter,
  Input,
  OnChanges,
  OnInit,
  Output,
  SimpleChanges
} from '@angular/core';
import {Candidate} from "../../../../../../../model/candidate";
import {
  CandidateOpportunity,
  isOppStageGreaterThanOrEqualTo
} from "../../../../../../../model/candidate-opportunity";
import {CreateChatRequest, JobChat, JobChatType} from "../../../../../../../model/chat";
import {ChatService} from "../../../../../../../services/chat.service";
import {forkJoin, of} from "rxjs";

const STAGE_TRANSLATION_KEY_ROOT = 'CASE-STAGE.';

@Component({
  selector: 'app-candidate-opp',
  templateUrl: './candidate-opp.component.html',
  styleUrls: ['./candidate-opp.component.scss']
})
export class CandidateOppComponent implements OnInit, OnChanges {
  @Input() selectedOpp: CandidateOpportunity;
  @Input() candidate: Candidate;
  @Output() back = new EventEmitter();
  error: any;
  loading: boolean;
  allJobCandidatesChat: JobChat;
  destinationChat: JobChat;
  sourceChat: JobChat;
  selectedChat: JobChat;
  selectedChatType: JobChatType;
  showDestinationChat: boolean;
  showAllChat: boolean;

  constructor(
    private chatService: ChatService
  ) { }

  ngOnInit(): void {
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (this.selectedOpp) {
      this.fetchJobChats();
    }
  }

  get JobChatType() {
    return JobChatType
  }

  private fetchJobChats() {
    const sourceChatRequest: CreateChatRequest = {
      type: JobChatType.CandidateProspect,
      candidateId: this.candidate.id
    }
    // Only want to show destination chat if candidate has reached or gone further than the CV Review stage.
    this.showDestinationChat = isOppStageGreaterThanOrEqualTo(
      this.selectedOpp?.lastActiveStage, "cvReview")
    const destinationChatRequest: CreateChatRequest = {
      type: JobChatType.CandidateRecruiting,
      candidateId: this.candidate.id,
      jobId: this.selectedOpp?.jobOpp?.id
    }
    // Only want to show all job candidates chat if candidate has reached or gone further than the Offer stage.
    this.showAllChat = isOppStageGreaterThanOrEqualTo(
      this.selectedOpp?.lastActiveStage, "offer");
    const allJobCandidatesChatRequest: CreateChatRequest = {
      type: JobChatType.AllJobCandidates,
      jobId: this.selectedOpp?.jobOpp?.id
    }

    forkJoin( {
      'sourceChat': this.chatService.getOrCreate(sourceChatRequest),
      'destinationChat': this.showDestinationChat ?
        this.chatService.getOrCreate(destinationChatRequest) : of(null),
      'allJobCandidatesChat': this.showAllChat?
        this.chatService.getOrCreate(allJobCandidatesChatRequest) : of(null),
    }).subscribe(
      results => {
        this.loading = false;
        this.sourceChat = results['sourceChat'];
        this.destinationChat = results['destinationChat'];
        this.allJobCandidatesChat = results['allJobCandidatesChat'];
      },
      (error) => {
        this.error = error;
        this.loading = false;
      }
    );
  }

  getChatHeaderTranslationKey(chatType: JobChatType): string {
    let key: string = null;
    switch (chatType) {
      case JobChatType.CandidateProspect:
        key = 'CANDIDATE-OPPS.TABLE.SOURCE-CHAT';
        break;

      case JobChatType.CandidateRecruiting:
        key = 'CANDIDATE-OPPS.TABLE.DESTINATION-CHAT';
        break;

      case JobChatType.AllJobCandidates:
        key = 'CANDIDATE-OPPS.TABLE.ALL-JOB-CANDIDATES-CHAT';
        break;
    }
    return key;
  }

  /**
   * Given the key of a CandidateOpportunityStage enum, return the translation key which is
   * used to display the meaning of this stage to candidates.
   * @param enumStageNameKey Key name of CandidateOpportunityStage
   * @return Translation key of stage description
   */
  getCandidateOpportunityStageTranslationKey(enumStageNameKey: string): string {
    return STAGE_TRANSLATION_KEY_ROOT + enumStageNameKey.toUpperCase();
  }

  goBack() {
    this.selectedOpp = null;
    this.back.emit();
  }

  setSelectedChatType(selectedChatType: JobChatType) {
    this.selectedChatType = selectedChatType;
    switch (this.selectedChatType) {
      case JobChatType.CandidateProspect:
        this.selectedChat = this.sourceChat;
        break;

      case JobChatType.CandidateRecruiting:
        this.selectedChat = this.destinationChat;
        break;

      case JobChatType.AllJobCandidates:
        this.selectedChat = this.allJobCandidatesChat;
        break;

      default:
        this.selectedChat = null;
    }
  }

  unSelectChat() {
    this.selectedChat = null;
  }

  onMarkChatAsRead() {
    this.chatService.markChatAsRead(this.selectedChat);
  }
}
