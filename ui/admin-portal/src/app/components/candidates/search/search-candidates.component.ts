import {
  Component,
  OnDestroy,
  OnInit,
  ViewChild
} from '@angular/core';

import {Candidate} from '../../../model/candidate';
import {CandidateService} from '../../../services/candidate.service';
import {SearchResults} from '../../../model/search-results';
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {SavedSearchService} from "../../../services/saved-search.service";
import {Subscription} from "rxjs";
import {CandidateShortlistItem} from "../../../model/candidate-shortlist-item";
import {ActivatedRoute} from "@angular/router";
import {HttpClient} from "@angular/common/http";
import {LocalStorageService} from "angular-2-local-storage";
import {SavedSearch, SavedSearchRunRequest} from "../../../model/saved-search";


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

  results: SearchResults<Candidate>;
  savedSearch: SavedSearch;
  savedSearchId;
  subscription: Subscription;
  pageNumber: number;
  pageSize: number;
  sortField = 'id';
  sortDirection = 'DESC';

  selectedCandidate: Candidate;

  constructor(private http: HttpClient,
              private candidateService: CandidateService,
              private savedSearchService: SavedSearchService,
              private modalService: NgbModal,
              private localStorage: LocalStorageService,
              private route: ActivatedRoute) {
  }

  ngOnInit() {
    this.selectedCandidate = null;
    this.pageNumber = 1;
    this.pageSize = 20;

    // start listening to route params after everything is loaded
    this.route.params.subscribe(params => {
      this.savedSearchId = params['savedSearchId'];
      if (this.savedSearchId) {

        //Load saved search to get name and type
        this.savedSearchService.get(this.savedSearchId).subscribe(result => {
          this.savedSearch = result;
        }, err => {
          this.error = err;
        });

        this.search();
      }
    });

    //todo implement review status field change
    /* SEARCH ON Review status changes*/
  }

  ngOnDestroy(): void {
    if (this.subscription){
      this.subscription.unsubscribe();
    }
  }

  search() {
    this.pageNumber = 1;
    this.doSearch();
  }

  private constructRunRequest(): SavedSearchRunRequest {
    return {
      savedSearchId: this.savedSearchId,
      pageNumber: this.pageNumber-1,
      pageSize: this.pageSize,
      sortFields: [this.sortField],
      sortDirection: this.sortDirection,
      // todo add reviewStatus
    }
  }

  doSearch() {

    this.searching = true;
    this.results = null;
    this.error = null;

    // todo - should do some caching localStorage.setItem(this.searchKey, JSON.stringify(request));

    this.subscription = this.candidateService.runSavedSearch(this.constructRunRequest()).subscribe(
      results => {
        this.results = results;
        this.searching = false;
      },
      error => {
        this.error = error;
        this.searching = false;
      });
  }

  viewCandidate(candidate: Candidate) {
    this.selectedCandidate = candidate;
  }

  handleCandidateShortlistSaved(candidateShortlistItem: CandidateShortlistItem) {
    this.search();
  }

  toggleSort(column) {
    if (this.sortField == column) {
      this.sortDirection = this.sortDirection == 'ASC' ? 'DESC' : 'ASC';
    } else {
      this.sortField = column;
      this.sortDirection = 'ASC';
    }
    this.search();
  }

  exportCandidates() {
    this.exporting = true;
    let request = this.constructRunRequest();
    //todo implment this
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


}
