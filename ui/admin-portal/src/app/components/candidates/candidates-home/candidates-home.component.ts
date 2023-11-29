import {Component} from '@angular/core';
import {SavedSearchService} from "../../../services/saved-search.service";
import {LocalStorageService} from "angular-2-local-storage";
import {AuthorizationService} from "../../../services/authorization.service";
import {AuthenticationService} from "../../../services/authentication.service";
import {HomeComponent} from "../home.component";

@Component({
  selector: 'app-candidates-home',
  templateUrl: './candidates-home.component.html',
  styleUrls: ['./candidates-home.component.scss']
})
export class CandidatesHomeComponent extends HomeComponent {

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

}
