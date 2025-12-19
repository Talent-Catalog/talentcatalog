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
import {Job} from "../../../../../model/job";
import {CreateChatRequest, JobChat, JobChatType} from "../../../../../model/chat";
import {ChatService} from "../../../../../services/chat.service";
import {forkJoin} from "rxjs";
import {AuthorizationService} from "../../../../../services/authorization.service";

@Component({
  selector: 'app-job-group-chats-tab',
  templateUrl: './job-group-chats-tab.component.html',
  styleUrls: ['./job-group-chats-tab.component.scss']
})
export class JobGroupChatsTabComponent implements OnInit, OnChanges {
  @Input() job: Job;
  @Input() fromUrl: boolean;
  chats: JobChat[];

  error: any;
  loading: boolean;

  constructor(
      private chatService: ChatService,
      private authorizationService: AuthorizationService
    ) { }

  ngOnInit(): void {
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (this.job) {
      this.fetchJobChats();
    }
  }

  private fetchJobChats() {
    const allCandidatesChatRequest: CreateChatRequest = {
      type: JobChatType.AllJobCandidates,
      jobId: this.job?.id
    }
    const allSourcePartnersChatRequest: CreateChatRequest = {
      type: JobChatType.JobCreatorAllSourcePartners,
      jobId: this.job?.id
    }

    forkJoin( {
      'allJobCandidatesChat': this.chatService.getOrCreate(allCandidatesChatRequest),
      'allSourcePartnersChat': this.chatService.getOrCreate(allSourcePartnersChatRequest),
    }).subscribe(
      results => {
        this.loading = false;

        const allJobCandidatesChat = results['allJobCandidatesChat'];
        allJobCandidatesChat.name = "All associated with job plus candidates who have accepted job offers";

        const allSourcePartnersChat = results['allSourcePartnersChat'];
        if (this.authorizationService.isSourcePartner()) {
          allSourcePartnersChat.name = this.job.jobCreator.name + " and all source partners";
        } else {
          allSourcePartnersChat.name = "All source partners";
        }

        this.chats = [allJobCandidatesChat, allSourcePartnersChat];
      },
      (error) => {
        this.error = error;
        this.loading = false;
      }
    );
  }

}
