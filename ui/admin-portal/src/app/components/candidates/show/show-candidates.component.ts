import {
  Component,
  EventEmitter,
  Input,
  OnChanges,
  OnDestroy,
  OnInit,
  Output,
  SimpleChanges,
  ViewChild
} from '@angular/core';

import {Candidate} from '../../../model/candidate';
import {CandidateService} from '../../../services/candidate.service';
import {SearchResults} from '../../../model/search-results';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {
  CreateFromDefaultSavedSearchRequest,
  SavedSearchService
} from '../../../services/saved-search.service';
import {Observable, of, Subscription} from 'rxjs';
import {CandidateReviewStatusItem} from '../../../model/candidate-review-status-item';
import {HttpClient} from '@angular/common/http';
import {
  ClearSelectionRequest,
  getCandidateSourceBreadcrumb,
  getCandidateSourceExternalHref,
  getCandidateSourceNavigation,
  getSavedSearchBreadcrumb,
  getSavedSourceNavigation,
  isSavedSearch,
  SavedSearch,
  SavedSearchGetRequest,
  SavedSearchRef,
  SaveSelectionRequest,
  SearchCandidateRequestPaged,
  SelectCandidateInSearchRequest
} from '../../../model/saved-search';
import {
  CandidateSource,
  canEditSource,
  defaultReviewStatusFilter,
  isMine,
  isSharedWithMe,
  ReviewedStatus
} from '../../../model/base';
import {
  CachedSourceResults,
  CandidateSourceResultsCacheService
} from '../../../services/candidate-source-results-cache.service';
import {FormBuilder, FormGroup} from '@angular/forms';
import {IDropdownSettings} from 'ng-multiselect-dropdown';
import {User} from '../../../model/user';
import {AuthService} from '../../../services/auth.service';
import {UserService} from '../../../services/user.service';
import {
  SelectListComponent,
  TargetListSelection
} from '../../list/select/select-list.component';
import {
  CreateSavedListRequest,
  IHasSetOfCandidates,
  isSavedList,
  SavedListGetRequest
} from '../../../model/saved-list';
import {CandidateSourceCandidateService} from '../../../services/candidate-source-candidate.service';
import {LocalStorageService} from 'angular-2-local-storage';
import {EditCandidateReviewStatusItemComponent} from '../../util/candidate-review/edit/edit-candidate-review-status-item.component';
import {Router} from '@angular/router';
import {CandidateSourceService} from '../../../services/candidate-source.service';
import {SavedListCandidateService} from '../../../services/saved-list-candidate.service';
import {
  catchError,
  debounceTime,
  distinctUntilChanged,
  map,
  switchMap,
  tap
} from 'rxjs/operators';
import {Location} from '@angular/common';
import {copyToClipboard} from '../../../util/clipboard';
import {SavedListService} from '../../../services/saved-list.service';
import {ConfirmationComponent} from "../../util/confirm/confirmation.component";
import {CandidateColumnSelectorComponent} from "../../util/candidate-column-selector/candidate-column-selector.component";
import {CandidateFieldInfo} from "../../../model/candidate-field-info";
import {CandidateFieldService} from "../../../services/candidate-field.service";

interface CachedTargetList {
  sourceID: number;
  listID: number;
  name: string;
  replace: boolean
}

@Component({
  selector: 'app-show-candidates',
  templateUrl: './show-candidates.component.html',
  styleUrls: ['./show-candidates.component.scss']
})
export class ShowCandidatesComponent implements OnInit, OnChanges, OnDestroy {

  @ViewChild('downloadCsvErrorModal', {static: true}) downloadCsvErrorModal;

  @Input() candidateSource: CandidateSource;
  @Input() manageScreenSplits: boolean = true;
  @Input() showBreadcrumb: boolean = true;
  @Input() pageNumber: number;
  @Input() pageSize: number;
  @Input() searchRequest: SearchCandidateRequestPaged;
  @Output() candidateSelection = new EventEmitter();
  @Output() editSource = new EventEmitter();

  selectedFields: CandidateFieldInfo[] = [];


  error: any;
  loading: boolean;
  searching: boolean;
  exporting: boolean;
  updating: boolean;
  savingSelection: boolean;
  searchForm: FormGroup;

  results: SearchResults<Candidate>;
  subscription: Subscription;
  sortField = 'id';
  sortDirection = 'DESC';

  /* Add candidates support */
  doNumberOrNameSearch;
  searchFailed: boolean;


  /* MULTI SELECT */
  statuses: string[];
  dropdownSettings: IDropdownSettings = {
    idField: 'id',
    textField: 'text',
    singleSelection: false,
    selectAllText: 'Select All',
    unSelectAllText: 'Deselect All',
    itemsShowLimit: 3,
    closeDropDownOnSelection: true,
    allowSearchFilter: true
  };

  selectedCandidate: Candidate;
  selectedListCandidates: number[];
  loggedInUser: User;
  targetListName: string;
  targetListId: number;
  targetListReplace: boolean;
  timestamp: number;
  private reviewStatusFilter: string[] = defaultReviewStatusFilter;
  savedSearchSelectionChange: boolean;


  constructor(private http: HttpClient,
              private fb: FormBuilder,
              private candidateService: CandidateService,
              private candidateSourceService: CandidateSourceService,
              private candidateSourceCandidateService: CandidateSourceCandidateService,
              private userService: UserService,
              private savedSearchService: SavedSearchService,
              private savedListCandidateService: SavedListCandidateService,
              private savedListService: SavedListService,
              private modalService: NgbModal,
              private localStorageService: LocalStorageService,
              private location: Location,
              private router: Router,
              private candidateSourceResultsCacheService: CandidateSourceResultsCacheService,
              private candidateFieldService: CandidateFieldService,
              private authService: AuthService

  ) {
    this.searchForm = this.fb.group({
      statusesDisplay: [defaultReviewStatusFilter],
    });
  }

  ngOnInit() {
    this.selectedFields = this.candidateFieldService.defaultDisplayableFields;

    this.setSelectedCandidate(null);
    this.loggedInUser = this.authService.getLoggedInUser();
    this.selectedListCandidates = [];

    this.statuses = [];
    for (const key in ReviewedStatus) {
      if (isNaN(Number(key))) {
        this.statuses.push(key);
      }
    }

    this.doNumberOrNameSearch = (text$: Observable<string>) =>
      text$.pipe(
        debounceTime(300),
        distinctUntilChanged(),
        tap(() => {
          this.error = null
        }),
        switchMap(candidateNumberOrName =>
          this.candidateService.findByCandidateNumberOrName(
            {candidateNumberOrName: candidateNumberOrName, pageSize: 10}).pipe(
            tap(() => this.searchFailed = false),
            map(result => result.content),
            catchError(() => {
              this.searchFailed = true;
              return of([]);
            }))
        )
      );

  }

  get pluralType() {
     return isSavedSearch(this.candidateSource) ? "searches" : "lists";
  }

  ngOnChanges(changes: SimpleChanges): void {
    //If we get both a source change and a request change, do the source
    //change in preference to the request change because the source change
    //in that case will be a saved search and it will load a new search request
    //anyway - being the search request associated with the saved search.

    if (changes.candidateSource) {
      if (changes.candidateSource.previousValue !== changes.candidateSource.currentValue) {
        if (this.candidateSource) {
          //Retrieve the list previously used for saving selections from this
          // source (if any)
          this.restoreTargetListFromCache();
          this.doSearch(true);
          // Set the selected candidates (List only) to null when changing candidate source.
          this.selectedListCandidates = [];

        }
      }
    } else {
      if (changes.searchRequest) {
        if (changes.searchRequest.previousValue !== changes.searchRequest.currentValue) {
          if (this.searchRequest) {
            this.updatedSearch();
          }
        }
      }
    }
  }

  ngOnDestroy(): void {
    if (this.subscription) {
      this.subscription.unsubscribe();
    }
  }

  private updatedSearch() {
    this.results = null;
    this.error = null;
    this.searching = true;
    const request = this.searchRequest;
    request.pageNumber = this.pageNumber - 1;
    request.pageSize = this.pageSize;
    request.sortFields = [this.sortField];
    request.sortDirection = this.sortDirection;

    this.subscription = this.candidateService.search(request).subscribe(
      results => {
        this.results = results;
        this.cacheResults();
        this.searching = false;
      },
      error => {
        this.error = error;
        this.searching = false;
      });
  }

  doSearch(refresh: boolean, usePageNumber = true) {

    this.results = null;
    this.error = null;

    let done: boolean = false;

    if (!refresh) {

      //Is there anything in cache?
      //We only cache certain results
      if (this.isCacheable()) {
        const cached: CachedSourceResults =
          this.candidateSourceResultsCacheService.getFromCache(this.candidateSource);
        if (cached) {
          //If we are not required to use the pageNumber (usePageNumber = false)
          //we can take the pageNumber of whatever the cache has.
          //If we have to use the page number, the pageNumber and size must match
          //what is in the cache or we can't use it.
          done = !usePageNumber ||
            (cached.pageNumber === this.pageNumber && cached.pageSize === this.pageSize);
          if (done) {
            this.results = cached.results;
            this.sortField = cached.sortFields[0];
            this.sortDirection = cached.sortDirection;
            this.reviewStatusFilter = defaultReviewStatusFilter;
            this.timestamp = cached.timestamp;
            this.pageNumber = cached.pageNumber;
            this.pageSize = cached.pageSize;
          }
        }
      }
    }

    if (!done) {
      /*
       * Fetch results from the server rather than from any local cache.
       * <p/>
       * This component is used in two ways:
       * - To display saved lists
       * - To display saved searches.
       * This affects the way to a refresh is done.
       *
       * For saved lists, it is simply going to the server requesting the requested
       * page of the list.
       *
       * For saved searches, it is made more complicated because saved searches
       * can be displayed with details of the exact search, which are available
       * for the user to modify if they wish - thus changing the search.
       * But those changes are only stored on the server when the user clicks on
       * Update Search.
       * So, at any time there are potentially two different searches:
       * - the one stored on the server
       * - the modified one on the browser
       *
       * The results displayed to the user looking at a saved search are those
       * which incorporate their local browser changes
       *
       * So... when we want to refresh a saved search, we don't want to use the
       * version stored on the server. But with a saved list we do.
       */

      //If we are being driven by a manually modifiable search request, submit
      //that search.
      if (this.searchRequest) {

        this.updatedSearch()

      } else {

        //Run the saved list or saved search as stored on the server.

        this.searching = true;

        //Create the appropriate request
        let request;
        if (isSavedSearch(this.candidateSource)) {
          request = new SavedSearchGetRequest();
        } else {
          request = new SavedListGetRequest();
        }
        request.pageNumber = this.pageNumber - 1;
        request.pageSize = this.pageSize;
        request.sortFields = [this.sortField];
        request.sortDirection = this.sortDirection;
        if (request instanceof SavedSearchGetRequest) {
          request.reviewStatusFilter = this.reviewStatusFilter;
        }

        this.candidateSourceCandidateService.searchPaged(
          this.candidateSource, request).subscribe(
          results => {
            this.results = results;
            this.cacheResults();

            this.searching = false;
          },
          error => {
            this.error = error;
            this.searching = false;
          });
      }
    }
  }

  private cacheResults() {
    this.timestamp = Date.now();

    //We only cache certain results
    if (this.isCacheable()) {
      this.candidateSourceResultsCacheService.cache(this.candidateSource,
        {
        id: this.candidateSource.id,
        pageNumber: this.pageNumber,
        pageSize: this.pageSize,
        sortFields: [this.sortField],
        sortDirection: this.sortDirection,
        results: this.results,
        timestamp: this.timestamp
      });
    }
  }

  private isCacheable(): boolean {
    return !this.isReviewable() ||
      this.reviewStatusFilter.toString() === defaultReviewStatusFilter.toString();
  }

  setSelectedCandidate(candidate: Candidate) {
    this.selectedCandidate = candidate;
    if (candidate && isSavedSearch(this.candidateSource)) {
      this.savedSearchSelectionChange = candidate.selected;
    }
    this.candidateSelection.emit(candidate);
  }

  viewCandidate(candidate: Candidate) {
    this.setSelectedCandidate(candidate);
  }

  onReviewStatusChange() {
    this.doSearch(true);
  }

  toggleSort(column) {
    if (this.sortField === column) {
      this.sortDirection = this.sortDirection === 'ASC' ? 'DESC' : 'ASC';
    } else {
      this.sortField = column;
      this.sortDirection = 'ASC';
    }
    this.doSearch(true);
  }

  exportCandidates() {
    this.exporting = true;

    //Create the appropriate request
    let request;
    if (isSavedSearch(this.candidateSource)) {
      request = new SavedSearchGetRequest();
    } else {
      request = new SavedListGetRequest();
    }
    request.pageNumber = this.pageNumber - 1;
    request.pageSize = this.pageSize;
    request.sortFields = [this.sortField];
    request.sortDirection = this.sortDirection;
    if (request instanceof SavedSearchGetRequest) {
      request.reviewStatusFilter = this.reviewStatusFilter;
    }

    this.candidateSourceCandidateService.export(
      this.candidateSource, request).subscribe(
      result => {
        const options = {type: 'text/csv;charset=utf-8;'};
        const filename = 'candidates.csv';
        this.createAndDownloadBlobFile(result, options, filename);
        this.exporting = false;
      },
      err => {
        const reader = new FileReader();
        const _this = this;
        reader.addEventListener('loadend', function () {
          if (typeof reader.result === 'string') {
            _this.error = JSON.parse(reader.result);
            const modalRef = _this.modalService.open(_this.downloadCsvErrorModal);
            modalRef.result
              .then(() => {
              })
              .catch(() => {
              });
          }
        });
        reader.readAsText(err.error);
        this.exporting = false;
      }
    );
  }

  createAndDownloadBlobFile(body, options, filename) {
    const blob = new Blob([body], options);
    if (navigator.msSaveBlob) {
      // IE 10+
      navigator.msSaveBlob(blob, filename);
    } else {
      const link = document.createElement('a');
      // Browsers that support HTML5 download attribute
      if (link.download !== undefined) {
        const url = URL.createObjectURL(blob);
        link.setAttribute('href', url);
        link.setAttribute('download', filename);
        link.style.visibility = 'hidden';
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
      }
    }
  }

  downloadCv(candidate) {
    const tab = window.open();
    this.candidateService.downloadCv(candidate.id).subscribe(
      result => {
        const fileUrl = URL.createObjectURL(result);
        tab.location.href = fileUrl;
      },
      error => {
          this.error = error;
      }
    )
  }

  getBreadcrumb(): string {
    let breadcrumb: string;
    if (isSavedSearch(this.candidateSource)) {
      const infos = this.savedSearchService.getSavedSearchTypeInfos();
      breadcrumb = getSavedSearchBreadcrumb(this.candidateSource, infos);
    } else {
      breadcrumb = getCandidateSourceBreadcrumb(this.candidateSource);
    }
    return breadcrumb;
  }

  private onReviewStatusFilterChange() {

    this.reviewStatusFilter = this.searchForm.value.statusesDisplay;

    //We can ignore page number because changing the reviewStatus filter will
    //completely change the number of results.
    //Ignoring the page number will allow the cache to supply pageNumber
    //if it has something cached.
    this.doSearch(false, false);
  }

  onItemSelect() {
    this.onReviewStatusFilterChange();
  }

  onItemDeSelect() {
    this.onReviewStatusFilterChange();
  }

  onSelectAll() {
    this.onReviewStatusFilterChange();
  }

  onDeSelectAll() {
    this.onReviewStatusFilterChange();
  }

  addToSharedWithMe() {
    this.candidateSourceService.addSharedUser(
      this.candidateSource, {userId: this.loggedInUser.id}).subscribe(
      result => {
        this.candidateSource = result;
      },
      error => {
        this.error = error;
      }
    );
  }

  removeFromSharedWithMe() {
    this.candidateSourceService.removeSharedUser(
      this.candidateSource, {userId: this.loggedInUser.id}).subscribe(
      result => {
        this.candidateSource = result;
      },
      error => {
        this.error = error;
      }
    );
  }

  haveTargetList(): boolean {
    return this.targetListName && this.targetListName.length > 0;
  }

  isContentModifiable(): boolean {
    return !isSavedSearch(this.candidateSource);
  }

  isReviewable(): boolean {
    return isSavedSearch(this.candidateSource)
      ? this.candidateSource.reviewable : false;
  }

  isSalesforceUpdatable(): boolean {
    return !isSavedSearch(this.candidateSource);
  }

  sourceType(): string {
    return isSavedSearch(this.candidateSource) ? 'savedSearch' : 'list';
  }

  isShareable(): boolean {
    let shareable: boolean = false;

    //Is shareable with me if it is not fixed or created by me.
    if (this.candidateSource) {
        //was it created by me?
        if (!isMine(this.candidateSource, this.authService)) {
          shareable = true;
        }
    }
    return shareable;
  }

  isSharedWithMe(): boolean {
    return isSharedWithMe(this.candidateSource, this.authService);
  }

  isEditable(): boolean {
    return canEditSource(this.candidateSource, this.authService);
  }

  onSelectionChange(candidate: Candidate, selected: boolean) {

    //Record change
    candidate.selected = selected;
    //Update cache
    this.cacheResults();

    if (isSavedSearch(this.candidateSource)) {
      //Saved search selection change

      if (candidate.contextNote && !selected) {
        //They have a context note which they will lose if they deselect.
        //Ask for confirmation.
        const confirmation = this.modalService.open(ConfirmationComponent, {
          centered: true,
          backdrop: 'static'
        });
        confirmation.componentInstance.message =
          'You will lose the context note for ' + candidate.user.firstName +
          ' if you deselect this. Are you sure?';
        confirmation.result
          .then((confirmed: boolean) => {
            if (confirmed) {
              //Clear local copy of note
              //Note that this does not trigger an update through to the
              //contextNote component.
              //That is why we use the savedSearchSelectionChange variable
              //which is passed down as an input to the contextNote component
              //and can trigger an action which updates the local contextNote
              //form field.
              candidate.contextNote = null;
              this.savedSearchSelectionChange = false;
              this.doSavedSearchSelection(candidate, selected);
            } else {
              //Unconfirmed, reinstate as selected
              candidate.selected = true;
            }
          })
          .catch(() => {
            //Unconfirmed, reinstate as selected
            candidate.selected = true;
          });
      } else {
        this.savedSearchSelectionChange = selected;
        this.doSavedSearchSelection(candidate, selected);
      }
    } else {
      // If selections coming from a list, create a list of the candidateIds selected
      if (selected) {
        this.selectedListCandidates.push(candidate.id);
      } else {
        this.selectedListCandidates = this.selectedListCandidates.filter(id => id !== candidate.id);
      }
    }
  }

  private doSavedSearchSelection(candidate: Candidate, selected: boolean) {
    //Record change on server
    //Candidate is added/removed from this users selection list for this saved search
    const request: SelectCandidateInSearchRequest = {
      userId: this.loggedInUser.id,
      candidateId: candidate.id,
      selected: selected
    };
    this.savedSearchService.selectCandidate(this.candidateSource.id, request).subscribe(
      () => {
      },
      err => {
        this.error = err;
      }
    );
  }

  saveSelection() {
    //Show modal allowing for list selection
    const modal = this.modalService.open(SelectListComponent);
    modal.componentInstance.action = "Save";
    modal.componentInstance.title = "Save Selection to List";
    if (this.candidateSource.sfJoblink != null) {
      modal.componentInstance.sfJoblink = this.candidateSource.sfJoblink;
    }
    if (!isSavedSearch(this.candidateSource)) {
      modal.componentInstance.excludeList = this.candidateSource;
    }

    modal.result
      .then((selection: TargetListSelection) => {
        const request: SaveSelectionRequest = selection;
        request.userId = this.loggedInUser.id;
        this.doSaveSelection(request);
      })
      .catch(() => { /* Isn't possible */
      });

  }

  saveSelectionAgain() {
    const request: SaveSelectionRequest = {
      userId: this.loggedInUser.id,
      savedListId: this.targetListId,
      replace: this.targetListReplace
    };
    this.doSaveSelection(request);
  }

  private doSaveSelection(request: SaveSelectionRequest) {
    //Save selection as specified in request
    this.savingSelection = true;
    this.error = null;

    if (isSavedSearch(this.candidateSource)) {

      const savedSearch = this.candidateSource;

      if (!savedSearch.defaultSearch) {

        //If the search is already saved, just save the selection
        this.saveSavedSearchSelection(savedSearch, request);

      } else {

        //If default search, auto save the search, then save the selection

        const ssCreateRequest: CreateFromDefaultSavedSearchRequest = {
          savedListId: request.savedListId,
          name: request.newListName,
          sfJoblink: request.sfJoblink
        };
        this.savedSearchService.createFromDefaultSearch(ssCreateRequest).subscribe(
          (newSavedSearch) => {

            this.saveSavedSearchSelection(newSavedSearch, request);

            //Navigate away from the default saved search to the newly created
            //search.
            const urlCommands = getCandidateSourceNavigation(newSavedSearch);
            this.savingSelection = false;
            this.router.navigate(urlCommands);
          },
          (error) => {
            this.error = error;

            //Even if auto saved search failed, we still want to save the selection
            this.saveSavedSearchSelection(savedSearch, request);
          });
      }

    } else {
      // LIST
      //Pick up ids info - including source list id
      const ids: IHasSetOfCandidates = {
        sourceListId: this.candidateSource.id,
        candidateIds: this.selectedListCandidates
      };
      // If request has a savedListId, merge or replace. Otherwise create a new list.
      if (request.savedListId > 0) {
        this.replaceOrMergeList(request.savedListId, ids, request.replace);
      } else {
        // create new saved list
        this.createList(request.newListName, ids, request.replace, request.sfJoblink);
      }
    }
  }

  private saveSavedSearchSelection(
    savedSearch: SavedSearch, request: SaveSelectionRequest) {

    this.savedSearchService.saveSelection(savedSearch.id, request)
      .subscribe(
        savedListResult => {
          this.savingSelection = false;

          //Save the target list
          this.targetListId = savedListResult.id;
          this.targetListName = savedListResult.name;
          this.targetListReplace = request.replace;

          //Associate current target list with this source.
          this.cacheTargetList(savedSearch);

          //Invalidate the cache for this list (so that user does not need
          //to refresh in order to see latest list contents)
          this.candidateSourceResultsCacheService.removeFromCache(savedListResult);

          this.savingSelection = false;

        },
        err => {
          this.error = err;
          this.savingSelection = false;
        });
  }

  private replaceOrMergeList(savedListId: number, ids: IHasSetOfCandidates, replace: boolean) {
    //Get saved list
    this.savedListService.get(savedListId).subscribe(
      (savedList) => {
        this.targetListName = savedList.name;
      }
    )
    if (replace) {
      this.savedListCandidateService.replace(savedListId, ids).subscribe(
        () => {
          this.savingSelection = false;
          //Save the target list
          this.targetListId = savedListId;
          this.targetListReplace = true;
          //Invalidate the cache for this list (so that user does not need
          //to refresh in order to see latest list contents)
          this.candidateSourceResultsCacheService.removeFromCache(this.candidateSource);

        },
        (error) => {
          this.error = error;
        }
      );
    } else {
      this.savedListCandidateService.merge(savedListId, ids).subscribe(
        () => {
          this.savingSelection = false;
          //Save the target list
          this.targetListId = savedListId;
          this.targetListReplace = false;
          //Invalidate the cache for this list (so that user does not need
          //to refresh in order to see latest list contents)
          this.candidateSourceResultsCacheService.removeFromCache(this.candidateSource);

        },
        (error) => {
          this.error = error;
        }
      );
    }
  }

  private createList(newListName: string, ids: IHasSetOfCandidates,
                     replace: boolean, sfJobLink: string) {
    const createSavedListRequest: CreateSavedListRequest = {
      sourceListId: ids.sourceListId,
      candidateIds: ids.candidateIds,
      fixed: null,
      name: newListName,
      sfJoblink: sfJobLink,
    }
    this.savedListService.create(createSavedListRequest).subscribe(
      savedListResult => {
        this.savingSelection = false;
        //Save the target list
        this.targetListId = savedListResult.id;
        this.targetListName = savedListResult.name;
        this.targetListReplace = replace;

        //Remember the target list for this source so that the user does not
        //have type in details each time they want to save
        this.cacheTargetList();

        //Invalidate the cache for this list (so that user does not need
        //to refresh in order to see latest list contents)
        this.candidateSourceResultsCacheService.removeFromCache(savedListResult);
      })
  }

  clearSelection() {
    const request: ClearSelectionRequest = {
      userId: this.loggedInUser.id,
    };
    if (isSavedSearch(this.candidateSource)) {
      this.savedSearchService.clearSelection(this.candidateSource.id, request).subscribe(
        () => {
          this.doSearch(true);
        },
        err => {
          this.error = err;
        });
    } else {
      this.selectedListCandidates = [];
      this.doSearch(true);
    }

  }

  /**
   * We keep track of the list used to save selections in local memory
   * associated with the candidate source.
   */
  private cacheTargetList(source: CandidateSource = this.candidateSource) {
    const cachedTargetList: CachedTargetList = {
      sourceID: source.id,
      listID: this.targetListId,
      name: this.targetListName,
      replace: this.targetListReplace
    }
    this.localStorageService.set(this.savedTargetListKey(source), cachedTargetList);
  }

  /**
   * We keep track of the list used to save selections in local memory
   * associated with the candidate source.
   */
  private restoreTargetListFromCache() {
    const cachedTargetList: CachedTargetList =
       this.localStorageService.get(this.savedTargetListKey());
    this.targetListId = cachedTargetList ? cachedTargetList.listID : null;
    this.targetListName = cachedTargetList ? cachedTargetList.name : null;
    this.targetListReplace = cachedTargetList ? cachedTargetList.replace : null;

  }

  private savedTargetListKey(source: CandidateSource = this.candidateSource): string {
    return "Target" + source.id;
  }

  review(candidate: Candidate) {
    const editModal = this.modalService.open(EditCandidateReviewStatusItemComponent, {
      centered: true,
      backdrop: 'static'
    });

    let item: CandidateReviewStatusItem = null;
    const items: CandidateReviewStatusItem[] = candidate.candidateReviewStatusItems;
    if (items) {
      item = items.find(s => s.savedSearch.id === this.candidateSource.id);
    }

    editModal.componentInstance.candidateReviewStatusItemId = item ? item.id : null;
    editModal.componentInstance.candidateId = candidate.id;
    editModal.componentInstance.savedSearch = this.candidateSource as SavedSearch;

    editModal.result
      .then(() => this.onReviewStatusChange())
      .catch(() => { /* Isn't possible */ });

  }

  doCopyLink() {
    copyToClipboard(getCandidateSourceExternalHref(
      this.router, this.location, this.candidateSource));
  }

  addCandidateToList(candidate: Candidate) {
    const request: IHasSetOfCandidates = {
      candidateIds: [candidate.id]
    };
    this.savedListCandidateService.merge(this.candidateSource.id, request).subscribe(
      () => {
        this.doSearch(true);
      },
      (error) => {
        this.error = error;
      }
    );

  }

  removeCandidateFromList(candidate: Candidate) {
    const request: IHasSetOfCandidates = {
      candidateIds: [candidate.id]
    };
    this.savedListCandidateService.remove(this.candidateSource.id, request).subscribe(
      () => {
        this.doSearch(true);
      },
      (error) => {
        this.error = error;
      }
    );

  }

  renderCandidateRow(candidate: Candidate) {
    if (this.candidateFieldService.isCandidateNameViewable()) {
      return candidate.candidateNumber + ": " + candidate.user.firstName + " " + candidate.user.lastName;
    } else {
      return candidate.candidateNumber;
    }
  }

  selectSearchResult ($event, input) {
    $event.preventDefault();
    input.value = '';
    const candidate: Candidate = $event.item;
    this.addCandidateToList(candidate);

  }

  createUpdateSalesforce() {
    this.error = null;
    this.updating = true;
    this.candidateSourceCandidateService.createUpdateSalesforce(
      this.candidateSource).subscribe(
      result => {
        this.doSearch(true);
        this.updating = false;
      },
      err => {
        this.error = err;
        this.updating = false;
      }
    );
  }

  doEditSource() {
    this.editSource.emit(this.candidateSource);
  }

  isDefaultSavedSearch(): boolean {
    if (isSavedSearch(this.candidateSource)) {
      return this.candidateSource.defaultSearch;
    } else {
     return false;
    }
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

  doSelectColumns() {
//todo Complete this
    //Initialize with current configuration
    //Output is new configuration
    const modal = this.modalService.open(CandidateColumnSelectorComponent);
    modal.componentInstance.selectedFields = this.selectedFields;

    modal.result
      .then((fields) => {
        console.log(fields)
        this.selectedFields = fields;
          },
          error => {
          }
        )
      .catch();

  }

  isCandidateNameViewable() {
    return this.candidateFieldService.isCandidateNameViewable()
  }
}
