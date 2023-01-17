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
  JobPrepJOI,
  JobPrepSuggestedCandidates,
  JobPrepSuggestedSearches
} from "../../../../../model/job-prep-item";

@Component({
  selector: 'app-view-job-preparation-items',
  templateUrl: './view-job-preparation-items.component.html',
  styleUrls: ['./view-job-preparation-items.component.scss']
})
export class ViewJobPreparationItemsComponent implements OnInit, OnChanges {
  @Input() job: Job;
  @Output() itemSelected = new EventEmitter();

  selectedItem: JobPrepItem;
  progressPercent: number;

  jobPrepItems: JobPrepItem[];

  constructor() {
  }

  ngOnInit(): void {
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.job) {
      this.jobPrepItems = [
        new JobPrepDueDate(this.job),
        new JobPrepJD(this.job),
        new JobPrepJobSummary(this.job),
        new JobPrepJOI(this.job),
        new JobPrepSuggestedCandidates(this.job),
        new JobPrepSuggestedSearches(this.job)
      ];
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
