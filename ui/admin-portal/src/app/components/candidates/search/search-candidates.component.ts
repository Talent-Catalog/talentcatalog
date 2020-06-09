import {Component, OnDestroy, OnInit, ViewChild} from '@angular/core';

import {Candidate} from '../../../model/candidate';
import {CandidateService} from '../../../services/candidate.service';
import {SearchResults} from '../../../model/search-results';
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {SavedSearchService} from "../../../services/saved-search.service";
import {Subscription} from "rxjs";
import {CandidateShortlistItem} from "../../../model/candidate-shortlist-item";
import {ActivatedRoute} from "@angular/router";
import {HttpClient} from "@angular/common/http";
import {
  defaultReviewStatusFilter,
  getSavedSearchBreadcrumb,
  ReviewedStatus,
  SavedSearch,
  SavedSearchRunRequest,
  SelectCandidateInSearchRequest
} from "../../../model/saved-search";
import {
  CachedSearchResults,
  SavedSearchResultsCacheService
} from "../../../services/saved-search-results-cache.service";
import {FormBuilder, FormGroup} from "@angular/forms";
import {IDropdownSettings} from "ng-multiselect-dropdown";
import {ListItem} from "ng-multiselect-dropdown/multiselect.model";
import {User} from "../../../model/user";
import {AuthService} from "../../../services/auth.service";
import {UserService} from "../../../services/user.service";

@Component({
  selector: 'app-search-candidates',
  templateUrl: './search-candidates.component.html',
  styleUrls: ['./search-candidates.component.scss']
})
export class SearchCandidatesComponent implements OnInit, OnDestroy {

  @ViewChild('downloadCsvErrorModal', {static: true}) downloadCsvErrorModal;

  error: any;
  loading: boolean;
  searching: boolean;
  exporting: boolean;
  searchForm: FormGroup;

  results: SearchResults<Candidate>;
  savedSearch: SavedSearch;
  savedSearchId;
  subscription: Subscription;
  pageNumber: number;
  pageSize: number;
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
  private timestamp: number;
  private reviewStatusFilter: string[] = defaultReviewStatusFilter;


  constructor(private http: HttpClient,
              private fb: FormBuilder,
              private candidateService: CandidateService,
              private userService: UserService,
              private savedSearchService: SavedSearchService,
              private modalService: NgbModal,
              private route: ActivatedRoute,
              private savedSearchResultsCacheService: SavedSearchResultsCacheService,
              private authService: AuthService

  ) {
    this.searchForm = this.fb.group({
      statusesDisplay: [defaultReviewStatusFilter],
    });
  }

  ngOnInit() {
    this.loading = true;
    this.selectedCandidate = null;
    this.loggedInUser = this.authService.getLoggedInUser();

    this.statuses = [];
    for (const key in ReviewedStatus) {
      if (isNaN(Number(key))) {
        this.statuses.push(key);
      }
    }

    // start listening to route params after everything is loaded
    this.route.queryParamMap.subscribe(
      params => {
        this.pageNumber = +params.get('pageNumber');
        if (!this.pageNumber) {
          this.pageNumber = 1;
        }
        this.pageSize = +params.get('pageSize');
        if (!this.pageSize) {
          this.pageSize = 20;
        }
      }
    );

    this.route.paramMap.subscribe(params => {
      this.savedSearchId = +params.get('savedSearchId');
      if (this.savedSearchId) {

        //Load saved search to get name and type to display
        this.savedSearchService.get(this.savedSearchId).subscribe(result => {
          this.savedSearch = result;
          this.loading = false;
        }, err => {
          this.error = err;
        });

        this.doSearch(false);
      }
    });

  }

  ngOnDestroy(): void {
    if (this.subscription) {
      this.subscription.unsubscribe();
    }
  }

  private constructRunRequest(): SavedSearchRunRequest {
    return {
      savedSearchId: this.savedSearchId,
      pageNumber: this.pageNumber - 1,
      pageSize: this.pageSize,
      sortFields: [this.sortField],
      sortDirection: this.sortDirection,
      shortlistStatus: this.reviewStatusFilter
    }
  }

  doSearch(refresh: boolean, usePageNumber = true) {

    this.results = null;
    this.error = null;

    let done: boolean = false;
    if (!refresh) {
      const cached: CachedSearchResults =
        this.savedSearchResultsCacheService.getFromCache(this.savedSearchId, this.reviewStatusFilter);
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

      this.subscription = this.candidateService.runSavedSearch(this.constructRunRequest()).subscribe(
        results => {
          this.timestamp = Date.now();
          this.results = results;

          //We only cache results with the default review status filter.
          if (this.reviewStatusFilter.toString() === defaultReviewStatusFilter.toString()) {
            this.savedSearchResultsCacheService.cache({
              searchID: this.savedSearchId,
              pageNumber: this.pageNumber,
              pageSize: this.pageSize,
              sortFields: [this.sortField],
              sortDirection: this.sortDirection,
              reviewStatusFilter: this.reviewStatusFilter,
              results: this.results,
              timestamp: this.timestamp
            });
          }

          this.searching = false;
        },
        error => {
          this.error = error;
          this.searching = false;
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
    const request = this.constructRunRequest();
    this.candidateService.exportFromSavedSearch(request, 10000).subscribe(
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

  getBreadcrumb() {
    const infos = this.savedSearchService.getSavedSearchTypeInfos();
    return getSavedSearchBreadcrumb(this.savedSearch, infos)
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
      this.savedSearchId, {userId: this.loggedInUser.id}).subscribe(
      result => {
        if (result) {
          this.savedSearch = result;
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
      this.savedSearchId, {userId: this.loggedInUser.id}).subscribe(
      result => {
        if (result) {
          this.savedSearch = result;
        } else {
          console.log('Did not work!')
        }
      },
      error => {
        this.error = error;
      }
    );
  }

  isShareable(): boolean {
    let shareable: boolean = false;

    //Is shareable with me if it is not fixed or created by me.
    if (this.savedSearch) {
      if (!this.savedSearch.fixed) {
        //was it created by me?
        if (this.savedSearch.createdBy.id !== this.loggedInUser.id) {
          shareable = true;
        }
      }
    }
    return shareable;
  }

  isSharedWithMe(): boolean {
    //Logged in user is in saved search user
    return this.savedSearch ?
      this.savedSearch.users.find(u => u.id === this.loggedInUser.id ) !== undefined
      : false;

  }

  onSelectionChange(candidate: Candidate, selected: boolean) {
    //Candidate is added/removed from this users selection list for this saved search
    const request: SelectCandidateInSearchRequest = {
        userId: this.loggedInUser.id,
        candidateId: candidate.id,
        selected: selected
      };
    this.savedSearchService.selectCandidate(this.savedSearchId, request).subscribe(
      result => {},
      err => {
        this.error = err;
      }
    );
  }
}
