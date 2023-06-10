import {
  Component,
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
import {indexOfHasId, SearchOppsBy} from "../../../model/base";
import {
  CandidateOpportunity,
  getCandidateOpportunityStageName,
  SearchOpportunityRequest
} from "../../../model/candidate-opportunity";
import {CandidateOpportunityService} from "../../../services/candidate-opportunity.service";
import {LocalStorageService} from "angular-2-local-storage";
import {FormBuilder, FormGroup} from "@angular/forms";
import {SearchResults} from "../../../model/search-results";
import {debounceTime, distinctUntilChanged} from "rxjs/operators";
import {formatDate} from "@angular/common";
import {SalesforceService} from "../../../services/salesforce.service";
import {AuthService} from "../../../services/auth.service";

@Component({
  selector: 'app-candidate-opps',
  templateUrl: './candidate-opps.component.html',
  styleUrls: ['./candidate-opps.component.scss']
})
export class CandidateOppsComponent implements OnInit, OnChanges {
  /**
   * Only one of these inputs should be selected - one does a search, the other just takes
   * an array of opps.
   */
  @Input() searchBy: SearchOppsBy;
  @Input() candidateOpps: CandidateOpportunity[];

  /**
   * Display the name of the job opportunity rather than the name of the candidate opportunity.
   * <p/>
   * This is useful when you are displaying the Jobs that a candidate has gone for.
   */
  @Input() showJobOppName: boolean = false;

  @Output() oppSelection = new EventEmitter();

  opps: CandidateOpportunity[];

  currentOpp: CandidateOpportunity;

  loading: boolean;
  error;
  myOppsOnlyTip = "Only show opps that I am the contact for";
  pageNumber: number;
  pageSize: number;

  results: SearchResults<CandidateOpportunity>;

  //Get reference to the search input filter element (see #searchFilter in html) so we can reset focus
  @ViewChild("searchFilter")
  searchFilter: ElementRef;

  searchForm: FormGroup;
  showClosedOppsTip = "Show opps that have been closed";

  //Default sort opps in descending order of nextDueDate
  sortField = 'nextStepDueDate';
  sortDirection = 'DESC';


  private filterKeySuffix: string = 'Filter';
  private myOppsOnlySuffix: string = 'MyOppsOnly';
  private savedStateKeyPrefix: string = 'BrowseKey';
  private showClosedOppsSuffix: string = 'ShowClosedOpps';
  private sortDirectionSuffix: string = 'SortDir';
  private sortFieldSuffix: string = 'Sort';

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,

    private localStorageService: LocalStorageService,
    private oppService: CandidateOpportunityService,
    private salesforceService: SalesforceService,
    @Inject(LOCALE_ID) private locale: string
  ) { }

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

  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.candidateOpps) {
      this.opps = this.candidateOpps;
    }
    if (changes.searchBy) {
      this.initSearchBy()
    }
  }

  private initSearchBy() {
    this.pageNumber = 1;
    this.pageSize = 30;

    //Pick up any previous keyword filter
    const filter = this.localStorageService.get(this.savedStateKey() + this.filterKeySuffix);

    //Pick up previous options
    const previousMyOppsOnly: string = this.localStorageService.get(this.savedStateKey() + this.myOppsOnlySuffix);
    const previousShowClosedOpps: string = this.localStorageService.get(this.savedStateKey() + this.showClosedOppsSuffix);

    this.searchForm = this.fb.group({
      keyword: [filter],
      myOppsOnly: [previousMyOppsOnly ? previousMyOppsOnly : false],
      showClosedOpps: [previousShowClosedOpps ? previousShowClosedOpps : false],
      selectedStages: [[]]
    });

    this.subscribeToFilterChanges();

    this.search();
  }

  private get keyword(): string {
    return this.searchForm ? this.searchForm.value.keyword : "";
  }

  private get showClosedOpps(): boolean {
    return this.searchForm ? this.searchForm.value.showClosedOpps : false;
  }

  private get myOppsOnly(): boolean {
    return this.searchForm ? this.searchForm.value.myOppsOnly : false;
  }

  get SearchOppsBy() {
    return SearchOppsBy;
  }

  private savedStateKey(): string {
    //This key is constructed from the combination of inputs which are associated with each tab
    // in home.component.html
    //This key is used to store the last state associated with each tab.

    //The standard key is "BrowseKey" + "Opps" +
    // the search by (corresponding to the specific displayed tab)
    let key = this.savedStateKeyPrefix
      + "Opps"
      + SearchOppsBy[this.searchBy];

    return key
  }

  search() {
    //Remember keyword filter
    this.localStorageService.set(this.savedStateKey() + this.filterKeySuffix, this.keyword);

    //Remember sort
    this.localStorageService.set(this.savedStateKey()+this.sortFieldSuffix, this.sortField);
    this.localStorageService.set(this.savedStateKey()+this.sortDirectionSuffix, this.sortDirection);

    //Remember options
    this.localStorageService.set(this.savedStateKey()+this.myOppsOnlySuffix, this.myOppsOnly);
    this.localStorageService.set(this.savedStateKey()+this.showClosedOppsSuffix, this.showClosedOpps);

    let req = new SearchOpportunityRequest();
    req.keyword = this.keyword;
    req.pageNumber = this.pageNumber - 1;
    req.pageSize = this.pageSize;

    req.sortFields = [this.sortField];
    req.sortDirection = this.sortDirection;

    switch (this.searchBy) {
      case SearchOppsBy.live:

        //Don't want to see closed jobs
        req.sfOppClosed = false;
        break;

      case SearchOppsBy.mine:
        if (this.myOppsOnly) {
          req.ownedByMe = true;
        } else {
          req.ownedByMyPartner = true;
        }
        if (!this.showClosedOpps) {
          //Don't show closed opps - otherwise(ie this.showClosedOpps is checked) leave
          // req.sfOppClosed undefined and both open and closed opps will be returned.
          req.sfOppClosed = false;
        }
        break;
    }

    this.error = null;
    this.loading = true;

    this.oppService.searchPaged(req).subscribe(results => {
        this.results = results;

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

        this.loading = false;
      },
      error => {
        this.error = error;
        this.loading = false;
      });

  }

  canAccessSalesforce(): boolean {
    return this.authService.canAccessSalesforce();
  }

  get getCandidateOpportunityStageName() {
    return getCandidateOpportunityStageName;
  }

  getOppSfLink(sfId: string): string {
    return this.salesforceService.sfOppToLink(sfId);
  }

  private subscribeToFilterChanges() {
    this.searchForm.valueChanges
    .pipe(
      debounceTime(400),
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

  selectCurrent(opp: CandidateOpportunity) {
    this.currentOpp = opp;

    const id: number = opp.id;
    this.localStorageService.set(this.savedStateKey(), id);

    this.oppSelection.emit(opp);

  }

  getNextStepHoverString(opp: CandidateOpportunity) {
    const date = opp == null ? null : opp.updatedDate;
    const dateStr = date == null ? "???" : formatDate(date, "yyyy-MM-dd", this.locale);
    return dateStr + ': ' + (opp.nextStep ? opp.nextStep : '');
  }
}
