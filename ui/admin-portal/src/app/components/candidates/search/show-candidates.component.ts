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
import {Subscription} from "rxjs";
import {CandidateShortlistItem} from "../../../model/candidate-shortlist-item";
import {HttpClient} from "@angular/common/http";
import {
  ClearSelectionRequest,
  defaultReviewStatusFilter,
  getCandidateSourceBreadcrumb,
  getCandidateSourceType,
  getSavedSearchBreadcrumb,
  isSavedSearch,
  ReviewedStatus,
  SavedSearchGetRequest,
  SaveSelectionRequest,
  SelectCandidateInSearchRequest
} from "../../../model/saved-search";
import {
  CachedSearchResults,
  CandidateSourceResultsCacheService
} from "../../../services/candidate-source-results-cache.service";
import {FormBuilder, FormGroup} from "@angular/forms";
import {IDropdownSettings} from "ng-multiselect-dropdown";
import {ListItem} from "ng-multiselect-dropdown/multiselect.model";
import {User} from "../../../model/user";
import {AuthService} from "../../../services/auth.service";
import {UserService} from "../../../services/user.service";
import {SelectListComponent} from "../../list/select/select-list.component";
import {CandidateSource} from "../../../model/base";
import {SavedList, SavedListGetRequest} from "../../../model/saved-list";
import {CandidateSourceService} from "../../../services/candidate-source.service";

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
  targetList: SavedList;
  targetListReplace: boolean;
  timestamp: number;
  private reviewStatusFilter: string[] = defaultReviewStatusFilter;


  constructor(private http: HttpClient,
              private fb: FormBuilder,
              private candidateService: CandidateService,
              private candidateSourceService: CandidateSourceService,
              private userService: UserService,
              private savedSearchService: SavedSearchService,
              private modalService: NgbModal,
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
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (this.candidateSource) {
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

      this.candidateSourceService.searchPaged(
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
        searchID: this.candidateSource.id,
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

  onReviewStatusChange(candidateShortlistItem: CandidateShortlistItem) {
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

    this.candidateSourceService.export(
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

  onItemSelect($event: ListItem) {
    this.onReviewStatusFilterChange();
  }

  onItemDeSelect($event: ListItem) {
    this.onReviewStatusFilterChange();
  }

  onSelectAll($event: Array<ListItem>) {
    this.onReviewStatusFilterChange();
  }

  onDeSelectAll() {
    this.onReviewStatusFilterChange();
  }

  addToSharedWithMe() {
    this.savedSearchService.addSharedUser(
      this.candidateSource.id, {userId: this.loggedInUser.id}).subscribe(
      result => {
        if (result) {
          this.candidateSource = result;
        } else {
          console.log('Did not work!')
        }
      },
      error => {
        this.error = error;
      }
    );
  }

  removeFromSharedWithMe() {
    this.savedSearchService.removeSharedUser(
      this.candidateSource.id, {userId: this.loggedInUser.id}).subscribe(
      result => {
        if (result) {
          this.candidateSource = result;
        } else {
          console.log('Did not work!')
        }
      },
      error => {
        this.error = error;
      }
    );
  }

  isCandidateNameViewable(): boolean {
    const role = this.loggedInUser ? this.loggedInUser.role : null;
    return role !== 'semilimited' && role !== 'limited';
  }

  isCountryViewable(): boolean {
    const role = this.loggedInUser ? this.loggedInUser.role : null;
    return role !== 'limited';
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
        if (this.candidateSource.createdBy.id !== this.loggedInUser.id) {
          shareable = true;
        }
      }
    }
    return shareable;
  }

  isSharedWithMe(): boolean {
    //Logged in user is in saved search user
    return this.candidateSource ?
      this.candidateSource.users.find(u => u.id === this.loggedInUser.id ) !== undefined
      : false;

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
      result => {},
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
      savedListId: this.targetList.id,
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
        this.targetList = result;
        this.targetListReplace = request.replace;
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
      result => {
        this.doSearch(true);
      },
      err => {
        this.error = err;
      });
  }
}
