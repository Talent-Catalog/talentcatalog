import {Component, ElementRef, EventEmitter, Input, OnInit, Output, ViewChild} from '@angular/core';
import {AuthService} from "../../../services/auth.service";
import {User} from "../../../model/user";
import {LocalStorageService} from "angular-2-local-storage";
import {FormBuilder, FormGroup} from "@angular/forms";
import {Job, JobOpportunityStage, SearchJobRequest} from "../../../model/job";
import {JobService} from "../../../services/job.service";
import {SearchResults} from "../../../model/search-results";
import {enumOptions} from "../../../util/enum";
import {debounceTime, distinctUntilChanged} from "rxjs/operators";
import {SearchJobsBy} from "../../../model/base";
import {indexOfHasId} from "../../../model/saved-search";
import {truncate} from 'src/app/util/string';

@Component({
  selector: 'app-jobs',
  templateUrl: './jobs.component.html',
  styleUrls: ['./jobs.component.scss']
})
export class JobsComponent implements OnInit {
  @Input() searchBy: SearchJobsBy;
  @Output() jobSelection = new EventEmitter();

  /*
     MODEL: Modal set component focus from code
   */
  //Get reference to the search input filter element (see #searchFilter in html)
  @ViewChild("searchFilter")
  searchFilter: ElementRef;

  pageNumber: number;
  pageSize: number;
  private loggedInUser: User;

  private filterKeySuffix: string = 'Filter';
  private savedStateKeyPrefix: string = 'BrowseKey';
  private sortDirectionSuffix: string = 'SortDir';
  private sortFieldSuffix: string = 'Sort';

  private stagesAcceptingCandidates = [
    'candidateSearch', 'visaEligibility', 'cvPreparation', 'cvReview', 'recruitmentProcess',
  'jobOffer', 'visaPreparation'];

  searchForm: FormGroup;
  loading: boolean;
  error: string;
  results: SearchResults<Job>;
  stages = enumOptions(JobOpportunityStage);

  //Default sort jobs with most recent job first - ie descending order of created date
  sortField = 'createdDate';
  sortDirection = 'DESC';
  currentJob: Job;
  private currentIndex = 0;
  myJobsOnlyTip = "Only show jobs that were created by me";

  constructor(
    private authService: AuthService,
    private fb: FormBuilder,
    private jobService: JobService,
    private localStorageService: LocalStorageService,
  ) { }

  ngOnInit(): void {
    this.loggedInUser = this.authService.getLoggedInUser();

    //Pick up any previous keyword filter
    const filter = this.localStorageService.get(this.savedStateKey() + this.filterKeySuffix);

    //Pick up previous sort
    const previousSortDirection: string = this.localStorageService.get(this.savedStateKey() + this.sortDirectionSuffix);
    if (previousSortDirection) {
      this.sortDirection = previousSortDirection;
    }
    const previousSortField: string = this.localStorageService.get(this.savedStateKey() + this.sortFieldSuffix);
    if (previousSortField) {
      this.sortField = previousSortField;
    }

    this.searchForm = this.fb.group({
      keyword: [filter],
      myJobsOnly: [false],
      selectedStages: [[]]
    });
    this.pageNumber = 1;
    this.pageSize = 30;

    this.subscribeToFilterChanges();
    this.search();

  }

  private get keyword(): string {
    return this.searchForm ? this.searchForm.value.keyword : "";
  }

  private get myJobsOnly(): boolean {
    return this.searchForm ? this.searchForm.value.myJobsOnly : false;
  }

  get SearchJobsBy() {
    return SearchJobsBy;
  }

  private get selectedStages(): string[] {
    return this.searchForm ? this.searchForm.value.selectedStages : "";
  }

  search() {
    //Remember keyword filter
    this.localStorageService.set(this.savedStateKey() + this.filterKeySuffix, this.keyword);

    //Remember sort
    this.localStorageService.set(this.savedStateKey()+this.sortFieldSuffix, this.sortField);
    this.localStorageService.set(this.savedStateKey()+this.sortDirectionSuffix, this.sortDirection);

    let req = new SearchJobRequest();
    req.keyword = this.keyword;
    req.pageNumber = this.pageNumber - 1;
    req.pageSize = this.pageSize;

    req.sortFields = [this.sortField];
    req.sortDirection = this.sortDirection;

    req.stages = this.selectedStages;

    switch (this.searchBy) {
      case SearchJobsBy.live:

        //Don't want to see closed jobs
        req.sfOppClosed = false;

        //Only want jobs which are accepting candidates
        req.accepting = true;
        break;

      case SearchJobsBy.mine:
        if (this.myJobsOnly) {
          req.ownedByMe = true;
        } else {
          req.ownedByMyPartner = true;
        }
        break;

      case SearchJobsBy.starredByMe:
        req.starred = true;
        break;
    }

    this.error = null;
    this.loading = true;

    this.jobService.searchPaged(req).subscribe(results => {
        this.results = results;

        if (results.content.length > 0) {
          //Select previously selected item if still present in results
          const id: number = this.localStorageService.get(this.savedStateKey());
          if (id) {
            this.currentIndex = indexOfHasId(id, this.results.content);
            if (this.currentIndex >= 0) {
              this.selectCurrent(this.results.content[this.currentIndex]);
            } else {
              this.selectCurrent(this.results.content[0]);
            }
          } else {
            //Select the first search if no previous
            this.selectCurrent(this.results.content[0]);
          }
        }

        //Following the search filter loses focus, so focus back on it again
        /* MODEL: Setting component focus*/
        setTimeout(()=>{this.searchFilter.nativeElement.focus()},0);

        this.loading = false;
      },
      error => {
        this.error = error;
        this.loading = false;
      });
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

  private savedStateKey(): string {
    //This key is constructed from the combination of inputs which are associated with each tab
    // in home.component.html
    //This key is used to store the last state associated with each tab.

    //The standard key is "BrowseKey" + "Jobs" +
    // the search by (corresponding to the specific displayed tab)
    let key = this.savedStateKeyPrefix
      + "Jobs"
      + SearchJobsBy[this.searchBy];

    return key
  }

  toggleSort(column: string) {
    if (this.sortField === column) {
      this.sortDirection = this.sortDirection === 'ASC' ? 'DESC' : 'ASC';
    } else {
      this.sortField = column;
      this.sortDirection = 'ASC';
    }

    this.search();
  }

  selectCurrent(job: Job) {
    this.currentJob = job;

    const id: number = job.id;
    this.localStorageService.set(this.savedStateKey(), id);

    this.currentIndex = indexOfHasId(id, this.results.content);

    this.jobSelection.emit(job);
  }

  get truncate() {
    return truncate;
  }

  fullUserName(contactUser: User) {
    return contactUser ? contactUser.firstName + " " + contactUser.lastName : "";
  }
}
