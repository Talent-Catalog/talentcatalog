import {Component} from '@angular/core';
import {SavedSearchService} from "../../../services/saved-search.service";
import {LocalStorageService} from "angular-2-local-storage";
import {AuthorizationService} from "../../../services/authorization.service";
import {AuthenticationService} from "../../../services/authentication.service";
import {HomeComponent} from "../../candidates/home.component";
import {SearchOppsBy} from "../../../model/base";

@Component({
  selector: 'app-job-home',
  templateUrl: './job-home.component.html',
  styleUrls: ['./job-home.component.scss']
})
export class JobHomeComponent extends HomeComponent {

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
