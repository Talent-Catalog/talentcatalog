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
  AfterViewChecked,
  Component,
  EventEmitter,
  Input,
  OnInit,
  Output, SimpleChanges,
  ViewChild
} from '@angular/core';
import {Candidate} from '../../../model/candidate';
import {User} from '../../../model/user';
import {CandidateSource} from '../../../model/base';
import {isSavedSearch} from "../../../model/saved-search";
import {isSavedList} from "../../../model/saved-list";
import {NgbNav, NgbNavChangeEvent} from "@ng-bootstrap/ng-bootstrap";
import {LocalStorageService} from "angular-2-local-storage";
import {AuthorizationService} from "../../../services/authorization.service";
import {CandidateOpportunity} from "../../../model/candidate-opportunity";

@Component({
  selector: 'app-candidate-search-card',
  templateUrl: './candidate-search-card.component.html',
  styleUrls: ['./candidate-search-card.component.scss']
})
export class CandidateSearchCardComponent implements OnInit, AfterViewChecked {

  @Input() candidate: Candidate;
  @Input() loggedInUser: User;
  @Input() candidateSource: CandidateSource;
  @Input() sourceType: String;
  @Input() defaultSearch: boolean;
  @Input() savedSearchSelectionChange: boolean;

  @Output() closeEvent = new EventEmitter();
  @Output() onSearchCardRendered = new EventEmitter();

  showAttachments: boolean = false;
  showNotes: boolean = true;

  //Get reference to the nav element
  @ViewChild('nav')
  nav: NgbNav;
  activeTabId: string;
  private lastTabKey: string = 'SelectedCandidateLastTab';

  @ViewChild('navContext')
  navContext: NgbNav;
  activeContextTabId: string;
  private lastContextTabKey: string = 'SelectedCandidateContextLastTab';

  /**
   * Compliments ngAfterViewChecked, which runs multiple times with each change, by ensuring the
   * desired attendant methods are only run once per view change.
   */
  afterViewCheckedHasRun: boolean = false;

  constructor(private localStorageService: LocalStorageService,
              private authService: AuthorizationService) { }

  ngOnInit() {
  }

  ngAfterViewChecked(): void {
    if (!this.afterViewCheckedHasRun) {
      this.afterViewCheckedHasRun = false;
      // This is called in order for the navigation tabs, this.nav, to be set.
      this.selectDefaultTab();
      // Parent component has stored previous scroll position, will restore if pixels from top > 0.
      this.onSearchCardRendered.emit();
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    // Ensures that AfterViewChecked methods will run after new candidate received from parent.
    if (changes.candidate) this.afterViewCheckedHasRun = false;
  }

  close() {
    this.closeEvent.emit();
  }

  toggleNotes() {
    this.showNotes = !this.showNotes;
  }

  get isList() {
    return isSavedList(this.candidateSource);
  }

  get isCandidateSelected(): boolean {
    return this.candidate.selected;
  }

  isSubmissionList(): boolean {
    return this.isList && this.candidateSource.sfJobOpp != null
  }

  isContextNoteDisplayed() {
    let display: boolean = true;
    if (isSavedSearch(this.candidateSource)) {
      if (this.candidateSource.defaultSearch) {
        display = false;
      }
    }
    return display;
  }

  onTabChanged(event: NgbNavChangeEvent) {
    this.setActiveTabId(event.nextId);
  }

  onContextTabChanged(event: NgbNavChangeEvent) {
    this.setActiveContextTabId(event.nextId);
  }

  private setActiveTabId(id: string) {
    this.nav?.select(id);
    this.localStorageService.set(this.lastTabKey, id);
  }

  private setActiveContextTabId(id: string) {
    this.navContext?.select(id);
    this.localStorageService.set(this.lastContextTabKey, id);
  }

  private selectDefaultTab() {
    const defaultActiveTabID: string = this.localStorageService.get(this.lastTabKey);
    this.setActiveTabId(defaultActiveTabID == null ? "general" : defaultActiveTabID);

    let defaultActiveContextTabID: string = this.localStorageService.get(this.lastContextTabKey);

    // IF the saved context tab is 'progress' but the source isn't a submission list
    // THEN we need to set the saved context tab to null
    // SO THAT it selects the default 'context notes' tab otherwise it won't select anything
    // as the progress tab only exists on submission lists.
    if (defaultActiveContextTabID == "progress" && !this.isSubmissionList()) {
      defaultActiveContextTabID = null;
    }
    this.setActiveContextTabId(defaultActiveContextTabID == null ? "contextNotes" : defaultActiveContextTabID);
  }

  canViewPrivateInfo() {
    return this.authService.canViewPrivateCandidateInfo(this.candidate);
  }

  /**
   * Get candidate opportunity matching current job (if it is a job submission list)
   */
  getCandidateOppForJobSource(): CandidateOpportunity {
    return this.candidate.candidateOpportunities.find(o => o.jobOpp.id === this.candidateSource.sfJobOpp?.id);
  }

}
