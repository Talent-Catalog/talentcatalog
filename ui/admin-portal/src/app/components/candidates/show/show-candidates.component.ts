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
  Component,
  EventEmitter,
  Input,
  OnChanges,
  OnDestroy,
  OnInit,
  Output,
  SimpleChanges
} from '@angular/core';

import {
  Candidate,
  CandidateOpportunityParams,
  UpdateCandidateStatusInfo,
  UpdateCandidateStatusRequest
} from '../../../model/candidate';
import {CandidateService, DownloadCVRequest} from '../../../services/candidate.service';
import {NgbModal, NgbOffcanvasRef} from '@ng-bootstrap/ng-bootstrap';
import {SavedSearchService} from '../../../services/saved-search.service';
import {Observable, of, Subscription} from 'rxjs';
import {CandidateReviewStatusItem} from '../../../model/candidate-review-status-item';
import {HttpClient} from '@angular/common/http';
import {
  ClearSelectionRequest,
  getCandidateSourceExternalHref,
  getCandidateSourceStatsNavigation,
  getCandidateSourceType,
  getSavedSearchBreadcrumb,
  getSavedSourceNavigation,
  isSavedSearch,
  SavedSearch,
  SavedSearchGetRequest,
  SavedSearchRef,
  SearchCandidateRequestPaged,
  SelectCandidateInSearchRequest
} from '../../../model/saved-search';
import {
  CandidateSource,
  defaultReviewStatusFilter,
  DtoType,
  indexOfHasId,
  ReviewStatus,
  Status
} from '../../../model/base';
import {
  CandidateSourceResultsCacheService
} from '../../../services/candidate-source-results-cache.service';
import {UntypedFormBuilder, UntypedFormGroup} from '@angular/forms';
import {User} from '../../../model/user';
import {AuthorizationService} from '../../../services/authorization.service';
import {SelectListComponent, TargetListSelection} from '../../list/select/select-list.component';
import {
  ContentUpdateType,
  CopySourceContentsRequest,
  IHasSetOfCandidates,
  isSavedList,
  isSubmissionList,
  PublishedDocColumnConfig,
  PublishedDocImportReport,
  PublishListRequest,
  SavedList,
  SavedListGetRequest,
  UpdateExplicitSavedListContentsRequest
} from '../../../model/saved-list';
import {
  CandidateSourceCandidateService
} from '../../../services/candidate-source-candidate.service';
import {
  EditCandidateReviewStatusItemComponent
} from '../../util/candidate-review/edit/edit-candidate-review-status-item.component';
import {Router} from '@angular/router';
import {CandidateSourceService} from '../../../services/candidate-source.service';
import {SavedListCandidateService} from '../../../services/saved-list-candidate.service';
import {catchError, debounceTime, distinctUntilChanged, map, switchMap, tap} from 'rxjs/operators';
import {Location} from '@angular/common';
import {copyToClipboard} from '../../../util/clipboard';
import {SavedListService} from '../../../services/saved-list.service';
import {ConfirmationComponent} from '../../util/confirm/confirmation.component';
import {CandidateFieldService} from '../../../services/candidate-field.service';
import {EditCandidateStatusComponent} from "../view/status/edit-candidate-status.component";
import {
  EditCandidateOppComponent
} from "../../candidate-opp/edit-candidate-opp/edit-candidate-opp.component";
import {FileSelectorComponent} from "../../util/file-selector/file-selector.component";
import {PublishedDocColumnService} from "../../../services/published-doc-column.service";
import {
  PublishedDocColumnSelectorComponent
} from "../../util/published-doc-column-selector/published-doc-column-selector.component";
import {AssignTasksListComponent} from "../../tasks/assign-tasks-list/assign-tasks-list.component";
import {Task} from "../../../model/task";
import {SalesforceService} from "../../../services/salesforce.service";
import {
  getOpportunityStageName,
  getStageBadgeColor,
  OpportunityIds
} from "../../../model/opportunity";
import {AuthenticationService} from "../../../services/authentication.service";
import {DownloadCvComponent} from "../../util/download-cv/download-cv.component";
import {CandidateSourceBaseComponent} from "./candidate-source-base";
import {LocalStorageService} from "../../../services/local-storage.service";
import {TcModalComponent} from "../../../shared/components/modal/tc-modal.component";

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
export class ShowCandidatesComponent extends CandidateSourceBaseComponent implements OnInit, OnChanges, OnDestroy {

  @Input() manageScreenSplits: boolean = true;
  @Input() showBreadcrumb: boolean = true;
  @Input() isKeywordSearch: boolean = false;
  @Input() declare pageNumber: number;
  @Input() declare pageSize: number;
  @Input() searchRequest: SearchCandidateRequestPaged;
  @Output() candidateSelection = new EventEmitter();
  @Output() editSource = new EventEmitter();
  @Input() selectedCandidates: Candidate[];
  @Output() selectedCandidatesChange = new EventEmitter<Candidate[]>();

  loading: boolean;
  closing: boolean;
  adding: boolean;
  exporting: boolean;
  importing: boolean;
  importingFeedback: boolean;
  publishing: boolean;
  showClosedOppsTip = "Display candidates who were not successful";
  updating: boolean;
  updatingStatuses: boolean;
  updatingTasks: boolean;
  savingSelection: boolean;
  showDescription: boolean = false;

  //This form is defined differently depending on whether the candidate source is a list or a search.
  //It is used to search within existing results.
  searchInResultsForm: UntypedFormGroup;

  monitoredTask: Task;
  tasksAssignedToList: Task[];

  subscription: Subscription;

  //Request full details on candidates
  searchDetail = DtoType.EXTENDED;

  /* Add candidates support */
  doNumberOrNameSearch;
  searchFailed: boolean;


  /* MULTI SELECT */
  statuses: string[];

  currentCandidate: Candidate;
  loggedInUser: User;
  targetListName: string;
  targetListId: number;
  targetListReplace: boolean;
  savedSelection: boolean;
  savedSearchSelectionChange: boolean;

  /**
   * Once set, refers to candidate profile search card.
   * @private
   */
  private searchCard: Element;
  /**
   * Stores search card scroll bar distance from top in px for restoring if > 0.
   * @private
   */
  private searchCardScrollTop: number = 0;

  private noCandidatesMessage = "No candidates are selected";

  public filterSearch: boolean = false;

  private savedListStateKeyPrefix: string = 'ListKey';
  private showClosedOppsSuffix: string = 'ShowClosedOpps';

  sideProfile: NgbOffcanvasRef;

  constructor(private http: HttpClient,
              private fb: UntypedFormBuilder,
              private candidateService: CandidateService,
              private candidateSourceService: CandidateSourceService,
              private savedSearchService: SavedSearchService,
              private savedListCandidateService: SavedListCandidateService,
              private savedListService: SavedListService,
              private localStorageService: LocalStorageService,
              private location: Location,
              private router: Router,
              private publishedDocColumnService: PublishedDocColumnService,
              public salesforceService: SalesforceService,
              protected authorizationService: AuthorizationService,
              protected authenticationService: AuthenticationService,
              protected candidateSourceResultsCacheService: CandidateSourceResultsCacheService,
              protected candidateSourceCandidateService: CandidateSourceCandidateService,
              protected candidateFieldService: CandidateFieldService,
              protected modalService: NgbModal,
  ) {
    super(
      authorizationService,
      candidateSourceResultsCacheService,
      candidateSourceCandidateService,
      candidateFieldService,
      modalService);

    this.longFormat = true;
  }

  ngOnInit() {

    this.setCurrentCandidate(null);
    this.loggedInUser = this.authenticationService.getLoggedInUser();
    if (this.isSavedSearch()) {
       this.savedListCandidateService.getSelectionListCandidates(this.candidateSource.id).subscribe(
        (result) => {
          this.selectedCandidates = result;
          this.selectedCandidatesChange.emit(result);
        },
        (error) => {
          this.error = error;
        }
      )
    } else {
      this.selectedCandidates = [];
    }

    this.statuses = [
      ReviewStatus[ReviewStatus.rejected],
      ReviewStatus[ReviewStatus.verified],
      ReviewStatus[ReviewStatus.unverified]
    ];

    //Different use of searchForm depending on whether saved search or saved list

    if (isSavedSearch(this.candidateSource)) {
      const reviewable = this.candidateSource.reviewable;
      this.searchInResultsForm = this.fb.group({
        statusesDisplay: [reviewable ? defaultReviewStatusFilter: []],
      });
    }
    if (isSavedList(this.candidateSource)) {
      this.searchInResultsForm = this.fb.group({
        keyword: [''],
        showClosedOpps: [this.showClosedOpps]
      });
      this.subscribeToFilterChanges();

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
  }

  get pluralType() {
     return isSavedSearch(this.candidateSource) ? "searches" : "lists";
  }

  get keyword(): string {
    return this.searchInResultsForm ? this.searchInResultsForm.value.keyword : "";
  }

  private savedListStateKey(): string {
    return this.savedListStateKeyPrefix + this.candidateSource.id;
  }

  get showClosedOpps(): boolean {
    const savedShowClosedOpps = this.localStorageService.get(this.savedListStateKey() + this.showClosedOppsSuffix);
    return savedShowClosedOpps ? savedShowClosedOpps === 'true' : true;
  }

  get ReviewStatus() {
    return ReviewStatus;
  }

  get numberSelections() {
    return this.selectedCandidates.length;
  }

  subscribeToFilterChanges(): void {
    this.searchInResultsForm.valueChanges
      .pipe(
        debounceTime(800),
        distinctUntilChanged()
      )
      .subscribe(() => {
        this.filterSearch = true;
        this.saveShowClosedOpps();
        this.doSearch(true);
      });
  }

  private saveShowClosedOpps(): void {
    const showClosedOppsValue = this.searchInResultsForm.get('showClosedOpps').value;
    this.localStorageService.set(this.savedListStateKey() + this.showClosedOppsSuffix, showClosedOppsValue.toString());
  }

  /**
   * Restores candidate profile search card to previous px distance from top if > 0.
   */
  public setSearchCardScrollTop() {
    // Starting at 0 is default behaviour so no action needed in that case.
    if (this.searchCardScrollTop > 0) {
      // The card has to fully render before scrolling, so a short delay is necessary.
      setTimeout(() => {
        this.searchCard.scrollTo({ top: this.searchCardScrollTop, behavior: 'smooth' });
      }, 200)
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    //If we get both a source change and a request change, do the source
    //change in preference to the request change because the source change
    //in that case will be a saved search and it will load a new search request
    //anyway - being the search request associated with the saved search.

    if (changes.candidateSource) {
      if (changes.candidateSource.previousValue !== changes.candidateSource.currentValue) {
        if (this.candidateSource) {

          //Set the selected fields to be displayed.
          this.loadSelectedFields();

          //Retrieve the list previously used for saving selections from this
          // source (if any)
          this.restoreTargetListFromCache();
          this.doSearch(true);
          // Set the selected candidates (List only) to null when changing candidate source.
          this.selectedCandidates = [];

        }
      }
    }
    // If there is a search request associated (saved search view) and the saved search request changes, update the search.
    if (changes.searchRequest) {
      if (changes.searchRequest.previousValue !== changes.searchRequest.currentValue) {
        if (this.searchRequest) {
          //A new search request has to clear the page number.
          //Old page number is no longer relevant with a new search.
          this.pageNumber = 1;
          this.updatedSearch();
        }
      }
    }
    // If the selected candidates is cleared via the parent define search component, trigger a refresh to update the selects.
    if (changes.selectedCandidates && changes.selectedCandidates.currentValue?.length == 0 && !changes.selectedCandidates.firstChange) {
      this.doSearch(true);
    }
  }

  isSelected(candidate: Candidate): boolean {
    let selected: boolean;
    if (isSavedSearch(this.candidateSource)) {
      selected = candidate.selected;
    } else {
      selected = indexOfHasId(candidate.id, this.selectedCandidates) >= 0;
    }
    return selected;
  }

  /**
   * True if any candidates are currently selected.
   */
  isSelection(): boolean {
    return this.selectedCandidates != null && this.selectedCandidates.length > 0;
  }

  ngOnDestroy(): void {
    if (this.subscription) {
      this.subscription.unsubscribe();
    }
  }

  /**
   * This is called when an existing search is being modified - ie a search field has changed.
   * @private
   */
  private updatedSearch() {
    this.results = null;
    this.error = null;
    this.searching = true;
    const request = this.searchRequest;

    //todo jc Display a text sort toggle if there is a query string

    //Guard against the case where we have a text sort where there is no query string.
    let queryString = request.simpleQueryString;
    const haveSimpleQueryString: boolean =  queryString != null && queryString.trim().length > 0;
    if (!haveSimpleQueryString && this.sortField === "text_match") {
      //Text sort when there is no query string does not make sense.
      //So revert to standard id sort.
      this.sortField = "id";
      this.sortDirection = "DESC";
    }

    //Search passed in externally will not have current reviewStatusFilter applied
    //because that is only managed by this component. So fill it in.
    if (this.isReviewable()) {
      request.reviewStatusFilter = this.reviewStatusFilter;
    }

    request.pageNumber = this.pageNumber - 1;
    request.pageSize = this.pageSize;
    request.sortFields = [this.sortField];
    request.sortDirection = this.sortDirection;
    request.dtoType = this.searchDetail;

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
    let done = this.checkCache(refresh, usePageNumber);

    if (!done) {
      /*
       * Fetch results from the server rather than from any local cache.
       * <p/>
       * This component is used in two ways:
       * - To display saved lists
       * - To display saved searches.
       * This affects the way that a refresh is done.
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

      //If we are being driven by a manually modifiable search request (eg someone has changed the
      //search parameters and clicked on the Apply button) submit that search.
      if (this.searchRequest) {
        this.updatedSearch()
      } else {
        //Save the current candidate selection, then set it to null while we are searching.
        //Then we restore it at the end of the search. This means that anything displaying
        //info on the current selection will update its data.
        const saveCurrentCandidate = this.currentCandidate;
        this.setCurrentCandidate(null);

        //Run the saved list or saved search as stored on the server.
        this.performSearch(
          this.pageSize,
          this.searchDetail,
          this.keyword,
          this.showClosedOpps).subscribe(() => {
          // Restore the selection prior to the search using the updated results (otherwise updated fields won't appear)
          const updatedCurrentCandidate = this.results.content.find(c => c.id == saveCurrentCandidate?.id);
          this.setCurrentCandidate(updatedCurrentCandidate);
          }, error => {
            // Error is already displayed in the UI
          }
        );
      }
    }
  }

  setCurrentCandidate(candidate: Candidate) {
    this.currentCandidate = candidate;
    if (candidate && isSavedSearch(this.candidateSource)) {
      this.savedSearchSelectionChange = candidate.selected;
    }
    this.candidateSelection.emit(candidate);
  }

  selectCandidate(candidate: Candidate) {
    // Save scrollbar px distance from top on previous candidate profile search card, if any.
    if (this.currentCandidate) {
      this.searchCard = document.querySelector('.profile');
      this.searchCardScrollTop = this.searchCard.scrollTop;
    }
    this.setCurrentCandidate(candidate);
  }

  onReviewStatusChange() {
    this.doSearch(true);
  }

  toggleSort(column: string, defaultSortDirection: string = 'ASC') {
    super.toggleSort(column, defaultSortDirection);
    this.doSearch(true);
  }

  importCandidates() {

    if (isSavedList(this.candidateSource)) {

      const fileSelectorModal = this.modalService.open(FileSelectorComponent, {
        centered: true,
        backdrop: 'static'
      })

      fileSelectorModal.componentInstance.validExtensions = ['csv', 'txt'];
      fileSelectorModal.componentInstance.maxFiles = 1;
      fileSelectorModal.componentInstance.closeButtonLabel = "Import";
      fileSelectorModal.componentInstance.title = "Select file containing candidate numbers";
      fileSelectorModal.componentInstance.instructions = "Select a file with one of the above " +
        "extensions which contains a candidate number at the start of each line. " +
        "This will work for a spreadsheet that has been exported in csv format as long as " +
        "candidate numbers are in the first column of the spreadsheet. " +
        "Other data in the spreadsheet will be ignored. Any header line will also be ignored.";

      fileSelectorModal.result
      .then((selectedFiles: File[]) => {
        this.doImport(selectedFiles);
      })
      .catch(() => { /* Isn't possible */ });
    }
  }

  private doImport(files: File[]) {
    const formData: FormData = new FormData();
    formData.append('file', files[0]);

    this.error = null;
    this.importing = true;
    this.savedListCandidateService.mergeFromFile(this.candidateSource.id, formData).subscribe(
      result => {
        this.importing = false;
        this.doSearch(true);
      },
      error => {
        this.error = error;
        this.importing = false;
      }
    )
  }

  exportCandidates() {
    if (this.results.totalElements > 5000) {
      const csvError: string = "Spreadsheet exports are currently capped at 5,000 candidates — please " +
        "contact a TC admin if if this limit will negatively impact your work."
      this.error = csvError
      throw new Error(csvError)
    }
    this.exporting = true;

    //Create the appropriate request
    let request;
    let reviewable = false;
    if (isSavedSearch(this.candidateSource)) {
      reviewable = this.candidateSource.reviewable;
      request = new SavedSearchGetRequest();
    } else {
      request = new SavedListGetRequest();
    }

    //Note: The page number and size are ignored in this call (all records are exported).
    //Only the sort fields are processed.
    request.sortFields = [this.sortField];
    request.sortDirection = this.sortDirection;
    if (reviewable) {
      request.reviewStatusFilter = this.reviewStatusFilter;
    }

    this.candidateSourceCandidateService.export(this.candidateSource, request).subscribe(
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
            const errorObj = JSON.parse(reader.result);
            const csvExportErrorModal = _this.modalService.open(TcModalComponent, {});
            csvExportErrorModal.componentInstance.title = 'Export Failed';
            csvExportErrorModal.componentInstance.icon = 'fas fa-triangle-exclamation';
            csvExportErrorModal.componentInstance.actionText = 'Retry';
            csvExportErrorModal.componentInstance.message =
              "CSV download error: " + "'" + errorObj.message + "'";
            csvExportErrorModal.componentInstance.isError = true;
            csvExportErrorModal.componentInstance.onAction.subscribe(() => {
              _this.exportCandidates();
              csvExportErrorModal.close();
            });
          }
        });
        reader.readAsText(err.error);
        this.exporting = false;
      }
    );
  }

  private publishCandidates(exportColumns: PublishedDocColumnConfig[]) {
    this.publishing = true;
    this.error = null;

    //Construct the request
    const request: PublishListRequest = new PublishListRequest();
    if (this.isSubmissionList()) {
      request.publishClosedOpps = this.showClosedOpps;
    }
    request.columns = exportColumns;

    this.savedListService.publish(this.candidateSource.id, request).subscribe(
      (result: SavedList) => {
        if (isSavedList(this.candidateSource)) {
          //Update the list's published doc link and the export columns
          this.candidateSource.publishedDocLink = result.publishedDocLink;
        }
        this.candidateSource.exportColumns = result.exportColumns;
        this.publishing = false;
      },
      error => {
        this.error = error;
        this.publishing = false;
      }
    );
  }

  importEmployerFeedback() {
    this.importingFeedback = true;
    this.error = null;

    this.savedListService.importEmployerFeedback(this.candidateSource.id).subscribe(
      (result) => {
        this.importingFeedback = false;
        //Refresh to display any changed salesforce stages
        this.doSearch(true);
        this.displayImportFeedbackReport(result);
      },
      error => {
        this.error = error;
        this.importingFeedback = false;
      }
    );
  }

  private displayImportFeedbackReport(report: PublishedDocImportReport) {
    const showReport = this.modalService.open(ConfirmationComponent, {
      centered: true, backdrop: 'static'});
    showReport.componentInstance.title = "Feedback Import Report";
    showReport.componentInstance.showCancel = false;
    let mess = report.message + ".";
    if (report.numEmployerFeedbacks > 0) {
      mess += " Stored employer feedback for " + report.numEmployerFeedbacks + " candidates on Salesforce.";
    }
    if (report.numJobOffers > 0) {
      mess += " Recorded job offers for " + report.numJobOffers + " candidates.";
    }
    if (report.numNoJobOffers > 0) {
      mess += " Closed job opportunities for " + report.numNoJobOffers + " candidates.";
    }

    showReport.componentInstance.message = mess;
  }

  modifyExportColumns() {
    const modal = this.modalService.open(PublishedDocColumnSelectorComponent, {size: "lg", scrollable: true});

    modal.componentInstance.availableColumns = this.publishedDocColumnService.getColumnConfigFromAllColumns();
    modal.componentInstance.selectedColumns =  this.publishedDocColumnService.getColumnConfigFromExportColumns(this.candidateSource.exportColumns);

    modal.result
      .then((request: PublishedDocColumnConfig[]) => {
          this.publishCandidates(request);

        },
        error => this.error = error
      )
      .catch();
  }

  createAndDownloadBlobFile(body, options, filename) {
    const blob = new Blob([body], options);
    if ('msSaveBlob' in navigator) {
      // IE 10+
      // @ts-ignore
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

  /**
   * Lightly adapted version of {@link ViewCandidateComponent.downloadGeneratedCV}.
   * Opens {@link DownloadCvComponent} modal that returns CV generated from candidate profile.
   */
  downloadGeneratedCV(candidate: Candidate) {
    if (this.canViewCandidateName()) {
        // Modal
        const downloadCVModal = this.modalService.open(DownloadCvComponent, {
          centered: true,
          backdrop: 'static'
        });

        downloadCVModal.componentInstance.candidateId = candidate.id;

        downloadCVModal.result
        .then((result) => {
        })
        .catch(() => { /* Isn't possible */ });

    } else {
      // No modal giving option to view name and contact details - straight to anonymised DL
      const request: DownloadCVRequest = {
        candidateId: candidate.id,
        showName: false,
        showContact: false
      }
      const tab = window.open();
      this.candidateService.downloadCv(request).subscribe(
        result => {
          tab.location.href = URL.createObjectURL(result);
        },
        error => {
          this.error = error;
        }
      );
    }
  }

  getBreadcrumb(): string {
    let breadcrumb: string;
    if (isSavedSearch(this.candidateSource)) {
      const infos = this.savedSearchService.getSavedSearchTypeInfos();
      breadcrumb = getSavedSearchBreadcrumb(this.candidateSource, infos);
    } else {
      breadcrumb = this.getCandidateSourceBreadcrumb(this.candidateSource);
    }
    return breadcrumb;
  }

  getCandidateSourceBreadcrumb(candidateSource: CandidateSource): string {
    const sourceType = getCandidateSourceType(candidateSource);
    let sourcePrefix = isSubmissionList(candidateSource) ? "Submission" : "";
    return candidateSource != null ?
      (sourcePrefix + ' ' + sourceType + ': ' + candidateSource.name + ' (' + candidateSource.id + ')')
      : sourceType;
  }

  onReviewStatusFilterChange() {

    this.reviewStatusFilter = this.searchInResultsForm.value.statusesDisplay;

    //We can ignore page number because changing the reviewStatus filter will
    //completely change the number of results.
    //Ignoring the page number will allow the cache to supply pageNumber
    //if it has something cached.
    //Note also that a refresh will still need to be done if the review filter is not
    //the default review filter - because that is all that is cached. This is handled in doSearch.
    this.doSearch(false, false);
  }

  haveTargetList(): boolean {
    return this.targetListName && this.targetListName.length > 0;
  }

  isSavedList(): boolean {
    return !isSavedSearch(this.candidateSource);
  }

  isSavedSearch(): boolean {
    return isSavedSearch(this.candidateSource);
  }

  isSwapSelectionSupported(): boolean {
    //Not supported for saved searches because swapping an empty selection on a search could
    //potentially end up selecting huge numbers of candidates - up to the whole database.
    return !isSavedSearch(this.candidateSource);
  }

  displayTextMatchRank(): boolean {
    return this.isSavedSearch() && !this.useOldSearch && this.isKeywordSearch;
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
    }
    //Maintain local candidate selections
    if (selected) {
      this.selectedCandidates.push(candidate);
    } else {
      this.selectedCandidates = this.selectedCandidates.filter(c => c.id !== candidate.id);
    }
    this.selectedCandidatesChange.emit(this.selectedCandidates);
  }

  private doSavedSearchSelection(candidate: Candidate, selected: boolean) {
    //Record change on server
    //Candidate is added/removed from this users selection list for this saved search
    this.error = null;
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

  /**
   * Opens window to user's configured email client (eg GMail) with a new email to be completed
   * by the user, sent to a TBB account and BCCing the emails of all selected candidates.
   */
  copyEmails() {
    if (isSavedList(this.candidateSource)) {
      //Concatenate all selected candidate emails.
      let emails: string = "";
      let numEmails: number = 0;
      for (const candidate of this.selectedCandidates) {
        const email = candidate?.user.email;
        if (email) {
          emails += email + "\n";
          numEmails += 1;
        }
      }
      copyToClipboard(emails);
      const copyConfirm = this.modalService.open(ConfirmationComponent, {
        centered: true, backdrop: 'static'});
      copyConfirm.componentInstance.title = "Copied " + numEmails + " emails to clipboard";
      copyConfirm.componentInstance.showCancel = false;
      copyConfirm.componentInstance.message = "Paste the emails where you want.";
    }
  }

  saveSelection() {
    this.error = null;

    if (isSavedSearch(this.candidateSource)) {
      this.savedSearchService.getSelectionCount(this.candidateSource.id).subscribe(
        (nSelections: number) => {
          if (nSelections === 0) {
            this.error = this.noCandidatesMessage;
          } else {
            this.requestSaveSelection();
          }
        },
        (error) => {
          this.error = error;
        });
    } else {
      const nSelections = this.selectedCandidates.length;
      if (nSelections === 0) {
        this.error = this.noCandidatesMessage;
      } else {
        this.requestSaveSelection();
      }
    }
  }

  /**
   * Selected becomes unselected and vice versa.
   * <p/>
   * Note that this only works for saved lists.
   * If it is called on a saved search it will do nothing.
   */
  swapSelection() {
    if (isSavedList(this.candidateSource)) {

      //First of all, get all candidates in this list from the server.
      let request = new SavedListGetRequest();
      request.keyword = this.keyword;
      request.showClosedOpps = this.showClosedOpps;

      this.searching = true;
      this.error = null;
      this.candidateSourceCandidateService.search(this.candidateSource, request).subscribe(
        (candidates: Candidate[]) => {
          //Now do the actual swap
          this.doSwapSelection(candidates)
          this.searching = false;
        },
        error => {
          this.error = error;
          this.searching = false;
        });
    }
  }

  /**
   * Selected becomes unselected and vice versa.
   * <p/>
   * Note: only works for saved lists - not saved sources.
   * @param candidates All candidates
   */
  private doSwapSelection(candidates: Candidate[]) {
    //This will contain the new selection
    const newSelectedCandidates: Candidate[] = [];

    //Invert the selection by looking at all candidates, and if they are not currently selected
    //add them to newSelectedCandidates.
    for (const candidate of candidates) {
      //Look for this candidate's id in the currently selected candidates
      if (indexOfHasId(candidate.id, this.selectedCandidates) < 0) {
        //Not in currently selected - so add to new selected.
        newSelectedCandidates.push(candidate);
      }
    }
    //Switch to new selection
    this.selectedCandidates = newSelectedCandidates;
    this.selectedCandidatesChange.emit(this.selectedCandidates);
  }

  private requestSaveSelection() {
    //Show modal allowing for list selection
    const modal = this.modalService.open(SelectListComponent, {size: "lg"});
    modal.componentInstance.action = "Save";
    modal.componentInstance.title = "Save Selection to List";
    let readOnly = this.authorizationService.isReadOnly();
    let employerPartner = this.authorizationService.isEmployerPartner();
    modal.componentInstance.readOnly = readOnly;
    modal.componentInstance.employerPartner = employerPartner;
    modal.componentInstance.canChangeStatuses = !readOnly;
    if (this.candidateSource.sfJobOpp != null) {
      modal.componentInstance.jobId = this.candidateSource?.sfJobOpp?.id;
    }
    if (!isSavedSearch(this.candidateSource)) {
      modal.componentInstance.excludeList = this.candidateSource;
    }

    modal.result
    .then((selection: TargetListSelection) => {
      this.doSaveSelection(selection);
    })
    .catch(() => { /* Isn't possible */
    });
  }

  saveSelectionAgain() {
    const request: TargetListSelection = {
      savedListId: this.targetListId,
      replace: this.targetListReplace
    };
    this.doSaveSelection(request);
  }

  private doSaveSelection(targetListSelection: TargetListSelection) {
    //Save selection as specified in request
    this.savingSelection = true;
    this.error = null;

    if (isSavedSearch(this.candidateSource)) {

      const savedSearch = this.candidateSource;

      this.saveSavedSearchSelection(savedSearch, targetListSelection);

    } else {
      // LIST

      const savedListId = targetListSelection.savedListId;
      const request: UpdateExplicitSavedListContentsRequest = {
        name: targetListSelection.newListName,
        statusUpdateInfo: targetListSelection.statusUpdateInfo,
        updateType: targetListSelection.replace ? ContentUpdateType.replace : ContentUpdateType.add,
        jobId: targetListSelection.jobId,
        candidateIds: this.selectedCandidates.map(c => c.id),
        sourceListId: this.candidateSource.id
      };
      // If request has a savedListId, merge or replace. Otherwise create a new list.
      if (savedListId > 0) {
        this.replaceOrMergeList(savedListId, request);
      } else {
        // create new saved list
        this.createList(request);
      }
    }
  }

  private saveSavedSearchSelection(savedSearch: SavedSearch, targetChoice: TargetListSelection) {
    const request: CopySourceContentsRequest = {
      savedListId: targetChoice.savedListId,
      newListName: targetChoice.newListName,
      updateType: targetChoice.replace ? ContentUpdateType.replace : ContentUpdateType.add,
      jobId: targetChoice.jobId,
      statusUpdateInfo: targetChoice.statusUpdateInfo,

    }
    this.savedSearchService.saveSelection(savedSearch.id, request)
      .subscribe(
        savedListResult => {
          this.savingSelection = false;

          //Save the target list
          this.targetListId = savedListResult.id;
          this.targetListName = savedListResult.name;
          this.targetListReplace = targetChoice.replace;
          this.savedSelection = true;

          //Associate current target list with this source.
          this.cacheTargetList(savedSearch);

          //Invalidate the cache for this list (so that user does not need
          //to refresh in order to see latest list contents)
          this.candidateSourceResultsCacheService.removeFromCache(savedListResult);

          this.savingSelection = false;

          if (targetChoice.statusUpdateInfo != null) {
            //Refresh display to see updated statuses
            this.doSearch(true);
          }

        },
        err => {
          this.error = err;
          this.savingSelection = false;
        });
  }

  private replaceOrMergeList(savedListId: number, request: UpdateExplicitSavedListContentsRequest) {
    //Get saved list
    this.savedListService.get(savedListId).subscribe(
      (savedList) => {
        this.targetListName = savedList.name;
      }
    )
    this.savedListCandidateService.saveSelection(savedListId, request).subscribe(
      () => {
        this.savingSelection = false;
        //Save the target list
        this.targetListId = savedListId;
        this.targetListReplace = request.updateType === ContentUpdateType.replace;
        this.savedSelection = true;
        //Invalidate the cache for this list (so that user does not need
        //to refresh in order to see latest list contents)
        this.candidateSourceResultsCacheService.removeFromCache(this.candidateSource);

        if (request.statusUpdateInfo != null) {
          //Refresh display to see updated statuses
          this.doSearch(true);
        }

      },
      (error) => {
        this.savingSelection = false;
        this.error = error;
      }
    );
  }

  private createList(request: UpdateExplicitSavedListContentsRequest) {
    this.savedListCandidateService.create(request).subscribe(
      savedListResult => {
        this.savingSelection = false;
        //Save the target list
        this.targetListId = savedListResult.id;
        this.targetListName = savedListResult.name;
        this.targetListReplace = request.updateType === ContentUpdateType.replace;

        //Remember the target list for this source so that the user does not
        //have type in details each time they want to save
        this.cacheTargetList();

        //Invalidate the cache for this list (so that user does not need
        //to refresh in order to see latest list contents)
        this.candidateSourceResultsCacheService.removeFromCache(savedListResult);

        if (request.statusUpdateInfo != null) {
          //Refresh display to see updated statuses
          this.doSearch(true);
        }
      },
      (error) => {
        this.savingSelection = false;
        this.error = error;
      })
  }

  clearSelectionAndDoSearch() {
    if (isSavedSearch(this.candidateSource)) {
      const request: ClearSelectionRequest = {
        userId: this.loggedInUser.id,
      };
      this.savedSearchService.clearSelection(this.candidateSource.id, request).subscribe(
        () => {
          // No need to do the search here as it'll be handed by the ngOnChanges trigger, if it's here it'll occur twice.
          this.selectedCandidatesChange.emit([]);
        },
        err => {
          this.error = err;
        });
    } else {
      //For saved lists, the candidate data - including whether or not they have been selected
      //is not saved anywhere, so just doing a refresh will clear all displayed selections
      this.doSearch(true);
    }
    this.selectedCandidates = [];
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
    const text = getCandidateSourceExternalHref(
      this.router, this.location, this.candidateSource);
    copyToClipboard(text);
    const showReport = this.modalService.open(ConfirmationComponent, {
      centered: true, backdrop: 'static'});
    showReport.componentInstance.title = "Copied link to clipboard";
    showReport.componentInstance.showCancel = false;
    showReport.componentInstance.message = "Paste the link where you want";
    showReport.componentInstance.message = "Paste the link (" + text + ") where you want";
  }

  addCandidateToList(candidate: Candidate) {
    this.adding = true;
    const request: IHasSetOfCandidates = {
      candidateIds: [candidate.id]
    };
    this.savedListCandidateService.merge(this.candidateSource.id, request).subscribe(
      () => {
        this.doSearch(true);
        this.adding = false;
      },
      (error) => {
        this.error = error;
        this.adding = false;
      }
    );

  }

  private removeFromList(candidates: Candidate[]) {

    //Need to deselect any candidates being removed.
    this.selectedCandidates = this.selectedCandidates.filter(c => !candidates.includes(c));

    const request: IHasSetOfCandidates = {
      candidateIds: candidates.map(c => c.id)
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

  removeCandidateFromList(candidate: Candidate) {
    this.removeFromList([candidate]);
  }

  removeSelectedCandidatesFromList() {
    this.removeFromList(this.selectedCandidates);
  }

  renderCandidateRow(candidate: Candidate) {
    if (this.candidateFieldService.isCandidateNameViewable()) {
      return candidate.candidateNumber + ": " + candidate.user.firstName + " " + candidate.user.lastName;
    } else {
      return candidate.candidateNumber;
    }
  }

  selectCandidateToAdd ($event, input) {
    $event.preventDefault();
    input.value = '';
    const candidate: Candidate = $event.item;
    this.addCandidateToList(candidate);

  }

  /**
   * Updates/creates candidate related records on Salesforce.
   * <p/>
   * Only works with saved lists (a bit dangerous with saved searches which could involve
   * very large numbers of candidates - even all candidates on database!).
   * Should be disabled in html for saved searches, but if it does get called for a search, it
   * will do nothing.
   */
  createUpdateSalesforce() {
    if (isSavedList(this.candidateSource)) {
      const nSelections = this.selectedCandidates.length;
      if (nSelections === 0) {
        //No candidates are selected, check whether the user wants to apply to the whole list.
        const applyToWholeListQuery = this.modalService.open(ConfirmationComponent, {
          centered: true, backdrop: 'static'});
        applyToWholeListQuery.componentInstance.message =
          'There are no candidates selected. Would you like to apply to everyone in the list?';
        applyToWholeListQuery.result
        .then((confirmed) => {if (confirmed === true) {
          this.doCreateUpdateSalesforceOnList(false);
          }})
        .catch(() => { });
      } else {
        this.doCreateUpdateSalesforceOnList(true);
      }
    }
  }

  private doCreateUpdateSalesforceOnList(selectedCandidatesOnly: boolean) {
    if (!this.candidateSource.sfJobOpp) {
      //If we do not have a job opportunity, there will be no candidate opp info.
      this.doCreateUpdateSalesforceOnList2(null, selectedCandidatesOnly);
    } else {
      const applyToWholeListQuery = this.modalService.open(EditCandidateOppComponent, {size: 'lg'});
      applyToWholeListQuery.result
      .then((info: CandidateOpportunityParams) => {
        this.doCreateUpdateSalesforceOnList2(info, selectedCandidatesOnly);
      })
      .catch(() => { });
    }
}

  private doCreateUpdateSalesforceOnList2(info: CandidateOpportunityParams, selectedCandidatesOnly: boolean) {
    this.error = null;
    this.updating = true;

    if (selectedCandidatesOnly) {
      const jobIds: OpportunityIds = this.candidateSource.sfJobOpp;
      if (jobIds) {
        const candidateIds: number[] = this.selectedCandidates.map(c => c.id);
        this.candidateService.createUpdateOppsFromCandidates(candidateIds, jobIds.sfId, info)
        .subscribe(result => {
            //Refresh to display any changed stages
            this.doSearch(true);
            this.updating = false;
          },
          err => {this.error = err; this.updating = false; }
        );
      }
    } else {
      this.candidateService.createUpdateOppsFromCandidateList(this.candidateSource, info)
      .subscribe(result => {
          //Refresh to display any changed salesforce stages
          this.doSearch(true);
          this.updating = false;
        },
        err => {this.error = err; this.updating = false; }
      );
    }
  }

  doEditSource() {
    this.editSource.emit(this.candidateSource);
  }

  isDefaultSavedSearch(): boolean {
    if (isSavedSearch(this.candidateSource)) {
      return this.candidateSource?.defaultSearch;
    } else {
     return false;
    }
  }

  private getSavedSearchSource(): SavedSearchRef {
    if (isSavedList(this.candidateSource)) {
      return this.candidateSource?.savedSearchSource;
    } else {
      return null;
    }
  }

  hasSavedSearchSource(): boolean {
    return this.getSavedSearchSource() != null;
  }

  hasPublishedDoc() {
    return isSavedList(this.candidateSource) && this.candidateSource.publishedDocLink != null;
  }

  doShowPublishedDoc() {
    if (isSavedList(this.candidateSource)) {
      const folderlink = this.candidateSource.publishedDocLink;
      if (folderlink) {
        //Open link in new window
        window.open(folderlink, "_blank");
      }
    }
  }

  doShowSalesforceLink() {
    const joblink = this.salesforceService.joblink(this.candidateSource);
    if (joblink != null) {
        //Open link in new window
        window.open(joblink, "_blank");
    }
  }

  doShowListFolder() {
    if (isSavedList(this.candidateSource)) {
      const folderlink = this.candidateSource.folderlink;
      if (folderlink) {
        //Open link in new window
        window.open(folderlink, "_blank");
      } else {
        this.error = null;
        this.searching = true;
        this.savedListService.createFolder(this.candidateSource.id).subscribe(
          savedList => {
            this.candidateSource = savedList;
            this.searching = false;
            window.open(savedList.folderlink, "_blank");
          },
          error => {
            this.error = error;
            this.searching = false;
          });
      }
    }
  }

  doShowSearch() {
    const savedSearchSource = this.getSavedSearchSource();
    if (savedSearchSource != null) {
      this.router.navigate(getSavedSourceNavigation(savedSearchSource));
    }
  }

  doRunStats() {
    //Navigate to the infographics requesting it to run stats on this source.
    const urlCommands = getCandidateSourceStatsNavigation(this.candidateSource);
    this.router.navigate(urlCommands);
  }

  isCandidateNameViewable() {
    return this.candidateFieldService.isCandidateNameViewable()
  }

  updateStatusOfSelection() {
    this.error = null;

    if (isSavedSearch(this.candidateSource)) {
      this.savedSearchService.getSelectionCount(this.candidateSource.id).subscribe(
        (nSelections: number) => {
          if (nSelections === 0) {
            this.error = this.noCandidatesMessage;
          } else {
            this.requestNewStatusInfo(nSelections);
          }
        },
        (error) => {
          this.error = error;
        });
    } else {
      const nSelections = this.selectedCandidates.length;
      if (nSelections === 0) {
        this.error = this.noCandidatesMessage;
      } else {
        this.requestNewStatusInfo(nSelections);
      }
    }
  }

  private requestNewStatusInfo(nSelections: number) {
    const modal = this.modalService.open(EditCandidateStatusComponent);
    if (nSelections > 1) {
      modal.componentInstance.warningText = "You are about to set the status of " +
        nSelections + " candidates. This can only be undone manually, one by one.";
    }
    modal.result
    .then((info: UpdateCandidateStatusInfo) => {
      this.updateCandidateStatuses(info);
    } )
    .catch(() => { /* Isn't possible */ }
    );
  }

  private updateCandidateStatuses(info: UpdateCandidateStatusInfo) {
    this.updatingStatuses = true;
    this.error = null;

    if (isSavedSearch(this.candidateSource)) {
      this.savedSearchService.updateSelectedStatuses(this.candidateSource.id, info).subscribe(
        () => {
          //Refresh display tp see updated statuses
          this.doSearch(true);
          this.updatingStatuses = false;
        },
        (error) => {
          this.error = error;
          this.updatingStatuses = false;
        });
    } else {
      const request: UpdateCandidateStatusRequest = {
        candidateIds: this.selectedCandidates.map(c => c.id),
        info: info
      };
      this.candidateService.updateStatus(request).subscribe(
        () => {
          //Update local candidates with new status
          for (const candidate of this.selectedCandidates) {
            candidate.status = info.status;
          }
          this.updatingStatuses = false;
          this.doSearch(true);
        },
        (error) => {
          this.error = error;
          this.updatingStatuses = false;
        });
    }
  }

  assignTasks() {
    const modal = this.modalService.open(AssignTasksListComponent, {scrollable: true, size: "xl"});
    if (isSavedList(this.candidateSource)) {
      modal.componentInstance.setTasks(this.candidateSource);
    }

    modal.result.then(
        (result) => {
          if (result != null) {
            this.monitoredTask = result;
            this.doSearch(true);
          } else {
            this.doSearch(true);
          }
        }
      )
      .catch();
  }

  hasTaskAssignments(candidate: Candidate): boolean {
    const active = candidate.taskAssignments?.filter(ta => ta.status === Status.active);
    return active?.length > 0;
  }

  doCopySource() {
    //Show modal allowing for list selection
    const modal = this.modalService.open(SelectListComponent, {size: "lg"});
    modal.componentInstance.action = "Copy";
    modal.componentInstance.title = "Copy to another List";
    let readOnly = this.authorizationService.isReadOnly();
    let employerPartner = this.authorizationService.isEmployerPartner();
    modal.componentInstance.readOnly = readOnly;
    modal.componentInstance.employerPartner = employerPartner;
    modal.componentInstance.canChangeStatuses = !readOnly;

    modal.componentInstance.excludeList = this.candidateSource;

    modal.result
      .then((selection: TargetListSelection) => {
        this.loading = true;
        const request: CopySourceContentsRequest = {
          savedListId: selection.savedListId,
          newListName: selection.newListName,
          sourceListId: this.candidateSource.id,
          statusUpdateInfo: selection.statusUpdateInfo,
          updateType: selection.replace ? ContentUpdateType.replace : ContentUpdateType.add,
          jobId: this.candidateSource?.sfJobOpp?.id

        }
        this.candidateSourceService.copy(this.candidateSource, request).subscribe(
          (targetSource) => {
            this.targetListId = targetSource.id;
            this.targetListName = targetSource.name;
            // Set false, to allow display of copied message in html. Otherwise it will display the saved message.
            this.savedSelection = false;

            //Clear cache for target list as its contents will have changed.
            this.candidateSourceResultsCacheService.removeFromCache(targetSource);

            this.loading = false;
          },
          error => {
            this.error = error;
            this.loading = false;
          }
        );
      })
      .catch(() => { /* Isn't possible */
      });
  }

  // When admins want to resolve outstanding tasks to bring the task count to all completed.
  resolveTaskAssignments() {
    this.error = null;
    const nSelections = this.selectedCandidates.length;
    if (nSelections === 0) {
      this.error = this.noCandidatesMessage;
    } else {
      if (isSavedList(this.candidateSource)) {
        this.updatingTasks = true;
        const request = {
          candidateIds: this.selectedCandidates.map(c => c.id)
        };
        this.candidateService.resolveOutstandingTasks(request).subscribe(
          () => {
            this.doSearch(true);
            this.updatingTasks = false;
          },
          (error) => {
            this.error = error;
            this.updatingTasks = false;
          });
      }
    }
  }

  getCompletedMonitoredTasks(candidate: Candidate) {
    if (this.monitoredTask != null) {
      let monitoredTask = candidate.taskAssignments.filter(ta => ta.task.id === this.monitoredTask.id && ta.status === Status.active);
      return monitoredTask.filter(ta => (ta.completedDate != null || ta.abandonedDate != null));
    } else {
      // DEFAULT tasks to monitor are required tasks
      // Only run through active tasks.
      let activeTaskAssignments = candidate.taskAssignments.filter(ta => ta.status === Status.active);
      return activeTaskAssignments.filter(ta => (ta.completedDate != null || ta.abandonedDate != null) && !ta.task.optional);
    }
  }

  getReviewStatus(candidate: Candidate): ReviewStatus {
    let item: CandidateReviewStatusItem = null;
    const items: CandidateReviewStatusItem[] = candidate.candidateReviewStatusItems;
    if (items) {
      item = items.find(s => s.savedSearch.id === this.candidateSource.id);
    }

    const reviewStatus: ReviewStatus = item == null ? ReviewStatus.unverified : ReviewStatus[item.reviewStatus];

    return reviewStatus;
  }

  getTotalMonitoredTasks(candidate: Candidate) {
    if (this.monitoredTask != null) {
      return candidate.taskAssignments.filter(ta => ta.task.id === this.monitoredTask.id && ta.status === Status.active);
    } else {
      // DEFAULT tasks to monitor are required tasks
      // Only run through active tasks.
      let activeTaskAssignments = candidate.taskAssignments.filter(ta => ta.status === Status.active);
      return activeTaskAssignments.filter(ta => !ta.task.optional);
    }
  }

  hasTasksAssigned() {
    if (isSavedList(this.candidateSource)) {
      this.tasksAssignedToList = this.candidateSource.tasks;
      return this.candidateSource.tasks.length > 0;
    }
  }

  isWatching(): boolean {
    return this.candidateSource.watcherUserIds === undefined ? false :
      this.candidateSource.watcherUserIds.indexOf(this.loggedInUser?.id) >= 0;
  }

  doToggleWatch() {
    this.loading = true;
    if (this.isWatching()) {
      this.savedSearchService
        .removeWatcher(this.candidateSource.id, {userId: this.loggedInUser.id})
        .subscribe(result => {
          //Update local copy
          this.candidateSource = result;
          this.loading = false;
        }, err => {
          this.loading = false;
          this.error = err;
        })
    } else {
      this.savedSearchService
        .addWatcher(this.candidateSource.id, {userId: this.loggedInUser.id})
        .subscribe(result => {
          //Update local copy
          this.candidateSource = result;
          this.loading = false;
        }, err => {
          this.loading = false;
          this.error = err;
        })
    }
  }

  doToggleStarred() {
    this.loading = true;
    this.error = null
    if (this.isStarred()) {
      this.candidateSourceService.unstarSourceForUser(
        this.candidateSource, {userId: this.loggedInUser.id}).subscribe(
        result => {
          this.candidateSource = result;
          this.loading = false;
        },
        error => {
          this.error = error;
          this.loading = false;
        }
      );
    } else {
      this.candidateSourceService.starSourceForUser(
        this.candidateSource, {userId: this.loggedInUser.id}).subscribe(
        result => {
          //Update local copy
          this.candidateSource = result;
          this.loading = false;
        },
        error => {
          this.error = error;
          this.loading = false;
        }
      );
    }
  }

  /**
   * Get candidate stage in opportunity matching current job
    * @param candidate Candidate who opportunities we need to search
   */
  getStage(candidate: Candidate): string {
    let stage = null;
    const opp = this.getCandidateOppForThisJob(candidate);
    if (opp) {
      stage = getOpportunityStageName(opp);
    }
    return stage;
  }

  closeSelectedOpportunities() {
    const editOpp = this.modalService.open(EditCandidateOppComponent, {size: 'lg'});
    editOpp.componentInstance.closing = true;
    editOpp.result
    .then((info: CandidateOpportunityParams) => {
      this.doCloseOpps(this.selectedCandidates, this.candidateSource.sfJobOpp, info)
    })
    .catch(() => { });
  }

  closeOpportunity(candidate: Candidate) {
    const job = this.candidateSource.sfJobOpp;
    if (job) {
      const editOpp = this.modalService.open(EditCandidateOppComponent, {size: 'lg'});
      editOpp.componentInstance.closing = true;
      editOpp.result
      .then((info: CandidateOpportunityParams) => {
        this.doCloseOpps([candidate], job, info)
      })
      .catch(() => { });
    }
  }

  /**
   *   This method is unique to the other create/update opportunity methods as we also want to deselect the selected opps.
   *   Otherwise, closed opps will still be selected and may not be viewable unless the 'show closed cases' box is checked.
   *   This could lead to these selected closed opps being targeted unintentionally by other methods which use selected candidates.
   */
  private doCloseOpps(candidates: Candidate[], job: OpportunityIds, info: CandidateOpportunityParams) {
    this.closing = true;
    this.error = null;

    const candidateIds: number[] = candidates.map(c => c.id);
    this.candidateService.createUpdateOppsFromCandidates(candidateIds, job.sfId, info)
    .subscribe(result => {
        //Need to deselect any candidates being removed.
        this.selectedCandidates = this.selectedCandidates.filter(c => !candidates.includes(c));
        //Refresh to display any changed stages
        this.doSearch(true);
        this.closing = false;
      },
      err => {this.error = err; this.closing = false; }
    );
  }

  updatedCandidate(candidate: Candidate) {
    let index = this.results.content.findIndex(c => c.id == candidate.id)
    this.results.content[index] = candidate;
  }

  public canViewCandidateName() {
    return this.authorizationService.canViewCandidateName();
  }

  public isEmployerPartner() {
    return this.authorizationService.isEmployerPartner();
  }

  isReadOnly(): boolean {
    return this.authorizationService.isReadOnly();
  }

  openCandidateInNewTab(candidateNumber: string): void {
    window.open(`/candidate/${candidateNumber}`, '_blank');
  }

  protected readonly getStageBadgeColor = getStageBadgeColor;
}
