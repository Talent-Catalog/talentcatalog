import {
  Component,
  ElementRef,
  EventEmitter,
  OnInit,
  Output,
  ViewChild
} from '@angular/core';
import {Candidate} from "../../../model/candidate";
import {SearchResults} from "../../../model/search-results";
import {AuthorizationService} from "../../../services/authorization.service";
import {FetchCandidatesWithActiveChatRequest, Status} from "../../../model/base";
import {Task} from "../../../model/task";
import {CandidateService} from "../../../services/candidate.service";
import {FormBuilder, FormGroup} from "@angular/forms";
import {debounceTime, distinctUntilChanged} from "rxjs/operators";

@Component({
  selector: 'app-view-source-candidates-with-chats',
  templateUrl: './view-source-candidates-with-chats.component.html',
  styleUrls: ['./view-source-candidates-with-chats.component.scss']
})
export class ViewSourceCandidatesWithChatsComponent implements OnInit {

  @Output() candidateSelection = new EventEmitter<Candidate>();

  @ViewChild("searchFilter")
  searchFilter: ElementRef;

  error: any;
  loading: boolean;
  currentCandidate: Candidate;
  pageNumber: number = 1;
  monitoredTask: Task;
  candidatesWithActiveChats: SearchResults<Candidate>;
  sortField = 'id';
  sortDirection = 'DESC';
  searchForm: FormGroup;

  constructor(
    private candidateService: CandidateService,
    private authService: AuthorizationService,
    private fb: FormBuilder
  ) {}

  ngOnInit(): void {
    this.fetchCandidatesWithActiveChat()

    this.searchForm = this.fb.group({
      keyword: ['']
    });

    this.subscribeToFilterChanges();
  }

  private get keyword(): string {
    return this.searchForm ? this.searchForm.value.keyword : "";
  }

  private subscribeToFilterChanges() {
    this.searchForm.valueChanges
    .pipe(
      debounceTime(1000),
      distinctUntilChanged()
    )
    .subscribe(() => {
      this.fetchCandidatesWithActiveChat();
    });
  }

  public fetchCandidatesWithActiveChat() {
    this.error = null;
    this.loading = true;

    const request: FetchCandidatesWithActiveChatRequest = {
      pageNumber: this.pageNumber - 1,
      pageSize: 25,
      sortFields: [this.sortField],
      sortDirection: this.sortDirection,
      keyword: this.keyword
    }

    this.candidateService.fetchCandidatesWithActiveChat(request).subscribe(candidates => {
      this.candidatesWithActiveChats = candidates;
      this.loading = false;
    },
      error => {
      this.error = error;
      this.loading = false;
      });
  }

  public onCandidateSelected(candidate: Candidate) {
    this.currentCandidate = candidate;
    this.candidateSelection.emit(candidate);
  }

  public refresh(): void {
    this.fetchCandidatesWithActiveChat();
  }

  public toggleSort(column) {
    if (this.sortField === column) {
      this.sortDirection = this.sortDirection === 'ASC' ? 'DESC' : 'ASC';
    } else {
      this.sortField = column;
      this.sortDirection = 'ASC';
    }
    this.fetchCandidatesWithActiveChat();
  }

  public canAccessSalesforce(): boolean {
    return this.authService.canAccessSalesforce();
  }

  public hasTaskAssignments(candidate: Candidate): boolean {
    const active = candidate.taskAssignments?.filter(ta => ta.status === Status.active);
    return active?.length > 0;
  }

  public getTotalMonitoredTasks(candidate: Candidate) {
    if (this.monitoredTask != null) {
      return candidate.taskAssignments.filter(ta => ta.task.id === this.monitoredTask.id && ta.status === Status.active);
    } else {
      // DEFAULT tasks to monitor are required tasks
      // Only run through active tasks.
      let activeTaskAssignments = candidate.taskAssignments.filter(ta => ta.status === Status.active);
      return activeTaskAssignments.filter(ta => !ta.task.optional);
    }
  }

  public getCompletedMonitoredTasks(candidate: Candidate) {
    if (this.monitoredTask != null) {
      let monitoredTask = candidate.taskAssignments.filter(ta => ta.task.id === this.monitoredTask.id && ta.status === Status.active);
      return monitoredTask.filter(ta => (ta.completedDate != null || ta.abandonedDate != null));
    } else {
      // DEFAULT tasks to monitor are required tasks
      // Only run through active tasks.
      let activeTaskAssignments = candidate.taskAssignments.filter(ta => ta.status === Status.active);
      return activeTaskAssignments.filter(ta => (ta.completedDate != null || ta.abandonedDate != null) && !ta.task.optional);
    }
  }

  // TODO currently not working - also in show-candidates component
  public downloadCv(candidate) {
    const tab = window.open();
    this.candidateService.downloadCv(candidate.id).subscribe(
      result => {
        const fileUrl = URL.createObjectURL(result);
        tab.location.href = fileUrl;
      },
      error => {
        this.error = error;
      }
    )
  }

}
