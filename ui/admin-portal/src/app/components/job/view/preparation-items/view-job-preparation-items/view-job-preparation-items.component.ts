import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {Job} from "../../../../../model/job";

@Component({
  selector: 'app-view-job-preparation-items',
  templateUrl: './view-job-preparation-items.component.html',
  styleUrls: ['./view-job-preparation-items.component.scss']
})
export class ViewJobPreparationItemsComponent implements OnInit, OnChanges {
  @Input() job: Job;
  progressPercent: number;

  constructor() { }

  ngOnInit(): void {
  }

  ngOnChanges(changes: SimpleChanges): void {
    //Job has changed, recompute the status of the items
    //todo
  }

  hasCreatedSearch() {
    //todo
    return false;
  }

  hasJobDescription() {
    //todo
    return true
  }
}
