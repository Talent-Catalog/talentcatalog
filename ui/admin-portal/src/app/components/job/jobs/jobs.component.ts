import {Component, Inject, LOCALE_ID} from '@angular/core';
import {AuthorizationService} from "../../../services/authorization.service";
import {LocalStorageService} from "angular-2-local-storage";
import {FormBuilder} from "@angular/forms";
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

@Component({
  selector: 'app-jobs',
  templateUrl: './jobs.component.html',
  styleUrls: ['./jobs.component.scss']
})
export class JobsComponent extends FilteredOppsComponentBase<Job> {

  //Override text to replace "opps" text with "jobs"
  myOppsOnlyLabel = "My jobs only";
  myOppsOnlyTip = "Only show jobs that I created or am the contact for";
  showClosedOppsLabel = "Show closed jobs";
  showClosedOppsTip = "Show jobs that have been closed";
  showInactiveOppsLabel = "Show inactive jobs";
  showInactiveOppsTip = "Show jobs that are not currently accepting new candidates";
  withUnreadMessagesLabel = "Jobs with unread chats only";
  withUnreadMessagesTip = "Only show jobs which have unread chats";

  constructor(
    chatService: ChatService,
    fb: FormBuilder,
    authService: AuthorizationService,
    localStorageService: LocalStorageService,
    oppService: JobService,
    salesforceService: SalesforceService,
    countryService: CountryService,
    partnerService: PartnerService,
    @Inject(LOCALE_ID) locale: string
  ) {
    super(chatService, fb, authService, localStorageService, oppService, salesforceService,
      countryService, partnerService, locale, "Jobs")
  }

  protected createSearchRequest(): SearchOpportunityRequest {
    let req =  new SearchJobRequest();

    switch (this.searchBy) {
      case SearchOppsBy.live:

        //Don't want to see closed jobs
        req.sfOppClosed = false;

        //Jobs must have been published
        req.published = true;

        //Only want jobs which are accepting candidates. This is equivalent to checking that the
        //job's stage is between candidate search and prior to job offer/acceptance.
        //This request is ignored if certain stages have been requested (because that will clash
        //with checking the above range of stages)
        req.activeStages = true;
        break;

      case SearchOppsBy.starredByMe:
        req.starred = true;
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
}
