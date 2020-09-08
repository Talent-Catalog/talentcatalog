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
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {SavedSearchService} from "../../../services/saved-search.service";
import {Observable, of, Subscription} from "rxjs";
import {CandidateReviewStatusItem} from "../../../model/candidate-review-status-item";
import {HttpClient} from "@angular/common/http";
import {
  ClearSelectionRequest,
  getCandidateSourceBreadcrumb,
  getCandidateSourceExternalHref,
  getSavedSearchBreadcrumb,
  isSavedSearch,
  SavedSearch,
  SavedSearchGetRequest,
  SaveSelectionRequest,
  SearchCandidateRequestPaged,
  SelectCandidateInSearchRequest
} from "../../../model/saved-search";
import {
  CandidateSource,
  defaultReviewStatusFilter,
  isMine,
  isSharedWithMe,
  ReviewedStatus
} from "../../../model/base";
import {
  CachedSourceResults,
  CandidateSourceResultsCacheService
} from "../../../services/candidate-source-results-cache.service";
import {FormBuilder, FormGroup} from "@angular/forms";
import {IDropdownSettings} from "ng-multiselect-dropdown";
import {User} from "../../../model/user";
import {AuthService} from "../../../services/auth.service";
import {UserService} from "../../../services/user.service";
import {
  SelectListComponent,
  TargetListSelection
} from "../../list/select/select-list.component";
import {
  IHasSetOfCandidates,
  SavedListGetRequest
} from "../../../model/saved-list";
import {CandidateSourceCandidateService} from "../../../services/candidate-source-candidate.service";
import {LocalStorageService} from "angular-2-local-storage";
import {EditCandidateReviewStatusItemComponent} from "../../util/candidate-review/edit/edit-candidate-review-status-item.component";
import {Router} from "@angular/router";
import {CandidateSourceService} from "../../../services/candidate-source.service";
import {SavedListCandidateService} from "../../../services/saved-list-candidate.service";
import {
  catchError,
  debounceTime,
  distinctUntilChanged,
  map,
  switchMap,
  tap
} from "rxjs/operators";
import {Location} from "@angular/common";
import {copyToClipboard} from "../../../util/clipboard";

interface CachedTargetList {
  searchID: number;
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
  loggedInUser: User;
  targetListName: string;
  targetListId: number;
  targetListReplace: boolean;
  timestamp: number;
  private reviewStatusFilter: string[] = defaultReviewStatusFilter;


  constructor(private http: HttpClient,
              private fb: FormBuilder,
              private candidateService: CandidateService,
              private candidateSourceService: CandidateSourceService,
              private candidateSourceCandidateService: CandidateSourceCandidateService,
              private userService: UserService,
              private savedSearchService: SavedSearchService,
              private savedListCandidateService: SavedListCandidateService,
              private modalService: NgbModal,
              private localStorageService: LocalStorageService,
              private location: Location,
              private router: Router,
              private candidateSourceResultsCacheService: CandidateSourceResultsCacheService,
              private authService: AuthService

  ) {
    this.searchForm = this.fb.group({
      statusesDisplay: [defaultReviewStatusFilter],
    });
  }

  ngOnInit() {
    this.setSelectedCandidate(null);
    this.loggedInUser = this.authService.getLoggedInUser();

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
          this.restoreTargetListFromCache();
          this.doSearch(false);
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
    this.subscription = this.candidateService.search(this.searchRequest).subscribe(
      results => {
        this.results = results;
        this.cacheResults();
        this.searching = false;
        this.searchRequest = null;
      },
      error => {
        this.error = error;
        this.searching = false;
        this.searchRequest = null;
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

  isCandidateNameViewable(): boolean {
    const role = this.loggedInUser ? this.loggedInUser.role : null;
    return role !== 'semilimited' && role !== 'limited';
  }

  isCountryViewable(): boolean {
    const role = this.loggedInUser ? this.loggedInUser.role : null;
    return role !== 'limited';
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

  isSelectable(): boolean {
    return isSavedSearch(this.candidateSource);
  }

  isShareable(): boolean {
    let shareable: boolean = false;

    //Is shareable with me if it is not fixed or created by me.
    if (this.candidateSource) {
      if (!this.candidateSource.fixed) {
        //was it created by me?
        if (!isMine(this.candidateSource, this.authService)) {
          shareable = true;
        }
      }
    }
    return shareable;
  }

  isSharedWithMe(): boolean {
    return isSharedWithMe(this.candidateSource, this.authService);
  }

  onSelectionChange(candidate: Candidate, selected: boolean) {

    //Record change
    candidate.selected = selected;
    //Update cache
    this.cacheResults();

    //Record change on server
    //Candidate is added/removed from this users selection list for this saved search
    const request: SelectCandidateInSearchRequest = {
        userId: this.loggedInUser.id,
        candidateId: candidate.id,
        selected: selected
      };
    this.savedSearchService.selectCandidate(this.candidateSource.id, request).subscribe(
      () => {},
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
    modal.componentInstance.sfJoblink = this.candidateSource.sfJoblink;

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
    this.savedSearchService.saveSelection(this.candidateSource.id, request).subscribe(
      savedListResult => {
        this.savingSelection = false;

        //Save the target list
        this.targetListId = savedListResult.id;
        this.targetListName = savedListResult.name;
        this.targetListReplace = request.replace;

        //Cache the target list
        this.cacheTargetList();

        //Invalidate the cache for this list (so that user does not need
        //to refresh in order to see latest list contents)
        this.candidateSourceResultsCacheService.removeFromCache(savedListResult);

      },
      err => {
        this.error = err;
        this.savingSelection = false;
      });
  }

  clearSelection() {
    const request: ClearSelectionRequest = {
      userId: this.loggedInUser.id,
    };
    this.savedSearchService.clearSelection(this.candidateSource.id, request).subscribe(
      () => {
        this.doSearch(true);
      },
      err => {
        this.error = err;
      });
  }

  private cacheTargetList() {
    if (isSavedSearch(this.candidateSource)) {
      const cachedTargetList: CachedTargetList = {
        searchID: this.candidateSource.id,
        listID: this.targetListId,
        name: this.targetListName,
        replace: this.targetListReplace
      }
      this.localStorageService.set(this.savedTargetListKey(), cachedTargetList);
    }
  }

  private restoreTargetListFromCache() {
    if (isSavedSearch(this.candidateSource)) {
      const cachedTargetList: CachedTargetList =
         this.localStorageService.get(this.savedTargetListKey());
      if (cachedTargetList) {
        this.targetListId = cachedTargetList.listID;
        this.targetListName = cachedTargetList.name;
        this.targetListReplace = cachedTargetList.replace;
      }
    }
  }

  private savedTargetListKey(): string {
    return "Target" + this.candidateSource.id;
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
    if (this.isCandidateNameViewable()) {
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
}
