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
  Directive,
  ElementRef,
  EventEmitter,
  Inject,
  Input,
  LOCALE_ID,
  OnChanges,
  OnDestroy,
  OnInit,
  Output,
  SimpleChanges
} from '@angular/core';
import {CandidateOpportunity, SearchOpportunityRequest} from "../../../model/candidate-opportunity";
import {SearchResults} from "../../../model/search-results";
import {UntypedFormBuilder, UntypedFormGroup} from "@angular/forms";
import {EnumOption} from "../../../util/enum";
import {AuthorizationService} from "../../../services/authorization.service";
import {SalesforceService} from "../../../services/salesforce.service";
import {indexOfHasId, SearchOppsBy} from "../../../model/base";
import {
  getOpportunityStageName,
  getStageBadgeColor,
  Opportunity,
  OpportunityOwnershipType
} from "../../../model/opportunity";
import {debounceTime, distinctUntilChanged, takeUntil} from "rxjs/operators";
import {OpportunityService} from "./OpportunityService";
import {User} from "../../../model/user";
import {CountryService} from "../../../services/country.service";
import {Country} from "../../../model/country";
import {JobChat, JobChatUserInfo} from "../../../model/chat";
import {BehaviorSubject, Subject, Subscription} from "rxjs";
import {ChatService} from "../../../services/chat.service";
import {PartnerService} from "../../../services/partner.service";
import {Partner} from "../../../model/partner";
import {LocalStorageService} from "../../../services/local-storage.service";

@Directive()
export abstract class FilteredOppsComponentBase<T extends Opportunity> implements OnInit, OnChanges, OnDestroy {

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

  /**
   * All chats associated with all opps. Used to construct overall chat read notifier.
   */
  protected allChats: JobChat[] = [];

  /**
   * Map of opp id to opp chats
   */
  protected oppChats: Map<number, JobChat[]> = new Map<number, JobChat[]>();

  /**
   * Subscription to all visible opps chats
   * @private
   */
  protected subscription: Subscription;

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
  withUnreadMessagesLabel = "Opps with unread chat messages only";
  withUnreadMessagesTip = "Only show opps which have unread chat messages";

  loading: boolean;
  error: string;
  pageNumber: number;
  pageSize: number;

  results: SearchResults<T>;

  //Get reference to the search input filter element (see #searchFilter in html) so we can reset focus
  protected searchFilter: ElementRef;

  searchForm: UntypedFormGroup;

  //Default sort opps in descending order of nextDueDate
  sortField = 'nextStepDueDate';
  sortDirection = 'DESC';

  stages: EnumOption[] = [];

  destinations: Country[] = [];
  sourcePartners: Partner[] = [];

  private filterKeySuffix: string = 'Filter';
  private myOppsOnlySuffix: string = 'MyOppsOnly';
  private overdueOppsOnlySuffix: string = 'OverdueOppsOnly';
  private savedStateKeyPrefix: string = 'BrowseKey';
  private showClosedOppsSuffix: string = 'ShowClosedOpps';
  private showInactiveOppsSuffix: string = 'ShowInactiveOpps';
  private showUnpublishedOppsSuffix: string = 'ShowUnpublishedOpps';
  private sortDirectionSuffix: string = 'SortDir';
  private sortFieldSuffix: string = 'Sort';
  private withUnreadMessagesSuffix: string = 'WithUnreadMessages';

  private destroy$ = new Subject<void>();

  protected constructor(
    protected chatService: ChatService,
    private fb: UntypedFormBuilder,
    protected authorizationService: AuthorizationService,
    private localStorageService: LocalStorageService,
    protected oppService: OpportunityService<T>,
    private salesforceService: SalesforceService,
    protected countryService: CountryService,
    private partnerService: PartnerService,
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

    this.partnerService.listSourcePartners().subscribe(
      (sourcePartners) => {
        this.sourcePartners = sourcePartners;
      });

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
    const previousWithUnreadMessages: string = this.localStorageService.get(this.savedStateKey() + this.withUnreadMessagesSuffix);
    const previousShowClosedOpps: string = this.localStorageService.get(this.savedStateKey() + this.showClosedOppsSuffix);
    const previousShowInactiveOpps: string = this.localStorageService.get(this.savedStateKey() + this.showInactiveOppsSuffix);
    const previousShowUnpublishedOpps: string = this.localStorageService.get(this.savedStateKey() + this.showUnpublishedOppsSuffix);

    this.searchForm = this.fb.group({
      destinationIds: [],
      keyword: [filter],
      myOppsOnly: [previousMyOppsOnly ? previousMyOppsOnly : false],
      overdueOppsOnly: [previousOverdueOppsOnly ? previousOverdueOppsOnly : false],
      selectedStages: [[]],
      showClosedOpps: [previousShowClosedOpps ? previousShowClosedOpps : false],
      showInactiveOpps: [previousShowInactiveOpps ? previousShowInactiveOpps : false],
      showUnpublishedOpps: [previousShowUnpublishedOpps ? previousShowUnpublishedOpps : false],
      withUnreadMessages: [previousWithUnreadMessages ? previousWithUnreadMessages : false]
    });

    this.subscribeToFilterChanges();

    this.subscribeToStagesChanges();

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

  protected get showUnpublishedOpps(): boolean {
    return this.searchForm ? this.searchForm.value.showUnpublishedOpps : false;
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

  protected get withUnreadMessages(): boolean {
    return this.searchForm ? this.searchForm.value.withUnreadMessages : false;
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

  refresh(event: any): void {
    this.search();
    event.preventDefault(); // Stops form from submitting and search being called twice on click
  }

  /**
   * This executes a search based on the current form fields by default - unless runSearch = false.
   * <p/>
   * If runSearch is false, it just uses the search fields to count the number of unwatched chats
   * associated with the opps specified by the search fields.
   * @param fetchOpps True (default) if actual new opps matching the search fields should be
   * fetched from the server.
   */
  search(fetchOpps: boolean = true) {
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
    this.localStorageService.set(this.savedStateKey()+this.showUnpublishedOppsSuffix, this.showUnpublishedOpps);
    this.localStorageService.set(this.savedStateKey()+this.withUnreadMessagesSuffix, this.withUnreadMessages);

    let req = this.createSearchRequest();
    req.keyword = this.keyword;
    req.pageNumber = this.pageNumber - 1;
    req.pageSize = this.pageSize;

    req.sortFields = [this.sortField];
    req.sortDirection = this.sortDirection;

    req.withUnreadMessages = this.withUnreadMessages;

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

    if (fetchOpps) {
      this.oppService.searchPaged(req).subscribe({
          next: results => this.processSearchResults(results),
          error: error => this.processSearchError(error)
        }
      )
    }

    this.oppService.checkUnreadChats(req).subscribe({
        next: info => this.processChatsReadStatus(info),
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

    if (this.showUnpublishedOpps) {
      //Normally we don't care about published. Published jobs will be displayed or not
      //based on the activeStages or oppClosed filters.
      //However, an owner may want to force unpublished opps to display so that they can see
      //if there are any that they still need to publish.
      req.published = false;
    }

    if (this.overdueOppsOnly) {
      //Only show overdue opps
      req.overdue = true;
    }

    if (this.showClosedOpps) {
      req.sfOppClosed = true;
    }

    if (this.withUnreadMessages) {
      //Only show opps with unread chat messages
      req.withUnreadMessages = true;
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
    return this.authorizationService.canAccessSalesforce();
  }

  get getCandidateOpportunityStageName() {
    return getOpportunityStageName;
  }

  get getBadgeColor() {
    return getStageBadgeColor;
  }

  getChats(opp: Opportunity): JobChat[] {
    return opp ? this.oppChats.get(opp.id) : null;
  }

  getOppSfLink(sfId: string): string {
    return this.salesforceService.sfOppToLink(sfId);
  }

  private subscribeToFilterChanges() {
    this.searchForm.valueChanges
    .pipe(
      debounceTime(1000),
      distinctUntilChanged(),
      takeUntil(this.destroy$)
    )
    .subscribe(() => {
      this.search();
    });
  }

  /**
   * Disable/enable the checkboxes depending on if there are stages selected.
   * We don't search by these fields AND a stage, so want to disable (and set to false)
   * when a stage is added. Re-enable once the stages are removed.
   */
  private subscribeToStagesChanges() {
    this.searchForm.get('selectedStages').valueChanges
      .pipe(takeUntil(this.destroy$))
      .subscribe((stages) => {
          if (stages.length > 0) {
            this.searchForm.get('showClosedOpps').reset({value: false, disabled: true});
            this.searchForm.get('showInactiveOpps').reset({value: false, disabled: true});
            this.searchForm.get('showUnpublishedOpps').reset({value: false, disabled: true});
            this.searchForm.get('overdueOppsOnly').reset({value: false, disabled: true});
            this.searchForm.get('withUnreadMessages').reset({value: false, disabled: true});
          } else {
            this.searchForm.get('showClosedOpps').enable()
            this.searchForm.get('showInactiveOpps').enable()
            this.searchForm.get('showUnpublishedOpps').enable()
            this.searchForm.get('overdueOppsOnly').enable()
            this.searchForm.get('withUnreadMessages').enable()
          }
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

  /**
   * This stores the chats for each opp in this.oppChats, indexed by the opp id.
   * This can be accessed by {@link getChats}.
   * <p/>
   * It also puts chats for all opps into this.allChats.
   * @param chatsByOpp Array of chats for each opp
   */
  protected processOppChats(chatsByOpp: JobChat[][]) {
    //Recalculate all chats for new opps
    this.allChats = [];
    for (let i = 0; i < this.opps.length; i++) {
      const opp = this.opps[i];
      let chats = chatsByOpp[i];
      this.oppChats.set(opp.id, chats);

      for (const jobChat of chats) {
        this.allChats.push(jobChat);
      }
    }

    //Resubscribe to composite status of all visible chats
    this.subscribeToAllVisibleChats();
  }

  private subscribeToAllVisibleChats() {
    this.unsubscribe();
    //Construct a single observable for all visible chat's read statuses, and subscribe to it
    const chatReadStatus$ =
      this.chatService.combineChatReadStatuses(this.allChats)
      .pipe(distinctUntilChanged());
    console.log("Subscribed to chats " + this.allChats.map( chat => chat.id).join(','));
    this.subscription = chatReadStatus$.subscribe(
      {
        next: chatsRead => this.processVisibleChatsReadUpdate(chatsRead),
        error: err => this.error = err
      }
    )
  }

  private processVisibleChatsReadUpdate(chatsRead: boolean) {
    if (this.chatsRead$) {
      console.log("Visible chats read update: " + chatsRead);
      if (this.chatsRead$.value && !chatsRead) {
        //Status from server says all chats read, but there are unread visible chats.
        //Mark all chats read false
        this.chatsRead$.next(false);
      } else if (!this.chatsRead$.value && chatsRead) {
        //All chats are showing not read, but all chats for visible opps are now read.
        //Fetch from server again to see if there are still some non-visible opps with unread chats.
        //Don't redo the search - we just want to see if there are any unread chats left in the full
        //search results.
        this.search(false);
      }
    }
  }

  private unsubscribe() {
    if (this.subscription) {
      console.log("Unsubscribed from previous visible chats")
      this.subscription.unsubscribe();
      this.subscription = null;
    }
  }

  // These changes ensure subscriptions are torn down whenever the tab content is destroyed, preventing multiple overlapping subscriptions and eliminating the repeated console logs.
  ngOnDestroy(): void {
    // Existing: release combined chat subscription
    this.unsubscribe();

    // New: terminate form subscriptions
    this.destroy$.next();
    this.destroy$.complete();
  }

}
