import {
  Component,
  EventEmitter,
  Input,
  OnChanges,
  OnInit,
  Output,
  SimpleChanges
} from '@angular/core';
import {Job} from "../../../../../model/job";
import {
  JobPrepDueDate,
  JobPrepItem,
  JobPrepJD,
  JobPrepJobSummary,
  JobPrepSuggestedCandidates,
  JobPrepSuggestedSearches
} from "../../../../../model/job-prep-item";
import {
  CandidateSourceCandidateService
} from "../../../../../services/candidate-source-candidate.service";

@Component({
  selector: 'app-view-job-preparation-items',
  templateUrl: './view-job-preparation-items.component.html',
  styleUrls: ['./view-job-preparation-items.component.scss']
})
export class ViewJobPreparationItemsComponent implements OnInit, OnChanges {
  @Input() job: Job;
  @Output() itemSelected = new EventEmitter();

  selectedItem: JobPrepItem;
  error: any;

  private jobPrepSuggestedCandidates = new JobPrepSuggestedCandidates();

  jobPrepItems: JobPrepItem[] = [
    new JobPrepJobSummary(),
    new JobPrepJD(),
    //todo temporary comment out: new JobPrepJOI(),
    new JobPrepSuggestedSearches(),
    this.jobPrepSuggestedCandidates,
    new JobPrepDueDate(),
  ];

  constructor(private candidateSourceService: CandidateSourceCandidateService) {
  }

  ngOnInit(): void {
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.job) {
      this.checkSubmissionListContents();
      this.jobPrepItems.forEach(j => j.job = this.job)
    }
  }

  private checkSubmissionListContents() {
    const submissionList = this.job?.submissionList;
    if (submissionList == null) {
      this.jobPrepSuggestedCandidates.empty = true;
    } else {
      this.candidateSourceService.isEmpty(submissionList).subscribe(
        (empty) => this.jobPrepSuggestedCandidates.empty = empty,
        (error) => this.error = error
      )
    }
  }

  onItemSelected(item: JobPrepItem) {
    this.selectedItem = item;

    this.itemSelected.emit(item);
  }

  isSelected(item: JobPrepItem): boolean {
    let res = item === this.selectedItem;
    return res;
  }
}
