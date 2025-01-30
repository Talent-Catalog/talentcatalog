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
    protected location: Location
  ) {
    this.savedSearchTypeInfos = savedSearchService.getSavedSearchTypeInfos();
  }

  ngOnInit() {
    this.savedSearchTypeSubInfos = this.savedSearchTypeInfos[0].categories;
    this.loggedInPartner = this.authenticationService.getLoggedInUser()?.partner;
    // This is called in order for the navigation tabs, this.nav, to be set.
    // Make this call in ngOnInit(). Do not do it ngAfterViewChecked() - doing so will throw
    // NG0100 errors because selectDefaultTabs() changes the activeTabId after the view has been
    // checked.
    // See: https://angular.io/errors/NG0100
    this.selectDefaultTab();
  }

  onTabChanged(event: NgbNavChangeEvent) {
    this.setActiveTabId(event.nextId);
  }

  onSavedSearchSubtypeChange($event: SavedSearchTypeSubInfo) {
    this.setSelectedSavedSearchSubtype($event.savedSearchSubtype);
  }

  private selectDefaultTab() {
    const defaultActiveTabID: string = this.localStorageService.get(this.lastTabKey);
    this.setActiveTabId(defaultActiveTabID == null ? this.defaultTabId : defaultActiveTabID);

    if (defaultActiveTabID == null) {
      this.setSelectedSavedSearchSubtype(this.savedSearchTypeSubInfos[0].savedSearchSubtype);
    } else {
      const defaultCategory: string = this.localStorageService.get(this.lastCategoryTabKey);
      this.setSelectedSavedSearchSubtype(defaultCategory == null ? 0 : +defaultCategory);
    }
  }

  protected setActiveTabId(id: string) {

    this.activeTabId = id;

    //The typed saved search tabs have id's which look like "type:profession", "type:jobs",
    //"type:other". Unpack the id to identify the search type
    const parts = id.split(':');
    if (parts[0] === 'type' && parts.length === 2) {

      const type: SavedSearchType = SavedSearchType[parts[1]];
      this.savedSearchTypeSubInfos = this.savedSearchTypeInfos[type].categories;

    }

    this.localStorageService.set(this.lastTabKey, id);

    this.setTabParam(id);
  }


  setTabParam(activeTab: string) {
    const currentUrl = this.location.path();
    const baseUrl = currentUrl.split('?')[0];
    const updatedUrl = `${baseUrl}?tab=${activeTab}`;
    this.location.replaceState(updatedUrl); // Update the URL without reloading
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
