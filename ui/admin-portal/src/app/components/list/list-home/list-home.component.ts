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
import {LocalStorageService} from "../../../services/local-storage.service";
import {Location} from "@angular/common";
import {ActivatedRoute} from "@angular/router";


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
    protected authenticationService: AuthenticationService,
    protected location: Location,
    protected route: ActivatedRoute
  ) {
    super(localStorageService, savedSearchService, authService, authenticationService, location, route);
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
