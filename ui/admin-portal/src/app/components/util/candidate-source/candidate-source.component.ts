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

import {
  Component,
  EventEmitter,
  Input,
  OnChanges,
  OnInit,
  Output,
  SimpleChanges
} from '@angular/core';
import {
  getCandidateSourceExternalHref,
  getCandidateSourceStatsNavigation,
  getSavedSourceNavigation,
  isSavedSearch,
  SavedSearch,
  SavedSearchRef
} from '../../../model/saved-search';
import {SavedSearchService} from '../../../services/saved-search.service';
import {AuthService} from '../../../services/auth.service';
import {User} from '../../../model/user';
import {CandidateSource, canEditSource, isMine, isStarredByMe} from '../../../model/base';
import {Router} from '@angular/router';
import {Location} from '@angular/common';
import {copyToClipboard} from '../../../util/clipboard';
import {externalDocLink, isSavedList} from "../../../model/saved-list";
import {ConfirmationComponent} from "../confirm/confirmation.component";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {SavedListService} from "../../../services/saved-list.service";
import {SalesforceService} from "../../../services/salesforce.service";
import {AuthenticationService} from "../../../services/authentication.service";


/**
 * This just displays the basic details about a source plus some icons.
 * <p/>
 * It does not display the candidates associated with that source.
 * For that reason some of the events (from clicking on the icons) may have to be passed up to
 * a parent who does managed the associated displayed candidates because actioning the event
 * may require knowledge of, for example, what is the current page of candidates being displayed.
 */
@Component({
  selector: 'app-candidate-source',
  templateUrl: './candidate-source.component.html',
  styleUrls: ['./candidate-source.component.scss']
})
export class CandidateSourceComponent implements OnInit, OnChanges {

  // The data processing is dependant on these variables
  // The candidate source passed in
  @Input() candidateSource: CandidateSource;
  // If the extra data can be loaded for the dropdowns (get request)
  @Input() canLoad: boolean = true;
  // Handles the toggle state of the candidate source (show less/show more)
  @Input() seeMore: boolean;

  // The font awesome icon buttons are dependant on these variables
  @Input() showLink: boolean = true;
  @Input() showMore: boolean = true;
  @Input() showOpen: boolean = true;
  @Input() showRunStats: boolean = true;
  @Input() showWatch: boolean = true;
  @Input() showStarred: boolean = true;
  @Input() showSelect: boolean = false;
  @Input() showCopy: boolean = false;
  @Input() showEdit: boolean = false;
  @Input() showDelete: boolean = false;
  @Input() showAudit: boolean = false;

  @Output() openSource = new EventEmitter<CandidateSource>();
  @Output() selectSource = new EventEmitter<CandidateSource>();
  @Output() copySource = new EventEmitter<CandidateSource>();
  @Output() editSource = new EventEmitter<CandidateSource>();
  @Output() deleteSource = new EventEmitter<CandidateSource>();
  @Output() selectColumns = new EventEmitter<CandidateSource>();
  @Output() toggleStarred = new EventEmitter<CandidateSource>();
  @Output() toggleWatch = new EventEmitter<CandidateSource>();

  loading;
  error;
  private loggedInUser: User;

  constructor(
    public salesforceService: SalesforceService,
    private savedSearchService: SavedSearchService,
    private savedListService: SavedListService,
    private location: Location,
    private modalService: NgbModal,
    private router: Router,
    private authService: AuthService,
    private authenticationService: AuthenticationService
  ) {
  }

  ngOnInit() {
    this.loggedInUser = this.authenticationService.getLoggedInUser();
  }

  ngOnChanges (changes: SimpleChanges){
    // WHEN candidateSource changes IF showAll fetch the savedSearch object
    // which has the multi select Names to display (not just Ids).
    if (this.seeMore && changes && changes.candidateSource
      && changes.candidateSource.previousValue !== changes.candidateSource.currentValue) {

      //Only fetch if we have an id - otherwise changes will just be local
      //modifications of a candidate source which has not been created yet.
      if (this.candidateSource.id) {
        this.getSavedSearch(this.candidateSource.id);
      }
    }
  }

  toggleShowMore() {
    this.seeMore = !this.seeMore;
    // Get extra data from saved search if needed (canLoad:true)
    if (this.canLoad) {
      this.loading = true;
      this.getSavedSearch(this.candidateSource.id);
    }
  }

  doOpenSource(){
    this.openSource.emit(this.candidateSource);
  }

  doRunStats() {
    //Navigate to the infographics requesting it to run stats on this source.
    const urlCommands = getCandidateSourceStatsNavigation(this.candidateSource);
    this.router.navigate(urlCommands);
  }

  doSelectSource(){
    this.selectSource.emit(this.candidateSource);
  }

  doEditSource(){
    this.editSource.emit(this.candidateSource);
  }

  doCopySource(){
    this.copySource.emit(this.candidateSource);
  }

  doDeleteSource(){
    this.deleteSource.emit(this.candidateSource);
  }

  doCopyLink() {
    const text = getCandidateSourceExternalHref(
      this.router, this.location, this.candidateSource);
    copyToClipboard(text);
    const showReport = this.modalService.open(ConfirmationComponent, {
      centered: true, backdrop: 'static'});
    showReport.componentInstance.title = "Copied link to clipboard";
    showReport.componentInstance.showCancel = false;
    showReport.componentInstance.message = "Paste the link (" + text + ") where you want";

  }

  doSelectColumns() {
    this.selectColumns.emit(this.candidateSource);
  }

  doToggleStarred() {
    this.toggleStarred.emit(this.candidateSource);
  }

  isStarred(): boolean {
    return isStarredByMe(this.candidateSource?.users, this.authenticationService);
  }

  doToggleWatch() {
    this.toggleWatch.emit(this.candidateSource);
  }

  isWatching(): boolean {
    return this.candidateSource.watcherUserIds === undefined ? false :
      this.candidateSource.watcherUserIds.indexOf(this.loggedInUser.id) >= 0;
  }

  get savedSearch(): SavedSearch {
    return isSavedSearch(this.candidateSource)
      ? this.candidateSource as SavedSearch : null;
  }

  isSavedSearch() {
    return isSavedSearch(this.candidateSource);
  }

  isShared() {
    return !isMine(this.candidateSource, this.authenticationService);
  }

  isEditable() {
    return canEditSource(this.candidateSource, this.authenticationService);
  }

  isRemovable() {
    // Can't delete global sources
    return !this.candidateSource.global;
  }

  getSavedSearch(savedSearchId: number) {
    this.savedSearchService.get(savedSearchId).subscribe(result => {
      this.candidateSource = result;
      this.loading = false;
    }, err => {
      this.loading = false;
      this.error = err;
    })
  }

  private getSavedSearchSource(): SavedSearchRef {
    if (isSavedList(this.candidateSource)) {
      return this.candidateSource.savedSearchSource;
    } else {
      return null;
    }
  }

  hasSavedSearchSource(): boolean {
    return this.getSavedSearchSource() != null;
  }

  doShowSearch() {
    const savedSearchSource = this.getSavedSearchSource();
    if (savedSearchSource != null) {
      this.router.navigate(getSavedSourceNavigation(savedSearchSource));
    }
  }

  publishedLink() {
    if (isSavedList(this.candidateSource)) {
      return externalDocLink(this.candidateSource);
    } else {
      return null;
    }
  }

  publishedDoc() {
    if (isSavedList(this.candidateSource)) {
      return this.candidateSource.publishedDocLink;
    } else {
      return null;
    }
  }

  truncatedSourceDescription(maxLen: number = 120):string {
    let s = this.candidateSource?.description;
    if (s && s.length > maxLen) {
      s = s.substring(0, maxLen) + "..."
    }
    return s;
  }

  isSavedList() {
    return isSavedList(this.candidateSource);
  }

  doShowListFolder() {
    if (isSavedList(this.candidateSource)) {
      const folderlink = this.candidateSource.folderlink;
      if (folderlink) {
        //Open link in new window
        window.open(folderlink, "_blank");
      } else {
        this.error = null;
        this.loading = true;
        this.savedListService.createFolder(this.candidateSource.id).subscribe(
          savedList => {
            this.candidateSource = savedList;
            this.loading = false;
            window.open(savedList.folderlink, "_blank");
          },
          error => {
            this.error = error;
            this.loading = false;
          });
      }
    }
  }

  canAccessSalesforce(): boolean {
    return this.authService.canAccessSalesforce();
  }

}
