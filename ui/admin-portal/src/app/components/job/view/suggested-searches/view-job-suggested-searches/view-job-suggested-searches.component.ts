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
import {SavedSearch} from "../../../../../model/saved-search";
import {JobService} from "../../../../../services/job.service";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {InputTextComponent} from "../../../../util/input/input-text/input-text.component";

@Component({
  selector: 'app-view-job-suggested-searches',
  templateUrl: './view-job-suggested-searches.component.html',
  styleUrls: ['./view-job-suggested-searches.component.scss']
})
export class ViewJobSuggestedSearchesComponent implements OnInit, OnChanges {
  @Input() job: Job;
  @Input() editable: boolean;
  @Output() jobUpdated = new EventEmitter<Job>();

  searches: SavedSearch[] = [];
  error: any;
  saving: boolean;

  constructor(private jobService: JobService,
              private modalService: NgbModal) { }

  ngOnInit(): void {
  }

  ngOnChanges(changes: SimpleChanges): void {
    //Refresh the searches if the job changes.
    this.searches = this.job.suggestedSearches;
  }

  addSearch() {
    const inputTextModal = this.modalService.open(InputTextComponent, {
      centered: true,
      backdrop: 'static'
    });

    inputTextModal.componentInstance.title = 'Enter search name suffix';
    inputTextModal.componentInstance.message = '(The search name will start with the job name. ' +
      'You just need to add a short suffix - eg "search 1" or "elastic search")';
    inputTextModal.result.then(
      (suffix) => {
        //Ignore blank suffixes
        if (suffix.trim()) {
          this.doAddSearch(suffix);
        }
      }
    );
  }

  private doAddSearch(suffix: string) {
    this.error = null;
    this.saving = true;
    this.jobService.createSuggestedSearch(this.job.id, suffix).subscribe(
      (job) => {
        this.fireJobUpdatedEvent(job);
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
        this.fireJobUpdatedEvent(job);
        this.saving = false;
      },
      (error) => {
        this.error = error;
        this.saving = false;
      });
  }

  private fireJobUpdatedEvent(job: Job) {
    //Fire the job updated event - that should bubble up to parent components, then be fed back
    //down to this component, which will then update itself when ngOnChanges is called with the
    //updated job.
    this.jobUpdated.emit(job);
  }
}
