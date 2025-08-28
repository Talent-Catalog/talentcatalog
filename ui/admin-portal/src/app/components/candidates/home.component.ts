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

import {Component, OnInit} from '@angular/core';
import {NgbNavChangeEvent} from "@ng-bootstrap/ng-bootstrap";
import {SavedSearchSubtype, SavedSearchType} from "../../model/saved-search";
import {CandidateSourceType, SearchBy, SearchOppsBy} from "../../model/base"
import {SavedSearchService, SavedSearchTypeInfo, SavedSearchTypeSubInfo} from "../../services/saved-search.service";
import {AuthorizationService} from "../../services/authorization.service";
import {Partner} from "../../model/partner";
import {AuthenticationService} from "../../services/authentication.service";
import {LocalStorageService} from "../../services/local-storage.service";
import {Location} from "@angular/common";
import {ActivatedRoute} from "@angular/router";

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
})
export class HomeComponent implements OnInit {

  activeTabId: string;
  protected defaultTabId: string;
  protected lastTabKey: string = 'HomeLastTab';
  protected lastCategoryTabKey: string = 'HomeLastCategoryTab';
  loggedInPartner: Partner;

  savedSearchTypeInfos: SavedSearchTypeInfo[];
  savedSearchTypeSubInfos: SavedSearchTypeSubInfo[];
  selectedSavedSearchSubtype: SavedSearchSubtype;

  constructor(
    protected localStorageService: LocalStorageService,
    protected savedSearchService: SavedSearchService,
    protected authorizationService: AuthorizationService,
    protected authenticationService: AuthenticationService,
    protected location: Location,
    protected route: ActivatedRoute
  ) {
    this.savedSearchTypeInfos = savedSearchService.getSavedSearchTypeInfos();
  }

  ngOnInit() {
    this.savedSearchTypeSubInfos = this.savedSearchTypeInfos[0].categories;
    this.fetchCachedSubCategory();
    this.loggedInPartner = this.authenticationService.getLoggedInUser()?.partner;
    // This is called in order for the navigation tabs, this.nav, to be set.
    // Make this call in ngOnInit(). Do not do it ngAfterViewChecked() - doing so will throw
    // NG0100 errors because selectDefaultTabs() changes the activeTabId after the view has been
    // checked.
    // See: https://angular.io/errors/NG0100
    this.route.queryParams.subscribe(params => {
      const tab = params['tab'];
      // If there is a tab param, set that as the active tab
      // If there is no tab param, check the browser cache for the last active tab or if none get the default tab.
      tab ? this.setActiveTabId(tab) : this.fetchCachedTab();
    });
  }

  onTabChanged(event: NgbNavChangeEvent) {
    this.setActiveTabId(event.nextId);
    this.setTabParam(event.nextId);
  }

  onSavedSearchSubtypeChange($event: SavedSearchTypeSubInfo) {
    this.setSelectedSavedSearchSubtype($event.savedSearchSubtype);
  }

  private fetchCachedTab() {
    const cachedActiveTabId: string = this.localStorageService.get(this.lastTabKey);
    // If there isn't a cached active tab, set it to the defaultTabId
    this.activeTabId = cachedActiveTabId != null ? cachedActiveTabId : this.defaultTabId;
    this.setTabParam(this.activeTabId);
  }

  private fetchCachedSubCategory() {
    // Get and set the subtype category (e.g. occupation category in search by occupations tab)
    const cachedSubCategory: string = this.localStorageService.get(this.lastCategoryTabKey);
    this.selectedSavedSearchSubtype = cachedSubCategory == null ? 0 : +cachedSubCategory;
  }

  protected setActiveTabId(id: string) {
    this.activeTabId = id;
    this.localStorageService.set(this.lastTabKey, id);
  }

  // Update the URL to include the tab param and the current active tab
  setTabParam(activeTab: string) {
    const currentUrl = this.location.path();
    const baseUrl = currentUrl.split('?')[0];
    const updatedUrl = `${baseUrl}?tab=${activeTab}`;
    this.location.replaceState(updatedUrl);
  }

  private setSelectedSavedSearchSubtype(savedSearchSubtype: number) {
    this.selectedSavedSearchSubtype = savedSearchSubtype;
    this.localStorageService.set(this.lastCategoryTabKey, this.selectedSavedSearchSubtype);
  }

  //Make some Enum types visible in HTML

  get CandidateSourceType() {
    return CandidateSourceType;
  }

  //MODEL: Exposing an Enum to the html. This can also be used to expose external functions
  //For example see how isHtml is exposed.
  get SearchBy() {
    return SearchBy;
  }

  get SearchOppsBy() {
    return SearchOppsBy;
  }

  get SavedSearchType() {
    return SavedSearchType;
  }

  isExperimental() {
    return false;
  }

  ownsOpps(): boolean {
    return this.authorizationService.ownsOpps();
  }

  isJobCreator(): boolean {
    return this.authorizationService.isJobCreatorPartner();
  }

  isReadOnly(): boolean {
    return this.authorizationService.isReadOnly();
  }

  isSourcePartner(): boolean {
    return this.authorizationService.isSourcePartner();
  }
}
