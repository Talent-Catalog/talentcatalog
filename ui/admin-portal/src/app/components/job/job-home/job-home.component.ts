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

import {Component} from '@angular/core';
import {SavedSearchService} from "../../../services/saved-search.service";
import {AuthorizationService} from "../../../services/authorization.service";
import {AuthenticationService} from "../../../services/authentication.service";
import {HomeComponent} from "../../candidates/home.component";
import {BehaviorSubject, Subject} from "rxjs";
import {SearchOpportunityRequest} from "../../../model/candidate-opportunity";
import {OpportunityOwnershipType} from "../../../model/opportunity";
import {CandidateOpportunityService} from "../../../services/candidate-opportunity.service";
import {JobChatUserInfo} from "../../../model/chat";
import {SearchJobRequest} from "../../../model/job";
import {JobService} from "../../../services/job.service";
import {CandidateService} from "../../../services/candidate.service";
import {SearchOppsBy} from "../../../model/base";
import {LocalStorageService} from "../../../services/local-storage.service";
import Clarity from '@microsoft/clarity';

@Component({
  selector: 'app-job-home',
  templateUrl: './job-home.component.html',
  styleUrls: ['./job-home.component.scss']
})
export class JobHomeComponent extends HomeComponent {

  /**
   * This tracks the overall read status of all the chats that it manages.
   * It is used to drive the read status component on the tab in the Jobs home component - which displays an asterisk
   * if some chats are unread.
   * <p/>
   * This component can call next on this subject if it knows that some of the chats it manages
   * are unread. The fact that it is a BehaviorSubject means that you can query the current status
   * of the higher level component.
   */
  jobCreatorChatsRead$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(null);
  sourcePartnerChatsRead$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(null);
  partnerJobChatsRead$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(null);
  starredJobChatsRead$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(null);
  candidatesWithChatRead$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(null);

  error: any;

  constructor(
    private candidateOpportunityService: CandidateOpportunityService,
    private jobService: JobService,
    protected localStorageService: LocalStorageService,
    protected savedSearchService: SavedSearchService,
    protected authorizationService: AuthorizationService,
    protected authenticationService: AuthenticationService,
    protected candidateService: CandidateService
  ) {
    super(localStorageService, savedSearchService, authorizationService, authenticationService);
    this.lastTabKey = 'JobsHomeLastTab';
    this.lastCategoryTabKey = 'JobsHomeLastCategoryTab';
    this.defaultTabId = 'StarredJobs';
  }

  ngOnInit() {
    super.ngOnInit();
    this.loadChatReadStatuses();
  }

  private loadChatReadStatuses() {
    let req = new SearchOpportunityRequest();
    req.ownedByMyPartner = true;
    req.activeStages = true;
    req.ownershipType = OpportunityOwnershipType.AS_JOB_CREATOR;
    this.candidateOpportunityService.checkUnreadChats(req).subscribe({
        next: info => this.processChatsReadStatus(this.jobCreatorChatsRead$, info),
        error: error => this.error = error
      }
    )

    req.ownershipType = OpportunityOwnershipType.AS_SOURCE_PARTNER;
    this.candidateOpportunityService.checkUnreadChats(req).subscribe({
        next: info => this.processChatsReadStatus(this.sourcePartnerChatsRead$, info),
        error: error => this.error = error
      }
    )

    let jobReq = new SearchJobRequest();
    jobReq.ownedByMyPartner = true;
    jobReq.activeStages = true;
    jobReq.starred = null;
    this.jobService.checkUnreadChats(jobReq).subscribe({
        next: info => this.processChatsReadStatus(this.partnerJobChatsRead$, info),
        error: error => this.error = error
      }
    )

    jobReq.ownedByMyPartner = null;
    jobReq.activeStages = null;
    jobReq.starred = true;
    this.jobService.checkUnreadChats(jobReq).subscribe({
        next: info => this.processChatsReadStatus(this.starredJobChatsRead$, info),
        error: error => this.error = error
      }
    )

    this.candidateService.checkUnreadChats().subscribe({
      next: info => this.processChatsReadStatus(this.candidatesWithChatRead$, info),
      error: error => this.error = error
    })

  }

  private processChatsReadStatus(subject: Subject<boolean>, info: JobChatUserInfo) {
    if (subject) {
      subject.next(info.numberUnreadChats === 0);
    }
  }

  /**
   * Compute my cases tab name - only inserting Job or Source into name if it is needed
   * to clarify different types in the case where we a dealing with a partner who is both a
   * job creator and source partner (eg TBB)
   * @param searchOppsBy Type of case search.
   */
  myCasesTabName(searchOppsBy: SearchOppsBy): string {
    const partnerName = this.loggedInPartner?.abbreviation;
    let tabName;
    if (this.isJobCreator() && this.isSourcePartner()) {
      const extra = searchOppsBy === SearchOppsBy.mineAsJobCreator ? " Job" : " Source";
      tabName = partnerName + extra + " Cases"
    } else {
      tabName = partnerName + " Cases"
    }
    return tabName;
  }

  seesAllLiveJobs() {
    //Everyone sees public live jobs - except for employers with direct access. They are only
    //interested in their own jobs.
    return !this.authorizationService.isEmployerPartner();
  }

  onTabChanged(event: any): void {
    const tabId = event.nextId; // Get the ID of the selected tab
  
    // Map tab IDs to meaningful names for tracking
    const tabNameMapping: { [key: string]: string } = {
      LiveJobs: 'Live Jobs',
      StarredJobs: 'Starred Jobs',
      MyJobs: 'TBB Jobs',
      MyCasesAsJobCreator: 'TBB Job cases',
      MyCasesAsSourcePartner: 'TBB Source cases',
      NewJob: 'New Job',
      MyCandidateChatsAsSourcePartner: 'Candidate Chats'
    };
  
    const tabName = tabNameMapping[tabId] || tabId; // Fallback to tabId if no mapping exists
   
     // Update the URL without reloading the page
     // window.history.replaceState(null, '', `/jobs?tab=${tabId}`);
 
    // Send a custom event to Clarity to track the tab change
    // Clarity.event(`TabChange_${tabId}_${tabName}`);
     // Combine pageUrl and tabName into a single string for Clarity
  const clarityEventData = `VirtualPageView | PageUrl: /jobs/tab/${tabId}, TabName: ${tabName}`;

  Clarity.setTag("tabName", tabName);
  // Send a single-parameter event to Clarity
  Clarity.event(clarityEventData);
    // console.log(`Clarity.event('TabChange_${tabId}_${tabName}') tracked`);
    console.log(tabName);
  }
  
}
