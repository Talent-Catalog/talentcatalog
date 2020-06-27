import {
  Component,
  Input,
  OnChanges,
  OnDestroy,
  OnInit,
  SimpleChanges,
  ViewChild
} from '@angular/core';

import {Candidate} from '../../../model/candidate';
import {CandidateService} from '../../../services/candidate.service';
import {SearchResults} from '../../../model/search-results';
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {SavedSearchService} from "../../../services/saved-search.service";
import {Observable, of, Subscription} from "rxjs";
import {CandidateShortlistItem} from "../../../model/candidate-shortlist-item";
import {HttpClient} from "@angular/common/http";
import {
  ClearSelectionRequest,
  copyCandidateSourceLinkToClipboard,
  getCandidateSourceBreadcrumb,
  getCandidateSourceType,
  getSavedSearchBreadcrumb,
  isSavedSearch,
  SavedSearch,
  SavedSearchGetRequest,
  SaveSelectionRequest,
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
  CachedSearchResults,
  CandidateSourceResultsCacheService
} from "../../../services/candidate-source-results-cache.service";
import {FormBuilder, FormGroup} from "@angular/forms";
import {IDropdownSettings} from "ng-multiselect-dropdown";
import {User} from "../../../model/user";
import {AuthService} from "../../../services/auth.service";
import {UserService} from "../../../services/user.service";
import {SelectListComponent} from "../../list/select/select-list.component";
import {
  IHasSetOfCandidates,
  SavedListGetRequest
} from "../../../model/saved-list";
import {CandidateSourceCandidateService} from "../../../services/candidate-source-candidate.service";
import {LocalStorageService} from "angular-2-local-storage";
import {EditCandidateShortlistItemComponent} from "../../util/candidate-review/edit/edit-candidate-shortlist-item.component";
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
  @Input() pageNumber: number;
  @Input() pageSize: number;

  error: any;
  loading: boolean;
  searching: boolean;
  exporting: boolean;
  savingSelection: boolean;
  searchForm: FormGroup;

  results: SearchResults<Candidate>;
  subscription: Subscription;
  sortField = 'id';
  sortDirection = 'DESC';

  /* Add candidates */
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
              private router: Router,
              private savedSearchResultsCacheService: CandidateSourceResultsCacheService,
              private authService: AuthService

  ) {
    this.searchForm = this.fb.group({
      statusesDisplay: [defaultReviewStatusFilter],
    });
  }

  ngOnInit() {
    this.selectedCandidate = null;
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

  ngOnChanges(changes: SimpleChanges): void {
    if (this.candidateSource) {
      this.restoreTargetListFromCache();
      this.doSearch(false);
    }
  }

  ngOnDestroy(): void {
    if (this.subscription) {
      this.subscription.unsubscribe();
    }
  }

  doSearch(refresh: boolean, usePageNumber = true) {

    this.results = null;
    this.error = null;

    let done: boolean = false;
    if (!refresh) {

      const cached: CachedSearchResults =
        this.savedSearchResultsCacheService.getFromCache(
          getCandidateSourceType(this.candidateSource),
          this.candidateSource.id, this.reviewStatusFilter);
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
          this.reviewStatusFilter = cached.reviewStatusFilter;
          this.timestamp = cached.timestamp;
          this.pageNumber = cached.pageNumber;
          this.pageSize = cached.pageSize;
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

    //We only cache results with the default review status filter.
    if (this.reviewStatusFilter.toString() === defaultReviewStatusFilter.toString()) {
      this.savedSearchResultsCacheService.cache(
        getCandidateSourceType(this.candidateSource),
        {
        id: this.candidateSource.id,
        pageNumber: this.pageNumber,
        pageSize: this.pageSize,
        sortFields: [this.sortField],
        sortDirection: this.sortDirection,
        reviewStatusFilter: this.reviewStatusFilter,
        results: this.results,
        timestamp: this.timestamp
      });
    }
  }

  viewCandidate(candidate: Candidate) {
    this.selectedCandidate = candidate;
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
    return isSavedSearch(this.candidateSource);
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

    modal.result
      .then((request: SaveSelectionRequest) => {

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
      result => {
        this.savingSelection = false;

        //Save the target list
        this.targetListId = result.id;
        this.targetListName = result.name;
        this.targetListReplace = request.replace;

        //Cache the target list
        this.cacheTargetList();

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
    const editModal = this.modalService.open(EditCandidateShortlistItemComponent, {
      centered: true,
      backdrop: 'static'
    });

    let item: CandidateShortlistItem = null;
    const items: CandidateShortlistItem[] = candidate.candidateShortlistItems;
    if (items) {
      item = items.find(s => s.savedSearch.id === this.candidateSource.id);
    }

    editModal.componentInstance.candidateShortListItemId = item ? item.id : null;
    editModal.componentInstance.candidateId = candidate.id;
    editModal.componentInstance.savedSearch = this.candidateSource as SavedSearch;

    editModal.result
      .then(() => this.onReviewStatusChange())
      .catch(() => { /* Isn't possible */ });

  }

  doCopyLink() {
    copyCandidateSourceLinkToClipboard(this.router, this.candidateSource);
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
    return candidate.candidateNumber + ": " + candidate.user.firstName + " " + candidate.user.lastName;
  }

  selectSearchResult ($event, input) {
    $event.preventDefault();
    input.value = '';
    const candidate: Candidate = $event.item;
    this.addCandidateToList(candidate);

  }

}
