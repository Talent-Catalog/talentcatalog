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

import {Component, OnInit} from '@angular/core';
import {Candidate, CandidateStatus, isMuted} from "../../../model/candidate";
import {CandidateService} from "../../../services/candidate.service";
import {US_AFGHAN_SURVEY_TYPE} from "../../../model/survey-type";
import {NgbNavChangeEvent} from "@ng-bootstrap/ng-bootstrap";
import {ChatPost, JobChat, JobChatType, JobChatUserInfo} from "../../../model/chat";
import {forkJoin, Observable, Subscription} from "rxjs";
import {ChatService} from "../../../services/chat.service";
import {LocalStorageService} from "../../../services/local-storage.service";
import {Location} from "@angular/common";
import {ActivatedRoute} from "@angular/router";
import {TaskAssignment} from "../../../model/task-assignment";
import {
  CandidateOpportunity,
  CandidateOpportunityStage
} from "../../../model/candidate-opportunity";
import {map, shareReplay} from "rxjs/operators";
import {LinkedinService} from "../../../services/linkedin.service";
import {AuthorizationService} from "../../../services/authorization.service";
import {CasiPortalService} from "../../../services/casi-portal.service";
import {environment} from "../../../../environments/environment";

@Component({
  selector: 'app-view-candidate',
  templateUrl: './view-candidate.component.html',
  styleUrls: ['./view-candidate.component.scss']
})
export class ViewCandidateComponent implements OnInit {

  private lastTabKey: string = 'CandidateLastTab';
  private defaultTabId: string = 'Profile';
  activeTabId: string;
  chatsForAllJobs: JobChat[];
  sourceChat: JobChat;
  filteredOpps: CandidateOpportunity[];
  showServicesTab$: Observable<boolean>;

  //Candidate only sees source chat if is not empty. That way they can't start posting themselves
  //until someone else has posted in the chat.
  private sourceChatHasPosts: boolean = false;

  //Used to unsubscribe
  private sourceChatSubscription: Subscription;

  error: any;
  loading: boolean;
  candidate: Candidate;
  usAfghan: boolean;
  activeDuolingoTask: TaskAssignment;
  linkedinEligible$: Observable<boolean>;
  referenceEligible$: Observable<boolean>;
  unhcrEligible$: Observable<boolean>;

  constructor(
    private authorizationService: AuthorizationService,
    private candidateService: CandidateService,
    private chatService: ChatService,
    private linkedinService: LinkedinService,
    private casiPortalService: CasiPortalService,
    private localStorageService: LocalStorageService,
    private location: Location,
    private route: ActivatedRoute,
  ) { }

  ngOnInit(): void {
    this.fetchCandidate();
    this.route.queryParams.subscribe(params => {
      const tab = params['tab'];
      // If there is a tab param, set that as the active tab
      // If there is no tab param, check the browser cache for the last active tab or if none get the default tab.
      tab ? this.setActiveTabId(tab) : this.fetchCachedTab();
    });
  }

  /**
   * Ineligible candidates can't see chat, even if eligibility is under review
   */
  get canSeeChatTab(): boolean {
    let canSee = this.canViewChats && this.sourceChatHasPosts;
    if (canSee) {
      if (
        this.candidate &&
        (
          CandidateStatus[this.candidate.status] === CandidateStatus.ineligible ||
          CandidateStatus[this.candidate.status] === CandidateStatus.ineligibleReview
        )
      ) {
        canSee = false;
      }
    }
    return canSee;
  }

  /**
   * Only candidates who have filtered opps can see the jobs tab
   */
  get canSeeJobTab(): boolean {
    return this.filteredOpps?.length > 0;
  }

  get canViewChats(): boolean {
    return this.authorizationService.canViewChats();
  }

  /**
   * Filter out prospect opportunities and closed due to "candidateMistakenProspect" stage
   */
  filterOppsToDisplay() {
    this.filteredOpps = [];
    if (this.candidate?.candidateOpportunities.length > 0) {
      this.filteredOpps = this.candidate.candidateOpportunities.filter(
        opp => CandidateOpportunityStage[opp.stage] != CandidateOpportunityStage.prospect &&
          CandidateOpportunityStage[opp.stage] != CandidateOpportunityStage.candidateMistakenProspect)
    }
  }

  fetchCandidate() {
    this.candidateService.getProfile().subscribe(
      (candidate) => {
        this.setCandidate(candidate);
        this.activeDuolingoTask = this.getActiveDuolingoTask();
        this.filterOppsToDisplay();
        this.usAfghan = candidate.surveyType?.id === US_AFGHAN_SURVEY_TYPE;
        this.initServicesTabVisibility()
        this.loading = false;
      },
      (error) => {
        this.error = error;
        this.loading = false;
      });
  }

  /**
   * Initialises the visibility of the services tab by checking eligibility for each available
   * service. The tab is shown if the candidate is eligible for any service.
   */
  private initServicesTabVisibility() {
    const results$ = forkJoin({
      linkedIn: this.linkedinService.isEligible(this.candidate.id),
      reference: this.casiPortalService.checkEligibility('REFERENCE', 'VOUCHER'),
      unhcr: this.casiPortalService.checkEligibility('UNHCR', 'HELP_SITE_LINK')
      // Additional async service eligibility calls here
    }).pipe(shareReplay(1)); // Avoid re-triggering on multiple subscriptions

    this.showServicesTab$ = results$.pipe(
      map(results => results.linkedIn || (this.isLocalEnv() && results.reference) || results.unhcr || !!this.activeDuolingoTask)
    );

    this.linkedinEligible$ = results$.pipe(
      map(results => results.linkedIn)
    );

    this.referenceEligible$ = results$.pipe(
      map(results => this.isLocalEnv() && results.reference)
    );

    this.unhcrEligible$ = results$.pipe(
      map(results => results.unhcr)
    );
  }

  private isLocalEnv(): boolean {
    return environment.environmentName === 'local';
  }

  private fetchCachedTab() {
    const cachedActiveTabID: string = this.localStorageService.get(this.lastTabKey);
    // If there isn't a cached active tab, set it to the defaultTabId
    this.activeTabId = cachedActiveTabID != null ? cachedActiveTabID : this.defaultTabId;
    this.setTabParam(this.activeTabId)
  }

  onTabChanged(event: NgbNavChangeEvent) {
    this.setActiveTabId(event.nextId);
    this.setTabParam(event.nextId);
  }

  private setActiveTabId(id: string) {
    this.activeTabId = id;
    this.localStorageService.set(this.lastTabKey, id);
  }

  // Update the URL to include the tab param and the current active tab
  setTabParam(activeTab: string) {
    const currentUrl = this.location.path();
    const baseUrl = currentUrl.split('?')[0];
    const updatedUrl = `${baseUrl}?tab=${activeTab}`;
    this.location.replaceState(updatedUrl);
  }

  private setCandidate(candidate: Candidate) {
    this.candidate = candidate;
    if (this.canViewChats) {
      this.getCandidateProspectChat();
      this.fetchAllOpportunityChats();
    }
  }

  private getActiveDuolingoTask(): TaskAssignment | null {
    const task = this.candidate?.taskAssignments.find(t =>
      (t.task.name === 'claimCouponButton' || t.task.name === 'duolingoTest'));
    return task || null;
  }


  private getCandidateProspectChat() {
    this.chatService.getCandidateProspectChat(this.candidate.id).subscribe(result => {
      if (result) {
        this.setSourceChat(result);
      }
    })
  }

  isCandidateMuted() {
    return isMuted(this.candidate);
  }

  private setSourceChat(chat:JobChat) {
    this.sourceChat = chat;

    //Get chat info to see whether sourceChat has any posts
    this.chatService.getJobChatUserInfo(this.sourceChat).subscribe({
        next: info => {
          this.processChatInfo(info)
        },
        error: err => {console.log(err)}
      }
    );
  }

  private processChatInfo(info: JobChatUserInfo) {
    //Has posts if lastPostId is not null
    this.sourceChatHasPosts = info.lastPostId != null;

    if (!this.sourceChatHasPosts) {
      //Empty now, but subscribe in case a post comes in

      //Get rid of any existing subscription.
      this.unsubscribeSourceChat();
      //And subscribe for posts
      this.sourceChatSubscription = this.chatService.getChatPosts$(this.sourceChat).subscribe({
          next: (post) => this.onNewPost(post)
        }
      );
    }
  }

  private onNewPost(post: ChatPost) {
    //Note that sourceChat now has posts.
    this.sourceChatHasPosts = true;

    //No longer need to subscribe for posts
    this.unsubscribeSourceChat()
  }

  private unsubscribeSourceChat() {
    if (this.sourceChatSubscription) {
      this.sourceChatSubscription.unsubscribe();
      this.sourceChatSubscription = null;
    }
  }

  private fetchAllOpportunityChats() {

    //Get all candidate's opportunities
    let candidateOpportunities = this.candidate.candidateOpportunities;
    const candidateId = this.candidate.id;

    //Scan the opportunities to extract all their chats.
    let chats$ = candidateOpportunities
    //First map opportunities to stream of arrays of chat requests for each opportunity
    .map(opp => [
        {type: JobChatType.CandidateRecruiting, candidateId: candidateId, jobId: opp.jobOpp?.id},
        {type: JobChatType.CandidateProspect, candidateId: candidateId},
        {type: JobChatType.AllJobCandidates, jobId: opp.jobOpp?.id}
      ]
    )
    //Convert this stream of arrays of requests, into a stream of requests (ie flattening the arrays)
    .reduce((accumulator,
             requests) => accumulator.concat(requests), [])
    //Lastly map the requests to Observable<JobChat> by calling the service with each request.
    .map(request => this.chatService.getOrCreate(request));

    //Now fetch all those chats
    this.loading = true;
    this.error = null;
    forkJoin(chats$).subscribe(
      (jobChats) => {
        //Filter out duplicate chats (a CandidateProspect chat can be shared across multiple opps)
        this.chatsForAllJobs = this.chatService.removeDuplicateChats(jobChats);
        this.loading = false;
      },
      (error) => {
        this.error = error;
        this.loading = false;
      }
    )
  }

  onMarkChatAsRead() {
    this.chatService.markChatAsRead(this.sourceChat);
  }

}
