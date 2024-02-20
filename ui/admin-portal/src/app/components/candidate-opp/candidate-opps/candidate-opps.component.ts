import {Component, Inject, Input, LOCALE_ID, SimpleChanges} from '@angular/core';
import {
  CandidateOpportunity,
  CandidateOpportunityStage,
  SearchOpportunityRequest
} from "../../../model/candidate-opportunity";
import {CandidateOpportunityService} from "../../../services/candidate-opportunity.service";
import {LocalStorageService} from "angular-2-local-storage";
import {FormBuilder} from "@angular/forms";
import {SalesforceService} from "../../../services/salesforce.service";
import {AuthorizationService} from "../../../services/authorization.service";
import {EnumOption, enumOptions} from "../../../util/enum";
import {FilteredOppsComponentBase} from "../../util/opportunity/FilteredOppsComponentBase";
import {CountryService} from "../../../services/country.service";
import {CreateChatRequest, JobChat, JobChatType} from "../../../model/chat";
import {forkJoin} from "rxjs";
import {ChatService} from "../../../services/chat.service";
import {SearchResults} from "../../../model/search-results";

@Component({
  selector: 'app-candidate-opps',
  templateUrl: './candidate-opps.component.html',
  styleUrls: ['./candidate-opps.component.scss']
})
export class CandidateOppsComponent extends FilteredOppsComponentBase<CandidateOpportunity> {
  /**
   * The opps to be displayed are either specified by this input - which just supplies an
   * array of opps, or by the inherited searchBy input which does a search.
   * Only one of those inputs should be selected.
   */
  @Input() candidateOpps: CandidateOpportunity[];

  /**
   * Display the name of the job opportunity rather than the name of the candidate opportunity.
   * <p/>
   * This is useful when you are displaying the Jobs that a candidate has gone for.
   */
  @Input() showJobOppName: boolean = false;

  /**
   * This is to determine if the component is being viewed from the candidate preview (from within a list/search) or not.
   * If preview is true we have less space to display the component so can use this boolean to make space efficient changes of the CSS.
   */
  @Input() preview: boolean = false;

  //Override text to replace "opps" text with "cases"
  myOppsOnlyLabel = "My cases only";
  myOppsOnlyTip = "Only show cases that I am the contact for";
  overdueOppsOnlyLabel = "Overdue next step cases only";
  overdueOppsOnlyTip = "Only show cases whose NextStep is overdue";
  showClosedOppsLabel = "Show closed cases";
  showClosedOppsTip = "Show cases that have been closed";
  showInactiveOppsLabel = "Show inactive cases";
  showInactiveOppsTip = "Show cases that are no longer active - " +
    "for example if the candidate has already relocated";


  /**
   * Map of opp id to opp chats
   */
  private oppChats: Map<number, JobChat[]> = new Map<number, JobChat[]>();


  constructor(
    private chatService: ChatService,
    fb: FormBuilder,
    authService: AuthorizationService,
    localStorageService: LocalStorageService,
    oppService: CandidateOpportunityService,
    salesforceService: SalesforceService,
    countryService: CountryService,
    @Inject(LOCALE_ID) locale: string
  ) {
    super(fb, authService, localStorageService, oppService, salesforceService, countryService, locale,
        "Opps")

  }

  ngOnChanges(changes: SimpleChanges): void {
    super.ngOnChanges(changes);

    if (changes.candidateOpps) {
      this.opps = this.candidateOpps;
      this.fetchChats();
    }
  }

  protected createSearchRequest(): SearchOpportunityRequest {
    return new SearchOpportunityRequest();
  }

  protected loadStages(): EnumOption[] {
    return enumOptions(CandidateOpportunityStage);
  }

  isOverdue(opp: CandidateOpportunity) {
    let overdue: boolean = false;
    if (opp.nextStepDueDate) {
      const today = new Date();
      const dueDate = new Date(opp.nextStepDueDate);
      overdue =  dueDate < today;
    }
    return overdue;
  }

  getChats(opp: CandidateOpportunity): JobChat[] {
    return opp ? this.oppChats.get(opp.id) : null;
  }

  /**
   * Override inherited method which processes the search results into this.opps so that
   * we can fetch the opps chats.
   * @param results
   * @protected
   */
  protected processSearchResults(results: SearchResults<CandidateOpportunity>) {
    //Call standard processing (which puts the results into this.opps)
    super.processSearchResults(results);

    //Then fetch the chats associated with all opps.
    this.fetchChats();
  }

  private fetchChats() {
    this.error = null;
    for (const opp of this.opps) {
      const candidateProspectChatRequest: CreateChatRequest = {
        type: JobChatType.CandidateProspect,
        candidateId: opp?.candidate?.id,
      }
      const candidateRecruitingChatRequest: CreateChatRequest = {
        type: JobChatType.CandidateRecruiting,
        candidateId: opp?.candidate?.id,
        jobId: opp?.jobOpp?.id
      }

      forkJoin( [
        this.chatService.getOrCreate(candidateProspectChatRequest),
        this.chatService.getOrCreate(candidateRecruitingChatRequest),
      ]).subscribe(
        chats => {
          this.oppChats.set(opp.id, chats);
        },
        (error) => {
          this.error = error;
        }
      );
    }
  }
}
