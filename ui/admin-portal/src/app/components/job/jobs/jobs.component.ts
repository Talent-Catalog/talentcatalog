import {Component, OnInit} from '@angular/core';
import {AuthService} from "../../../services/auth.service";
import {User} from "../../../model/user";
import {LocalStorageService} from "angular-2-local-storage";
import {FormBuilder, FormGroup} from "@angular/forms";
import {Job, JobOpportunityStage, SearchJobRequest} from "../../../model/job";
import {JobService} from "../../../services/job.service";
import {SearchResults} from "../../../model/search-results";
import {enumOptions} from "../../../util/enum";
import {debounceTime, distinctUntilChanged} from "rxjs/operators";

@Component({
  selector: 'app-jobs',
  templateUrl: './jobs.component.html',
  styleUrls: ['./jobs.component.scss']
})
export class JobsComponent implements OnInit {
  private pageNumber: number;
  private pageSize: number;
  private loggedInUser: User;

  private filterKeySuffix: string = 'Filter';

  searchForm: FormGroup;
  loading: boolean;
  error: string;
  results: SearchResults<Job>;
  stages = enumOptions(JobOpportunityStage);
  sortField = 'id';
  sortDirection = 'DESC';


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
    this.searchForm = this.fb.group({
      keyword: [filter],
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

  private get selectedStages(): string[] {
    return this.searchForm ? this.searchForm.value.selectedStages : "";
  }

  search() {
    let req = new SearchJobRequest();
    req.keyword = this.keyword;
    req.pageNumber = this.pageNumber - 1;
    req.pageSize = this.pageSize;

    //Sort jobs with most recent job first - ie descending order of id
    req.sortFields = ['id'];
    req.sortDirection = 'DESC';

    req.stages = this.selectedStages;

    //Don't want to see closed jobs
    req.sfOppClosed = false;

    this.error = null;
    this.loading = true;

    this.jobService.searchPaged(req).subscribe(results => {
        this.results = results;
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
    return "Jobs"
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

  viewJob(job: Job) {
    //todo
  }
}
