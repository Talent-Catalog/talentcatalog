import {Component, Input, OnInit} from '@angular/core';
import {Job} from "../../../../../model/job";
import {SavedSearch} from "../../../../../model/saved-search";
import {JobService} from "../../../../../services/job.service";

@Component({
  selector: 'app-view-job-suggested-searches',
  templateUrl: './view-job-suggested-searches.component.html',
  styleUrls: ['./view-job-suggested-searches.component.scss']
})
export class ViewJobSuggestedSearchesComponent implements OnInit {
  @Input() job: Job;
  @Input() editable: boolean;

  searches: SavedSearch[] = [];
  error: any;
  saving: boolean;

  constructor(private jobService: JobService) { }

  ngOnInit(): void {
    this.searches = this.job.suggestedSearches;
  }

  addSearch() {
    this.error = null;
    this.saving = true;
    this.jobService.createSuggestedSearch(this.job.id).subscribe(
      (job) => {
        this.job = job;
        this.searches = this.job.suggestedSearches;
        this.saving = false;
      },
      (error) => {
        this.error = error;
        this.saving = false;
      });
  }

  removeSearch(search: SavedSearch) {
    this.error = null;
    this.saving = true;
    this.jobService.removeSuggestedSearch(this.job.id, search.id).subscribe(
      (job) => {
        this.job = job;
        this.searches = this.job.suggestedSearches;
        this.saving = false;
      },
      (error) => {
        this.error = error;
        this.saving = false;
      });
  }
}
