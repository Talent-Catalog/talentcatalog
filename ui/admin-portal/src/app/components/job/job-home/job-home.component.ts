import {Component} from '@angular/core';
import {SavedSearchService} from "../../../services/saved-search.service";
import {LocalStorageService} from "angular-2-local-storage";
import {AuthorizationService} from "../../../services/authorization.service";
import {AuthenticationService} from "../../../services/authentication.service";
import {HomeComponent} from "../../candidates/home.component";
import {SearchOppsBy} from "../../../model/base";
import {BehaviorSubject, Subject} from "rxjs";
import {SearchOpportunityRequest} from "../../../model/candidate-opportunity";
import {OpportunityOwnershipType} from "../../../model/opportunity";
import {CandidateOpportunityService} from "../../../services/candidate-opportunity.service";
import {JobChatUserInfo} from "../../../model/chat";

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

  error: any;

  constructor(
    private candidateOpportunityService: CandidateOpportunityService,
    protected localStorageService: LocalStorageService,
    protected savedSearchService: SavedSearchService,
    protected authService: AuthorizationService,
    protected authenticationService: AuthenticationService
  ) {
    super(localStorageService, savedSearchService, authService, authenticationService);
    this.lastTabKey = 'JobsHomeLastTab';
    this.lastCategoryTabKey = 'JobsHomeLastCategoryTab';
    this.defaultTabId = 'LiveJobs';
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
}
