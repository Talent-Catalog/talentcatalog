import {
  Component,
  EventEmitter,
  Input,
  OnChanges,
  OnInit,
  Output,
  SimpleChanges
} from '@angular/core';
import {truncate} from 'src/app/util/string';
import {SearchJobsBy} from "../../../model/base";
import {
  CandidateOpportunity,
  getCandidateOpportunityStageName,
  SearchCandidateOpportunityRequest
} from "../../../model/candidate-opportunity";
import {CandidateOpportunityService} from "../../../services/candidate-opportunity.service";

@Component({
  selector: 'app-candidate-opps',
  templateUrl: './candidate-opps.component.html',
  styleUrls: ['./candidate-opps.component.scss']
})
export class CandidateOppsComponent implements OnInit, OnChanges {
  @Input() searchBy: SearchJobsBy;
  @Input() candidateOpps: CandidateOpportunity[];

  @Output() oppSelection = new EventEmitter();

  opps: CandidateOpportunity[];

  currentOpp: CandidateOpportunity;

  loading: boolean;
  error;
  pageNumber: number;
  pageSize: number;

  constructor(
    private oppService: CandidateOpportunityService
  ) { }

  ngOnInit(): void {
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

    this.search();
  }

  private get myJobsOnly(): boolean {
    //todo
    return true;
  }

  private search() {
    let req = new SearchCandidateOpportunityRequest();
    // req.keyword = this.keyword;
    req.pageNumber = this.pageNumber - 1;
    req.pageSize = this.pageSize;

    // req.sortFields = [this.sortField];
    // req.sortDirection = this.sortDirection;

    switch (this.searchBy) {
      case SearchJobsBy.live:

        //Don't want to see closed jobs
        req.sfOppClosed = false;
        break;

      case SearchJobsBy.mine:
        if (this.myJobsOnly) {
          req.ownedByMe = true;
        } else {
          req.ownedByMyPartner = true;
        }
        break;
    }

    this.error = null;
    this.loading = true;

    this.oppService.searchPaged(req).subscribe(results => {
        this.opps = results.content;

        // if (results.content.length > 0) {
        //   //Select previously selected item if still present in results
        //   const id: number = this.localStorageService.get(this.savedStateKey());
        //   if (id) {
        //     this.currentIndex = indexOfHasId(id, this.results.content);
        //     if (this.currentIndex >= 0) {
        //       this.selectCurrent(this.results.content[this.currentIndex]);
        //     } else {
        //       this.selectCurrent(this.results.content[0]);
        //     }
        //   } else {
        //     //Select the first search if no previous
        //     this.selectCurrent(this.results.content[0]);
        //   }
        // }

        //Following the search filter loses focus, so focus back on it again
        /* MODEL: Setting component focus*/
        // setTimeout(()=>{this.searchFilter.nativeElement.focus()},0);

        this.loading = false;
      },
      error => {
        this.error = error;
        this.loading = false;
      });

  }

  get getCandidateOpportunityStageName() {
    return getCandidateOpportunityStageName;
  }

  get truncate() {
    return truncate;
  }

  selectCurrent(opp: CandidateOpportunity) {
    this.currentOpp = opp;

    const id: number = opp.id;

    this.oppSelection.emit(opp);

  }
}
