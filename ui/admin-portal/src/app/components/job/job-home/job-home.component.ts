import {Component} from '@angular/core';
import {SavedSearchService} from "../../../services/saved-search.service";
import {LocalStorageService} from "angular-2-local-storage";
import {AuthorizationService} from "../../../services/authorization.service";
import {AuthenticationService} from "../../../services/authentication.service";
import {HomeComponent} from "../../candidates/home.component";
import {SearchOppsBy} from "../../../model/base";
import {BehaviorSubject} from "rxjs";

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
  jobCreatorChatsRead$: BehaviorSubject<boolean>;
  sourcePartnerChatsRead$: BehaviorSubject<boolean>;

  constructor(
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
    //Initialize with null - Unknown.
    this.jobCreatorChatsRead$ = new BehaviorSubject<boolean>(null);
    this.sourcePartnerChatsRead$ = new BehaviorSubject<boolean>(null);
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
