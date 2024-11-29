import {Component} from '@angular/core';
import {SavedSearchService} from "../../../services/saved-search.service";
import {LocalStorageService} from "angular-2-local-storage";
import {AuthorizationService} from "../../../services/authorization.service";
import {AuthenticationService} from "../../../services/authentication.service";
import {HomeComponent} from "../../candidates/home.component";


@Component({
  selector: 'app-list-home',
  templateUrl: './list-home.component.html',
  styleUrls: ['./list-home.component.scss']
})
export class ListHomeComponent extends HomeComponent {

  constructor(
    protected localStorageService: LocalStorageService,
    protected savedSearchService: SavedSearchService,
    protected authService: AuthorizationService,
    protected authenticationService: AuthenticationService
  ) {
    super(localStorageService, savedSearchService, authService, authenticationService);
    this.lastTabKey = 'CandidateHomeLastTab';
    this.lastCategoryTabKey = 'CandidateHomeLastCategoryTab';
    this.defaultTabId = 'MyLists';
  }

  seesPublicLists() {
    //Employers are not interested in public lists
    return !this.authorizationService.isEmployerPartner();
  }

  public canSeeJobDetails() {
    return this.authorizationService.canSeeJobDetails()
  }

}
