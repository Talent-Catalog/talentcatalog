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

import {Component, ElementRef, Inject, LOCALE_ID, ViewChild} from '@angular/core';
import {AuthorizationService} from "../../../services/authorization.service";
import {UntypedFormBuilder} from "@angular/forms";
import {Job, JobOpportunityStage, SearchJobRequest} from "../../../model/job";
import {JobService} from "../../../services/job.service";
import {EnumOption, enumOptions} from "../../../util/enum";
import {SearchOppsBy} from "../../../model/base";
import {FilteredOppsComponentBase} from "../../util/opportunity/FilteredOppsComponentBase";
import {SalesforceService} from "../../../services/salesforce.service";
import {SearchOpportunityRequest} from "../../../model/candidate-opportunity";
import {CountryService} from "../../../services/country.service";
import {SearchResults} from "../../../model/search-results";
import {forkJoin, Observable} from "rxjs";
import {CreateChatRequest, JobChat, JobChatType} from "../../../model/chat";
import {ChatService} from "../../../services/chat.service";
import {PartnerService} from "../../../services/partner.service";
import {LocalStorageService} from "../../../services/local-storage.service";

@Component({
  selector: 'app-jobs',
  templateUrl: './jobs.component.html',
  styleUrls: ['./jobs.component.scss']
})
export class JobsComponent extends FilteredOppsComponentBase<Job> {

  //Override text to replace "opps" text with "jobs"
  myOppsOnlyLabel = "My jobs only";
  myOppsOnlyTip = "Only show jobs that I created or am the contact for";
  showUnpublishedLabel = "Show unpublished jobs";
  showUnpublishedTip = "Show jobs that have not yet been published";
  showClosedOppsLabel = "Show closed jobs";
  showClosedOppsTip = "Show jobs that have been closed";
  showInactiveOppsLabel = "Show inactive jobs";
  showInactiveOppsTip = "Show jobs that are not currently accepting new candidates";
  withUnreadMessagesLabel = "Jobs with unread chats only";
  withUnreadMessagesTip = "Only show jobs which have unread chats";

  @ViewChild("searchFilter")
  declare searchFilter: ElementRef;

  constructor(
    chatService: ChatService,
    fb: UntypedFormBuilder,
    authorizationService: AuthorizationService,
    localStorageService: LocalStorageService,
    oppService: JobService,
    salesforceService: SalesforceService,
    countryService: CountryService,
    partnerService: PartnerService,
    @Inject(LOCALE_ID) locale: string
  ) {
    super(chatService, fb, authorizationService, localStorageService, oppService, salesforceService,
      countryService, partnerService, locale, "Jobs")
  }

  protected createSearchRequest(): SearchOpportunityRequest {
    let req =  new SearchJobRequest();

    switch (this.searchBy) {
      case SearchOppsBy.live:

        //Don't want to see closed jobs
        req.sfOppClosed = false;

        //Only want jobs which are accepting candidates. This is equivalent to checking that the
        //job's stage is between candidate search and prior to job offer/acceptance.
        //This request is ignored if certain stages have been requested (because that will clash
        //with checking the above range of stages)
        req.activeStages = true;
        break;

      case SearchOppsBy.starredByMe:
        req.starred = true;

        //If it is starred I want to see it even if it is closed
        req.sfOppClosed = true;
        break;
    }

    return req;
  }

  protected loadStages(): EnumOption[] {
    return enumOptions(JobOpportunityStage);
  }


  /**
   * Override inherited method which processes the search results into this.opps so that
   * we can fetch the opps chats.
   * @param results
   * @protected
   */
  protected processSearchResults(results: SearchResults<Job>) {
    //Call standard processing (which puts the results into this.opps)
    super.processSearchResults(results);

    //Then fetch the chats associated with all opps.
    this.fetchChats();
  }

  private fetchChats() {

    //We want all the Observables grouped by opp
    let oppsChats$: Observable<JobChat[]>[] = [];

    for (const opp of this.opps) {
      let chatRequests = this.constructChatRequestsForOpp(opp);

      //Convert the requests to chat observables
      let chats$ = chatRequests.map(
        request => this.chatService.getOrCreate(request));

      //Add the forkJoin Observable which will return all the chats for this opp into our array
      oppsChats$.push(forkJoin(chats$));
    }

    //This is a forkJoin of forkJoins - one for each opp.
    this.error = null;
    forkJoin(oppsChats$).subscribe({
        next: chatsByOpp => this.processOppChats(chatsByOpp),
        error: err => this.error = err
      }
    )
  }

  private constructChatRequestsForOpp(opp: Job) {
    let chatRequests: CreateChatRequest[] = [];

    chatRequests.push({
      type: JobChatType.AllJobCandidates,
      jobId: opp?.id,
    });

    chatRequests.push({
      type: JobChatType.JobCreatorAllSourcePartners,
      jobId: opp?.id
    });

    //There is a chat for each source partner
    for (const sourcePartner of this.sourcePartners) {
      chatRequests.push({
        type: JobChatType.JobCreatorSourcePartner,
        jobId: opp?.id,
        sourcePartnerId: sourcePartner.id
      })
    }
    return chatRequests;
  }

  needsFilterByDestination() {
    //Employers with direct access know that all jobs are coming to their destination.
    return !this.authorizationService.isEmployerPartner();
  }

  public canSeeJobDetails() {
    return this.authorizationService.canSeeJobDetails()
  }

}
