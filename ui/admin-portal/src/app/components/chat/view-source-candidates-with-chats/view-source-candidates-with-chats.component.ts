import {Component, EventEmitter, OnInit, Output} from '@angular/core';
import {Candidate} from "../../../model/candidate";
import {SearchResults} from "../../../model/search-results";
import {AuthorizationService} from "../../../services/authorization.service";
import {FetchCandidatesWithActiveChatRequest, Status} from "../../../model/base";
import {Task} from "../../../model/task";
import {CandidateService} from "../../../services/candidate.service";

@Component({
  selector: 'app-view-source-candidates-with-chats',
  templateUrl: './view-source-candidates-with-chats.component.html',
  styleUrls: ['./view-source-candidates-with-chats.component.scss']
})
export class ViewSourceCandidatesWithChatsComponent implements OnInit {

  @Output() candidateSelection = new EventEmitter<Candidate>();

  error: any;
  loading: boolean;
  currentCandidate: Candidate;
  pageNumber: number = 1;
  monitoredTask: Task;
  candidatesWithActiveChats: SearchResults<Candidate>;
  sortField = 'id';
  sortDirection = 'DESC';

  constructor(
    private candidateService: CandidateService,
    private authService: AuthorizationService,
  ) {}

  ngOnInit(): void {
    this.fetchCandidatesWithActiveChat()
  }

  public fetchCandidatesWithActiveChat() {
    this.loading = true;
    // See SearchTasksComponent.search for how to manage pagination
    const request: FetchCandidatesWithActiveChatRequest = {
      pageNumber: this.pageNumber - 1,
      pageSize: 25,
      sortFields: [this.sortField],
      sortDirection: this.sortDirection
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

  // TODO add CV error code from show-candidates.component

}
