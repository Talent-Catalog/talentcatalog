/*
 * Copyright (c) 2021 Talent Beyond Boundaries.
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

import {AfterViewChecked, Component, OnInit} from '@angular/core';
import {NgbNavChangeEvent} from "@ng-bootstrap/ng-bootstrap";
import {SavedSearchSubtype, SavedSearchType} from "../../model/saved-search";
import {CandidateSourceType, SearchBy, SearchOppsBy} from "../../model/base"
import {LocalStorageService} from "angular-2-local-storage";
import {
  SavedSearchService,
  SavedSearchTypeInfo,
  SavedSearchTypeSubInfo
} from "../../services/saved-search.service";
import {FormBuilder} from "@angular/forms";
import {AuthService} from "../../services/auth.service";
import {Partner} from "../../model/partner";

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
})
export class HomeComponent implements OnInit, AfterViewChecked {

  activeTabId: string;
  private lastTabKey: string = 'HomeLastTab';
  private lastCategoryTabKey: string = 'HomeLastCategoryTab';
  loggedInPartner: Partner;

  savedSearchTypeInfos: SavedSearchTypeInfo[];
  savedSearchTypeSubInfos: SavedSearchTypeSubInfo[];
  selectedSavedSearchSubtype: SavedSearchSubtype;

  constructor(
    private fb: FormBuilder,
    private localStorageService: LocalStorageService,
    private savedSearchService: SavedSearchService,
    private authService: AuthService
  ) {
    this.savedSearchTypeInfos = savedSearchService.getSavedSearchTypeInfos();
  }

  ngOnInit() {
    this.savedSearchTypeSubInfos = this.savedSearchTypeInfos[0].categories;
    this.loggedInPartner = this.authService.getLoggedInUser()?.partner;
  }

  ngAfterViewChecked(): void {
    //This is called in order for the navigation tabs, this.nav, to be set.
    this.selectDefaultTab()
  }

  onTabChanged(event: NgbNavChangeEvent) {
    this.setActiveTabId(event.nextId);
  }

  onSavedSearchSubtypeChange($event: SavedSearchTypeSubInfo) {
    this.setSelectedSavedSearchSubtype($event.savedSearchSubtype);
  }

  private selectDefaultTab() {
    const defaultActiveTabID: string = this.localStorageService.get(this.lastTabKey);
    this.setActiveTabId(defaultActiveTabID == null ? "LiveJobs" : defaultActiveTabID);

    if (defaultActiveTabID == null) {
      this.setSelectedSavedSearchSubtype(this.savedSearchTypeSubInfos[0].savedSearchSubtype);
    } else {
      const defaultCategory: string = this.localStorageService.get(this.lastCategoryTabKey);
      this.setSelectedSavedSearchSubtype(defaultCategory == null ? 0 : +defaultCategory);
    }
  }

  private setActiveTabId(id: string) {

    this.activeTabId = id;

    //The typed saved search tabs have id's which look like "type:profession", "type:jobs",
    //"type:other". Unpack the id to identify the search type
    const parts = id.split(':');
    if (parts[0] === 'type' && parts.length === 2) {

      const type: SavedSearchType = SavedSearchType[parts[1]];
      this.savedSearchTypeSubInfos = this.savedSearchTypeInfos[type].categories;

    }

    this.localStorageService.set(this.lastTabKey, id);
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

  canCreateJob(): boolean {
    return this.authService.canCreateJob();
  }

  isExperimental() {
    return false;
  }

  ownsOpps(): boolean {
    return this.authService.ownsOpps();
  }
}
