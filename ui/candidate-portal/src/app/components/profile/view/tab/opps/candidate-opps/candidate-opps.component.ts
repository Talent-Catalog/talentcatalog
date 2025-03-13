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

import {Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges} from '@angular/core';
import {Candidate} from "../../../../../../model/candidate";
import {CandidateOpportunity} from "../../../../../../model/candidate-opportunity";
import {forkJoin, Observable} from "rxjs";
import {JobChat, JobChatType} from "../../../../../../model/chat";
import {ChatService} from "../../../../../../services/chat.service";

@Component({
  selector: 'app-candidate-opps',
  templateUrl: './candidate-opps.component.html',
  styleUrls: ['./candidate-opps.component.scss']
})
export class CandidateOppsComponent implements OnInit, OnChanges {
  error: string;
  loading: boolean;
  @Input() candidate: Candidate;
  @Output() refresh = new EventEmitter();

  //Map of job chats by opp id
  private mapOppChatsByOppId: Map<number, JobChat[]> = new Map()

  selectedOpp: CandidateOpportunity;

  constructor(
    private chatService: ChatService
  ) { }

  ngOnInit(): void {
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.candidate) {
      this.fetchChats();
    }
  }

  get activeOppsGreaterThanProspect(): CandidateOpportunity[] {
    return this.candidate?.activeCandidateOppsGreaterThanProspect;
  }

  selectOpp(opp: CandidateOpportunity) {
    this.selectedOpp = opp;
  }

  unSelectOpp() {
    this.selectedOpp = null;
    this.refresh.emit();
  }

  getOppChats(opp: CandidateOpportunity): JobChat[] {
    return this.mapOppChatsByOppId.get(opp.id)
  }

  fetchOppChats(opp: CandidateOpportunity) {
    let chats$: Observable<JobChat>[] = [];

    const chatRequests = [
      {
        type: JobChatType.CandidateRecruiting,
        candidateId: this.candidate.id,
        jobId: opp?.jobOpp?.id
      },
      {
        type: JobChatType.CandidateProspect,
        candidateId: this.candidate.id
      },
      {
        type: JobChatType.AllJobCandidates,
        jobId: opp?.jobOpp?.id
      }
    ];

    for (const request of chatRequests) {
      chats$.push(this.chatService.getOrCreate(request))
    }

    //Now fetch all those chats
    this.loading = true;
    this.error = null;
    forkJoin(chats$).subscribe(
      (jobChats) => {
        this.mapOppChatsByOppId.set(opp.id, this.chatService.removeDuplicateChats(jobChats));
        this.loading = false;
      },
      (error) => {
        this.error = error;
        this.loading = false;
      }
    )
  }

  private fetchChats() {
    let opportunities = this.candidate.activeCandidateOppsGreaterThanProspect;
    for (const opp of opportunities) {
      this.fetchOppChats(opp);
    }
  }
}
