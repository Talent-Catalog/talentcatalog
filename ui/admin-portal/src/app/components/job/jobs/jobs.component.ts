import {Component, Inject, LOCALE_ID} from '@angular/core';
import {AuthService} from "../../../services/auth.service";
import {LocalStorageService} from "angular-2-local-storage";
import {FormBuilder} from "@angular/forms";
import {Job, JobOpportunityStage, SearchJobRequest} from "../../../model/job";
import {JobService} from "../../../services/job.service";
import {EnumOption, enumOptions} from "../../../util/enum";
import {SearchOppsBy} from "../../../model/base";
import {FilteredOppsComponent} from "../../opportunity/filtered-opps/filtered-opps.component";
import {SalesforceService} from "../../../services/salesforce.service";
import {SearchOpportunityRequest} from "../../../model/candidate-opportunity";

@Component({
  selector: 'app-jobs',
  templateUrl: './jobs.component.html',
  styleUrls: ['./jobs.component.scss']
})
export class JobsComponent extends FilteredOppsComponent<Job> {

  //Override text to replace "opps" text with "jobs"
  myOppsOnlyLabel = "My jobs only";
  myOppsOnlyTip = "Only show jobs that I manage";
  showClosedOppsLabel = "Show closed jobs";
  showClosedOppsTip = "Show jobs that have been closed";
  showInactiveOppsLabel = "Show inactive jobs";
  showInactiveOppsTip = "Show jobs that are not currently accepting new candidates";

  constructor(
    fb: FormBuilder,
    authService: AuthService,
    localStorageService: LocalStorageService,
    oppService: JobService,
    salesforceService: SalesforceService,
    @Inject(LOCALE_ID) locale: string
  ) {
    super(fb, authService, localStorageService, oppService, salesforceService, locale,
          "Jobs")
  }

  protected createSearchRequest(): SearchOpportunityRequest {
    let req =  new SearchJobRequest();

    switch (this.searchBy) {
      case SearchOppsBy.live:

        //Don't want to see closed jobs
        req.sfOppClosed = false;

        //Jobs must have been published
        req.published = true;

        //Only want jobs which are accepting candidates. This is equivalent to checking that the
        //job's stage is between candidate search and prior to job offer/acceptance.
        //This request is ignored if certain stages have been requested (because that will clash
        //with checking the above range of stages)
        req.activeStages = true;
        break;

      case SearchOppsBy.starredByMe:
        req.starred = true;
        break;
    }

    return req;
  }

  protected loadStages(): EnumOption[] {
    return enumOptions(JobOpportunityStage);
  }


  // @Input() searchBy: SearchOppsBy;
  // @Output() oppSelection = new EventEmitter();
  //
  // loading: boolean;
  // error: string;
  // myOppsOnlyTip = "Only show jobs that were created by me";
  //
  // pageNumber: number;
  // pageSize: number;
  // results: SearchResults<Job>;
  // private loggedInUser: User;
  //
  // private filterKeySuffix: string = 'Filter';
  // private myOppsOnlySuffix: string = 'MyOppsOnly';
  // private savedStateKeyPrefix: string = 'BrowseKey';
  // private showClosedOppsSuffix: string = 'ShowClosedOpps';
  // private showInactiveOppsSuffix: string = 'ShowInactiveOpps';
  // private sortDirectionSuffix: string = 'SortDir';
  // private sortFieldSuffix: string = 'Sort';
  //
  // private stagesAcceptingCandidates = [
  //   'candidateSearch', 'visaEligibility', 'cvPreparation', 'cvReview', 'recruitmentProcess',
  // 'jobOffer', 'visaPreparation'];
  //
  // /*
  //    MODEL: Modal set component focus from code
  //  */
  // //Get reference to the search input filter element (see #searchFilter in html)
  // @ViewChild("searchFilter")
  // searchFilter: ElementRef;
  //
  // searchForm: FormGroup;
  // showClosedOppsTip = "Show jobs that have been closed";
  // showInactiveOppsTip = "Show jobs that are no longer active";
  // stages = enumOptions(JobOpportunityStage);
  //
  // //Default sort jobs with most recent job first - ie descending order of created date
  // sortField = 'createdDate';
  // sortDirection = 'DESC';
  // currentJob: Job;
  //
  // constructor(
  //   private authService: AuthService,
  //   private fb: FormBuilder,
  //   private jobService: JobService,
  //   private localStorageService: LocalStorageService,
  // ) { }
  //
  // ngOnInit(): void {
  //   this.loggedInUser = this.authService.getLoggedInUser();
  //   this.pageNumber = 1;
  //   this.pageSize = 30;
  //
  //   //Pick up any previous keyword filter
  //   const filter = this.localStorageService.get(this.savedStateKey() + this.filterKeySuffix);
  //
  //   //Pick up previous sort
  //   const previousSortDirection: string = this.localStorageService.get(this.savedStateKey() + this.sortDirectionSuffix);
  //   if (previousSortDirection) {
  //     this.sortDirection = previousSortDirection;
  //   }
  //   const previousSortField: string = this.localStorageService.get(this.savedStateKey() + this.sortFieldSuffix);
  //   if (previousSortField) {
  //     this.sortField = previousSortField;
  //   }
  //
  //   //Pick up previous options
  //   const previousMyOppsOnly: string = this.localStorageService.get(this.savedStateKey() + this.myOppsOnlySuffix);
  //   const previousShowClosedOpps: string = this.localStorageService.get(this.savedStateKey() + this.showClosedOppsSuffix);
  //   const previousShowInactiveOpps: string = this.localStorageService.get(this.savedStateKey() + this.showInactiveOppsSuffix);
  //
  //   this.searchForm = this.fb.group({
  //     keyword: [filter],
  //     myOppsOnly: [previousMyOppsOnly ? previousMyOppsOnly : false],
  //     showClosedOpps: [previousShowClosedOpps ? previousShowClosedOpps : false],
  //     showInactiveOpps: [previousShowInactiveOpps ? previousShowInactiveOpps : false],
  //     selectedStages: [[]]
  //   });
  //
  //   this.subscribeToFilterChanges();
  //
  //   this.search();
  //
  // }
  //
  // private get keyword(): string {
  //   return this.searchForm ? this.searchForm.value.keyword : "";
  // }
  //
  // private get showClosedOpps(): boolean {
  //   return this.searchForm ? this.searchForm.value.showClosedOpps : false;
  // }
  //
  // private get showInactiveOpps(): boolean {
  //   return this.searchForm ? this.searchForm.value.showInactiveOpps : false;
  // }
  //
  // private get myOppsOnly(): boolean {
  //   return this.searchForm ? this.searchForm.value.myOppsOnly : false;
  // }
  //
  // get SearchOppsBy() {
  //   return SearchOppsBy;
  // }
  //
  // get selectedStages(): string[] {
  //   return this.searchForm ? this.searchForm.value.selectedStages : "";
  // }
  //
  // private savedStateKey(): string {
  //   //This key is constructed from the combination of inputs which are associated with each tab
  //   // in home.component.html
  //   //This key is used to store the last state associated with each tab.
  //
  //   //The standard key is "BrowseKey" + "Jobs" +
  //   // the search by (corresponding to the specific displayed tab)
  //   let key = this.savedStateKeyPrefix
  //     + "Jobs"
  //     + SearchOppsBy[this.searchBy];
  //
  //   return key
  // }
  //
  // search() {
  //   //Remember keyword filter
  //   this.localStorageService.set(this.savedStateKey() + this.filterKeySuffix, this.keyword);
  //
  //   //Remember sort
  //   this.localStorageService.set(this.savedStateKey()+this.sortFieldSuffix, this.sortField);
  //   this.localStorageService.set(this.savedStateKey()+this.sortDirectionSuffix, this.sortDirection);
  //
  //   //Remember options
  //   this.localStorageService.set(this.savedStateKey()+this.myOppsOnlySuffix, this.myOppsOnly);
  //   this.localStorageService.set(this.savedStateKey()+this.showClosedOppsSuffix, this.showClosedOpps);
  //   this.localStorageService.set(this.savedStateKey()+this.showInactiveOppsSuffix, this.showInactiveOpps);
  //
  //   let req = new SearchJobRequest();
  //   req.keyword = this.keyword;
  //   req.pageNumber = this.pageNumber - 1;
  //   req.pageSize = this.pageSize;
  //
  //   req.sortFields = [this.sortField];
  //   req.sortDirection = this.sortDirection;
  //
  //   req.stages = this.selectedStages;
  //
  //   switch (this.searchBy) {
  //     case SearchOppsBy.live:
  //
  //       //Don't want to see closed jobs
  //       req.sfOppClosed = false;
  //
  //       //Jobs must have been published
  //       req.published = true;
  //
  //       //Only want jobs which are accepting candidates. This is equivalent to checking that the
  //       //job's stage is between candidate search and prior to job offer/acceptance.
  //       //This request is ignored if certain stages have been requested (because that will clash
  //       //with checking the above range of stages)
  //       req.activeStages = true;
  //       break;
  //
  //     case SearchOppsBy.mine:
  //       if (this.myOppsOnly) {
  //         req.ownedByMe = true;
  //       } else {
  //         req.ownedByMyPartner = true;
  //       }
  //
  //       //Default - filters out closed opps and only includes active stages
  //       req.sfOppClosed = false;
  //       req.activeStages = true;
  //
  //       if (this.showInactiveOpps) {
  //         //Turn off the active stages filter
  //         req.activeStages = false;
  //       }
  //
  //       if (this.showClosedOpps) {
  //         req.sfOppClosed = true;
  //       }
  //       break;
  //
  //     case SearchOppsBy.starredByMe:
  //       req.starred = true;
  //       break;
  //   }
  //
  //   this.error = null;
  //   this.loading = true;
  //
  //   this.jobService.searchPaged(req).subscribe(results => {
  //       this.results = results;
  //
  //       if (results.content.length > 0) {
  //         //Select previously selected item if still present in results
  //         const id: number = this.localStorageService.get(this.savedStateKey());
  //         if (id) {
  //           let currentIndex = indexOfHasId(id, this.results.content);
  //           if (currentIndex >= 0) {
  //             this.selectCurrent(this.results.content[currentIndex]);
  //           } else {
  //             this.selectCurrent(this.results.content[0]);
  //           }
  //         } else {
  //           //Select the first search if no previous
  //           this.selectCurrent(this.results.content[0]);
  //         }
  //       }
  //
  //       //Following the search filter loses focus, so focus back on it again
  //       /* MODEL: Setting component focus*/
  //       setTimeout(()=>{this.searchFilter.nativeElement.focus()},0);
  //
  //       this.loading = false;
  //     },
  //     error => {
  //       this.error = error;
  //       this.loading = false;
  //     });
  // }
  //
  // private subscribeToFilterChanges() {
  //   this.searchForm.valueChanges
  //   .pipe(
  //     debounceTime(400),
  //     distinctUntilChanged()
  //   )
  //   .subscribe(() => {
  //     this.search();
  //   });
  // }
  //
  // toggleSort(column: string) {
  //   if (this.sortField === column) {
  //     this.sortDirection = this.sortDirection === 'ASC' ? 'DESC' : 'ASC';
  //   } else {
  //     this.sortField = column;
  //     this.sortDirection = 'ASC';
  //   }
  //
  //   this.search();
  // }
  //
  // selectCurrent(job: Job) {
  //   this.currentJob = job;
  //
  //   const id: number = job.id;
  //   this.localStorageService.set(this.savedStateKey(), id);
  //
  //   this.oppSelection.emit(job);
  // }
  //
  // fullUserName(contactUser: User) {
  //   return contactUser ? contactUser.firstName + " " + contactUser.lastName : "";
  // }
}
