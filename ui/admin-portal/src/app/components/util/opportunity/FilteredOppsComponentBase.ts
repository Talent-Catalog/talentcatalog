/*
 * Copyright (c) 2023 Talent Beyond Boundaries.
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
  Directive,
  ElementRef,
  EventEmitter,
  Inject,
  Input,
  LOCALE_ID,
  OnChanges,
  OnInit,
  Output,
  SimpleChanges,
  ViewChild
} from '@angular/core';
import {CandidateOpportunity, SearchOpportunityRequest} from "../../../model/candidate-opportunity";
import {SearchResults} from "../../../model/search-results";
import {FormBuilder, FormGroup} from "@angular/forms";
import {EnumOption} from "../../../util/enum";
import {AuthorizationService} from "../../../services/authorization.service";
import {LocalStorageService} from "angular-2-local-storage";
import {SalesforceService} from "../../../services/salesforce.service";
import {indexOfHasId, SearchOppsBy} from "../../../model/base";
import {
  getOpportunityStageName,
  Opportunity,
  OpportunityOwnershipType
} from "../../../model/opportunity";
import {debounceTime, distinctUntilChanged} from "rxjs/operators";
import {OpportunityService} from "./OpportunityService";
import {User} from "../../../model/user";
import {CountryService} from "../../../services/country.service";
import {Country} from "../../../model/country";
import {JobChatUserInfo} from "../../../model/chat";
import {BehaviorSubject} from "rxjs";

@Directive()
export abstract class FilteredOppsComponentBase<T extends Opportunity> implements OnInit, OnChanges {

  /**
   * Defines type of opportunity search.
   */
  @Input() searchBy: SearchOppsBy;

  /**
   * This is passed in from a higher level component which tracks whether the overall read status
   * of all the chats that it manages.
   * That component is the cases tab in the Jobs home component - which displays an asterisk
   * if some chats are unread.
   * <p/>
   * This component can call next on this subject if it knows that some of the chats it manages
   * are unread. The fact that it is a BehaviorSubject means that you can query the current status
   * of the higher level component.
   */
  @Input() chatsRead$: BehaviorSubject<boolean>;

  @Output() oppSelection = new EventEmitter();

  opps: T[];

  currentOpp: T;

  /*
   * These are default values which will normally be overridden in subclasses
   */
  myOppsOnlyLabel = "My opps only";
  myOppsOnlyTip = "Only show opps that I am the contact for";
  overdueOppsOnlyLabel = "Overdue next step opps only";
  overdueOppsOnlyTip = "Only show opps whose NextStep is overdue";
  showClosedOppsLabel = "Show closed opps";
  showClosedOppsTip = "Show opps that have been closed";
  showInactiveOppsLabel = "Show inactive opps";
  showInactiveOppsTip = "Show opps that are no longer active - for example if they have already relocated";

  loading: boolean;
  error;
  pageNumber: number;
  pageSize: number;

  results: SearchResults<T>;

  //Get reference to the search input filter element (see #searchFilter in html) so we can reset focus
  @ViewChild("searchFilter")
  searchFilter: ElementRef;

  searchForm: FormGroup;

  //Default sort opps in descending order of nextDueDate
  sortField = 'nextStepDueDate';
  sortDirection = 'DESC';

  stages: EnumOption[] = [];

  destinations: Country[] = [];

  private filterKeySuffix: string = 'Filter';
  private myOppsOnlySuffix: string = 'MyOppsOnly';
  private overdueOppsOnlySuffix: string = 'OverdueOppsOnly';
  private savedStateKeyPrefix: string = 'BrowseKey';
  private showClosedOppsSuffix: string = 'ShowClosedOpps';
  private showInactiveOppsSuffix: string = 'ShowInactiveOpps';
  private sortDirectionSuffix: string = 'SortDir';
  private sortFieldSuffix: string = 'Sort';

  constructor(
    private fb: FormBuilder,
    private authService: AuthorizationService,
    private localStorageService: LocalStorageService,
    protected oppService: OpportunityService<T>,
    private salesforceService: SalesforceService,
    protected countryService: CountryService,
    @Inject(LOCALE_ID) private locale: string,
    private stateKeysRoot: string
  ) {}

  ngOnInit(): void {

    //Pick up previous sort
    const previousSortDirection: string = this.localStorageService.get(this.savedStateKey() + this.sortDirectionSuffix);
    if (previousSortDirection) {
      this.sortDirection = previousSortDirection;
    }
    const previousSortField: string = this.localStorageService.get(this.savedStateKey() + this.sortFieldSuffix);
    if (previousSortField) {
      this.sortField = previousSortField;
    }

    this.stages = this.loadStages();

    this.countryService.listCountries().subscribe((destinations: Country[]): void => {
      this.destinations = destinations;
    });
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.searchBy) {
      this.initSearchBy()
    }
  }

  protected abstract loadStages(): EnumOption[];

  private initSearchBy() {
    this.pageNumber = 1;
    this.pageSize = 30;

    //Pick up any previous keyword filter
    const filter = this.localStorageService.get(this.savedStateKey() + this.filterKeySuffix);

    //Pick up previous options
    const previousMyOppsOnly: string = this.localStorageService.get(this.savedStateKey() + this.myOppsOnlySuffix);
    const previousOverdueOppsOnly: string = this.localStorageService.get(this.savedStateKey() + this.overdueOppsOnlySuffix);
    const previousShowClosedOpps: string = this.localStorageService.get(this.savedStateKey() + this.showClosedOppsSuffix);
    const previousShowInactiveOpps: string = this.localStorageService.get(this.savedStateKey() + this.showInactiveOppsSuffix);

    this.searchForm = this.fb.group({
      keyword: [filter],
      myOppsOnly: [previousMyOppsOnly ? previousMyOppsOnly : false],
      overdueOppsOnly: [previousOverdueOppsOnly ? previousOverdueOppsOnly : false],
      showClosedOpps: [previousShowClosedOpps ? previousShowClosedOpps : false],
      showInactiveOpps: [previousShowInactiveOpps ? previousShowInactiveOpps : false],
      selectedStages: [[]],
      destinationIds: []
    });

    this.subscribeToFilterChanges();

    this.search();
  }

  private get keyword(): string {
    return this.searchForm ? this.searchForm.value.keyword : "";
  }

  protected get showClosedOpps(): boolean {
    return this.searchForm ? this.searchForm.value.showClosedOpps : false;
  }

  protected get showInactiveOpps(): boolean {
    return this.searchForm ? this.searchForm.value.showInactiveOpps : false;
  }

  protected get myOppsOnly(): boolean {
    return this.searchForm ? this.searchForm.value.myOppsOnly : false;
  }

  protected get overdueOppsOnly(): boolean {
    return this.searchForm ? this.searchForm.value.overdueOppsOnly : false;
  }

  get SearchOppsBy() {
    return SearchOppsBy;
  }

  get selectedStages(): string[] {
    return this.searchForm ? this.searchForm.value.selectedStages : "";
  }

  get selectedDestinationIds(): number[] {
    return this.searchForm ? this.searchForm.value.destinationIds : null;
  }

  private savedStateKey(): string {
    //This key is constructed from the combination of inputs which are associated with each tab
    // in home.component.html
    //This key is used to store the last state associated with each tab.

    //The standard key is "BrowseKey" + stateKeysRoot  (eg "Jobs") +
    // the search by (corresponding to the specific displayed tab)
    let key = this.savedStateKeyPrefix
      + this.stateKeysRoot
      + SearchOppsBy[this.searchBy];

    return key
  }

  protected abstract createSearchRequest(): SearchOpportunityRequest;

  search() {
    //Remember keyword filter
    this.localStorageService.set(this.savedStateKey() + this.filterKeySuffix, this.keyword);

    //Remember sort
    this.localStorageService.set(this.savedStateKey()+this.sortFieldSuffix, this.sortField);
    this.localStorageService.set(this.savedStateKey()+this.sortDirectionSuffix, this.sortDirection);

    //Remember options
    this.localStorageService.set(this.savedStateKey()+this.myOppsOnlySuffix, this.myOppsOnly);
    this.localStorageService.set(this.savedStateKey()+this.overdueOppsOnlySuffix, this.overdueOppsOnly);
    this.localStorageService.set(this.savedStateKey()+this.showClosedOppsSuffix, this.showClosedOpps);
    this.localStorageService.set(this.savedStateKey()+this.showInactiveOppsSuffix, this.showInactiveOpps);

    let req = this.createSearchRequest();
    req.keyword = this.keyword;
    req.pageNumber = this.pageNumber - 1;
    req.pageSize = this.pageSize;

    req.sortFields = [this.sortField];
    req.sortDirection = this.sortDirection;

    req.stages = this.selectedStages;

    req.destinationIds = this.selectedDestinationIds;

    switch (this.searchBy) {
      case SearchOppsBy.live:

        //Don't want to see closed jobs
        req.sfOppClosed = false;
        break;

      case SearchOppsBy.mineAsSourcePartner:
        req.ownershipType = OpportunityOwnershipType.AS_SOURCE_PARTNER;
        this.populateRequestUsingContext(req);
        break;

      case SearchOppsBy.mineAsJobCreator:
        req.ownershipType = OpportunityOwnershipType.AS_JOB_CREATOR;
        this.populateRequestUsingContext(req);
        break;
    }

    this.error = null;
    this.loading = true;

    this.oppService.searchPaged(req).subscribe({
        next: results => this.processSearchResults(results),
        error: error => this.processSearchError(error)
      }
    )

    this.oppService.checkUnreadChats(req).subscribe({
        next: unreadChats => this.processChatsReadStatus(unreadChats),
        error: error => this.processSearchError(error)
      }
    )
  }

  private populateRequestUsingContext(req: SearchOpportunityRequest) {
    if (this.myOppsOnly) {
      req.ownedByMe = true;
    } else {
      req.ownedByMyPartner = true;
    }

    //Default - filters out closed opps and only includes active stages but not only overdue opps
    req.sfOppClosed = false;
    req.activeStages = true;
    req.overdue = false;

    if (this.showInactiveOpps) {
      //Turn off the active stages filter
      req.activeStages = false;
    }

    if (this.overdueOppsOnly) {
      //Only show overdue opps
      req.overdue = true;
    }

    if (this.showClosedOpps) {
      req.sfOppClosed = true;
    }
  }

  protected processChatsReadStatus(info: JobChatUserInfo) {
    if (this.chatsRead$) {
      //There is a high level component monitoring the read status of all chats.
      //Notify that component but sending the new read status on the Subject. Read is true
      //if there are no unread chats, otherwise false.
      this.chatsRead$.next(info.numberUnreadChats === 0);
    }
    this.loading = false;
  }

  protected processSearchError(error: any) {
    this.error = error;
    this.loading = false;
  }

  protected processSearchResults(results: SearchResults<T>) {
    this.results = results;
    this.loading = false;

    this.opps = results.content;

    if (this.opps.length > 0) {
      //Select previously selected item if still present in results
      const id: number = this.localStorageService.get(this.savedStateKey());
      if (id) {
        let currentIndex = indexOfHasId(id, this.opps);
        if (currentIndex >= 0) {
          this.selectCurrent(this.opps[currentIndex]);
        } else {
          this.selectCurrent(this.opps[0]);
        }
      } else {
        //Select the first search if no previous
        this.selectCurrent(this.results.content[0]);
      }
    }

    //Following the search filter loses focus, so focus back on it again
    setTimeout(()=>{this.searchFilter.nativeElement.focus()},0);
  }

  canAccessSalesforce(): boolean {
    return this.authService.canAccessSalesforce();
  }

  get getCandidateOpportunityStageName() {
    return getOpportunityStageName;
  }

  getOppSfLink(sfId: string): string {
    return this.salesforceService.sfOppToLink(sfId);
  }

  private subscribeToFilterChanges() {
    this.searchForm.valueChanges
    .pipe(
      debounceTime(1000),
      distinctUntilChanged()
    )
    .subscribe(() => {
      this.search();
    });
  }

  /**
   * Call when column title is clicked. If the column is the currently selected column,
   * the sort toggles between ASC and DESC.
   * If the column is not the currently selected column, the sort is set to the given
   * default.
   * @param column Name of column which was clicked
   * @param directionDefault Default sort direction for the clicked column
   */
  toggleSort(column: string, directionDefault = 'ASC') {
    if (this.sortField === column) {
      this.sortDirection = this.sortDirection === 'ASC' ? 'DESC' : 'ASC';
    } else {
      this.sortField = column;
      this.sortDirection = directionDefault;
    }

    if (this.searchBy) {
      this.search();
    }
  }

  selectCurrent(opp: T) {
    this.currentOpp = opp;

    const id: number = opp.id;
    this.localStorageService.set(this.savedStateKey(), id);

    this.oppSelection.emit(opp);

  }

  fullUserName(user: User) {
    return user ? user.firstName + " " + user.lastName : "";
  }

  getNextStepHoverString(opp: CandidateOpportunity) {
    return (opp.nextStep ? opp.nextStep : '');
  }
}
