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

import {
  AfterViewChecked,
  Component,
  EventEmitter,
  Input,
  OnDestroy,
  OnInit,
  Output,
  SimpleChanges
} from '@angular/core';
import {Candidate} from '../../../model/candidate';
import {User} from '../../../model/user';
import {CandidateSource} from '../../../model/base';
import {isSavedSearch} from "../../../model/saved-search";
import {isSavedList} from "../../../model/saved-list";
import {AuthorizationService} from "../../../services/authorization.service";
import {CandidateOpportunity} from "../../../model/candidate-opportunity";
import {LocalStorageService} from "../../../services/local-storage.service";
import {CandidateService} from "../../../services/candidate.service";
import {takeUntil} from "rxjs/operators";
import {Subject} from "rxjs";

@Component({
  selector: 'app-candidate-search-card',
  templateUrl: './candidate-search-card.component.html',
  styleUrls: ['./candidate-search-card.component.scss']
})
export class CandidateSearchCardComponent implements OnInit, OnDestroy, AfterViewChecked {

  @Input() candidate: Candidate;
  @Input() loggedInUser: User;
  @Input() candidateSource: CandidateSource;
  @Input() sourceType: String;
  @Input() defaultSearch: boolean;
  @Input() savedSearchSelectionChange: boolean;
  @Input() isKeywordSearch: boolean;

  @Output() closeEvent = new EventEmitter();
  @Output() onSearchCardRendered = new EventEmitter();
  @Output() candidateUpdated = new EventEmitter<Candidate>();

  private destroy$ = new Subject<void>();

  showAttachments: boolean = false;
  showNotes: boolean = true;

  activeTabId: string;
  private lastTabKey: string = 'SelectedCandidateLastTab';

  activeContextTabId: string;
  private lastContextTabKey: string = 'SelectedCandidateContextLastTab';

  /**
   * Compliments ngAfterViewChecked, which runs multiple times with each change, by ensuring the
   * desired attendant methods are only run once per content change.
   */
  afterViewCheckedHasRun: boolean = false;

  constructor(private localStorageService: LocalStorageService,
              private authorizationService: AuthorizationService,
              private candidateService: CandidateService) { }

  ngOnInit() {
    // The only things that can be updated via the search card is the context notes, general notes and selected shareable
    // attachments. The context notes and general notes are updated via a parent/child object relationship. So updates
    // to these fields are sent to the parent just by updating the object in the child. Selected shareable attachments
    // are handled differently as shareable attachments are nested deeper and not parent/child.
    // Using the candidateUpdated observable is best for this case.
    this.candidateService.candidateUpdated().pipe(takeUntil(this.destroy$)).subscribe(updatedCandidate => {
      // To avoid an API call to fetch the updated candidate object we can use the existing extended candidate object
      // and the Spread operator to merge in the updated candidate object from the observable. See doc about Spread:
      // https://www.typescriptlang.org/docs/handbook/variable-declarations.html#spread
      // We just need to handle null fields, as fields that are changed to null in an update aren't returned in the DTO
      // so there is nothing to merge in to replace the old value. However the only place this observable is being
      // called for the search card is in the selected shareable attachments. So we can handle the case of null fields
      // manually in the shareable-docs component.
      // If we want to allow more updates to the candidate object via the search card we may need to look at handling
      // updates using an API call to fetch the updated object like we do in the view-candidate component use of this observable.
      this.candidate = {...this.candidate, ...updatedCandidate};
      // Emit the updated candidate object so that it can be inserted into the page results.
      this.candidateUpdated.emit(this.candidate);
    })
  }

  ngAfterViewChecked(): void {
    if (!this.afterViewCheckedHasRun) { // Don't execute if already run
      this.afterViewCheckedHasRun = true; // Prohibit further execution
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

  isEditable(): boolean {
    return this.authorizationService.isEditableCandidate(this.candidate);
  }

  onTabChanged(tabId: string) {
    this.setActiveTabId(tabId);
  }

  onContextTabChanged(contextTabId: string) {
    this.setActiveContextTabId(contextTabId);
  }

  private setActiveTabId(id: string) {
    this.localStorageService.set(this.lastTabKey, id);
  }

  private setActiveContextTabId(id: string) {
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
    return this.authorizationService.canViewPrivateCandidateInfo(this.candidate);
  }

  /**
   * Get candidate opportunity matching current job (if it is a job submission list)
   */
  getCandidateOppForJobSource(): CandidateOpportunity {
    return this.candidate.candidateOpportunities.find(o => o.jobOpp.id === this.candidateSource.sfJobOpp?.id);
  }

  isAnAdmin(): boolean {
    return this.authorizationService.isAnAdmin();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  public canSeeJobDetails() {
    return this.authorizationService.canSeeJobDetails()
  }

}
