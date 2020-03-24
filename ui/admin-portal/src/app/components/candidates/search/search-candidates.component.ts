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
import {SavedSearch, SavedSearchRunRequest, SavedSearchType} from "../../../model/saved-search";
import {
  CachedSearchResults,
  SavedSearchResultsCacheService
} from "../../../services/saved-search-results-cache.service";
import {FormBuilder, FormGroup} from "@angular/forms";
import {User} from "../../../model/user";
import {AuthService} from "../../../services/auth.service";

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
  shortlistStatus: string[];

  selectedCandidate: Candidate;
  loggedInUser: User;
  private timestamp: number;


  constructor(private http: HttpClient,
              private fb: FormBuilder,
              private candidateService: CandidateService,
              private savedSearchService: SavedSearchService,
              private modalService: NgbModal,
              private route: ActivatedRoute,
              private savedSearchResultsCacheService: SavedSearchResultsCacheService,
              private authService: AuthService

  ) {
    this.searchForm = this.fb.group({
      shortListStatusField: ['valid'],
    });

  }

  ngOnInit() {
    this.selectedCandidate = null;
    this.loggedInUser = this.authService.getLoggedInUser();

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

        //Load saved search to get name and type
        this.savedSearchService.get(this.savedSearchId).subscribe(result => {
          this.savedSearch = result;
        }, err => {
          this.error = err;
        });

        this.doSearch(false);
      }
    });

  }

  ngOnDestroy(): void {
    if (this.subscription){
      this.subscription.unsubscribe();
    }
  }

  private constructRunRequest(): SavedSearchRunRequest {
    return {
      savedSearchId: this.savedSearchId,
      pageNumber: this.pageNumber-1,
      pageSize: this.pageSize,
      sortFields: [this.sortField],
      sortDirection: this.sortDirection,
      shortlistStatus: this.shortlistStatus
    }
  }

  doSearch(refresh: boolean) {

    this.results = null;
    this.error = null;

    let done: boolean = false;
    if (!refresh) {
      const cached: CachedSearchResults =
        this.savedSearchResultsCacheService.getFromCache(this.savedSearchId, this.shortlistStatus);
      if (cached) {
        this.results = cached.results;
        this.pageNumber = cached.pageNumber;
        this.pageSize = cached.pageSize;
        this.sortField = cached.sortFields[0];
        this.sortDirection = cached.sortDirection;
        this.setShortlistStatus(cached.shortlistStatus);
        this.timestamp = cached.timestamp;
        done = true;
      }
    }

    if (!done) {
      this.searching = true;

      this.subscription = this.candidateService.runSavedSearch(this.constructRunRequest()).subscribe(
        results => {
          this.timestamp = Date.now();
          this.results = results;

          this.savedSearchResultsCacheService.cache({
            searchID: this.savedSearchId,
            pageNumber: this.pageNumber,
            pageSize: this.pageSize,
            sortFields: [this.sortField],
            sortDirection: this.sortDirection,
            shortlistStatus: this.shortlistStatus,
            results: this.results,
            timestamp: this.timestamp
          });

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

  handleCandidateShortlistSaved(candidateShortlistItem: CandidateShortlistItem) {
    this.doSearch(true);
  }

  toggleSort(column) {
    if (this.sortField == column) {
      this.sortDirection = this.sortDirection == 'ASC' ? 'DESC' : 'ASC';
    } else {
      this.sortField = column;
      this.sortDirection = 'ASC';
    }
    this.doSearch(true);
  }

  exportCandidates() {
    this.exporting = true;
    let request = this.constructRunRequest();
    //todo Test this
    this.candidateService.exportFromSavedSearch(request, 10000).subscribe(
      result => {
        let options = {type: 'text/csv;charset=utf-8;'};
        let filename = 'candidates.csv';
        this.createAndDownloadBlobFile(result, options, filename);
        this.exporting = false;
      },
      err => {
        const reader = new FileReader();
        let _this = this;
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
    let blob = new Blob([body], options);
    if (navigator.msSaveBlob) {
      // IE 10+
      navigator.msSaveBlob(blob, filename);
    } else {
      let link = document.createElement('a');
      // Browsers that support HTML5 download attribute
      if (link.download !== undefined) {
        let url = URL.createObjectURL(blob);
        link.setAttribute('href', url);
        link.setAttribute('download', filename);
        link.style.visibility = 'hidden';
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
      }
    }
  }

  downloadCv(candidate){
    let tab = window.open();
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

  //todo Duplicated code
  getBreadcrumb() {
    let infos = this.savedSearchService.getSavedSearchTypeInfos();
    return this.savedSearch ? (infos[SavedSearchType.profession].title +
      ': ' + this.savedSearch.name) : 'Search'
  }

  onReviewStatusChange(event: any) {
    this.shortlistStatus = [];

    const value: string = typeof event === "string" ? event : event.target.value;
    if (value == 'valid'){
      this.shortlistStatus.push('pending', 'verified')
    } else {
      this.shortlistStatus.push(value);
    }

    this.doSearch(true);
  }

  private setShortlistStatus(shortlistStatus: string[]) {
    this.shortlistStatus = shortlistStatus;

    if (shortlistStatus) {
      if (shortlistStatus.length > 1) {
        this.searchForm.controls['shortListStatusField'].patchValue('valid');
      } else {
        this.searchForm.controls['shortListStatusField'].patchValue(shortlistStatus[0]);
      }
    }
  }

}
