import {
  Component,
  ElementRef,
  EventEmitter, Input,
  OnInit,
  Output,
  ViewChild
} from '@angular/core';
import {Candidate} from "../../../model/candidate";
import {SearchResults} from "../../../model/search-results";
import {AuthorizationService} from "../../../services/authorization.service";
import {FetchCandidatesWithChatRequest, Status} from "../../../model/base";
import {Task} from "../../../model/task";
import {CandidateService} from "../../../services/candidate.service";
import {FormBuilder, FormGroup} from "@angular/forms";
import {debounceTime, distinctUntilChanged} from "rxjs/operators";
import {ChatService} from "../../../services/chat.service";
import {BehaviorSubject, forkJoin, Observable, Subscription} from "rxjs";
import {JobChat, JobChatUserInfo} from "../../../model/chat";

@Component({
  selector: 'app-view-source-candidates-with-chats',
  templateUrl: './view-source-candidates-with-chats.component.html',
  styleUrls: ['./view-source-candidates-with-chats.component.scss']
})
export class ViewSourceCandidatesWithChatsComponent implements OnInit {

  @Output() candidateSelection = new EventEmitter<Candidate>();

  /**
   * This is passed in from a higher level component which tracks whether the overall read status
   * of all the chats that it manages.
   * <p/>
   * This component can call next on this subject if it knows that some of the chats it manages
   * are unread. The fact that it is a BehaviorSubject means that you can query the current status
   * of the higher level component.
   */
  @Input() chatsRead$: BehaviorSubject<boolean>;

  @ViewChild("searchFilter") searchFilter: ElementRef;

  error: any;
  loading: boolean;
  currentCandidate: Candidate;
  pageNumber: number = 1;
  pageSize: number = 25;
  monitoredTask: Task;
  sortField = 'id';
  sortDirection = 'DESC';
  searchForm: FormGroup;
  candidates: Candidate[];
  results: SearchResults<Candidate>;
  chatsReadProcessingComplete: boolean = false;

  // Form info
  unreadOnlyTip = "Show only candidates whose chats have unread posts. " +
    "This view doesn't auto-update: click 'Refresh' to see current results.";
  unreadOnlyLabel = "Show unread only";

  /**
   * All chats associated with all candidates. Used to construct overall chat read notifier.
   */
  protected allChats: JobChat[] = [];

  /**
   * Map of candidate id to chat
   */
  protected candidateChats: Map<number, JobChat> = new Map<number, JobChat>();

  /**
   * Subscription to all visible candidate chats
   * @private
   */
  protected subscription: Subscription;

  constructor(
    private candidateService: CandidateService,
    private authService: AuthorizationService,
    private fb: FormBuilder,
    private chatService: ChatService
  ) {}

  ngOnInit(): void {
    this.searchForm = this.fb.group({
      keyword: [''],
      unreadOnly: [false]
    });

    this.subscribeToFilterChanges();

    this.fetchCandidatesWithActiveChat(true)
  }

  private get keyword(): string {
    return this.searchForm ? this.searchForm.value.keyword : "";
  }

  protected get unreadOnly(): boolean {
    return this.searchForm? this.searchForm.value.unreadOnly : false;
  }

  private subscribeToFilterChanges() {
    this.searchForm.valueChanges
    .pipe(
      debounceTime(1000),
      distinctUntilChanged()
    )
    .subscribe(() => {
      this.fetchCandidatesWithActiveChat(true);
    });
  }

  public fetchCandidatesWithActiveChat(refresh: boolean) {
    this.loading = true;

    const request: FetchCandidatesWithChatRequest = {
      pageNumber: this.pageNumber - 1,
      pageSize: this.pageSize,
      sortFields: [this.sortField],
      sortDirection: this.sortDirection,
      keyword: this.keyword,
      unreadOnly: this.unreadOnly
    }

    if (refresh) {
      this.candidateService.fetchCandidatesWithChat(request).subscribe(
        candidates => {
          this.processSearchResults(candidates);
        },
        error => {
          this.error = error;
        })
    }

    this.candidateService.checkUnreadChats().subscribe(
      info => {
        this.processChatsReadStatus(info);
        this.loading = false;
      },
        error => {
          this.error = error;
          this.loading = false;
      })
  }

  protected processSearchResults(results: SearchResults<Candidate>) {
      this.results = results;
      this.candidates = results.content;

    //Following the search, filter loses focus, so focus back on it again
    setTimeout(()=>{this.searchFilter.nativeElement.focus()},0);

    this.fetchChats();
  }

  protected processChatsReadStatus(info: JobChatUserInfo) {
    if (this.chatsRead$) {
      //There is a high level component monitoring the read status of all chats.
      //Notify that component but sending the new read status on the Subject. Read is true
      //if there are no unread chats, otherwise false.
      this.chatsRead$.next(info.numberUnreadChats === 0);
    }
  }

  public onCandidateSelected(candidate: Candidate) {
    this.currentCandidate = candidate;
    this.candidateSelection.emit(candidate);
  }

  public refresh(): void {
    this.fetchCandidatesWithActiveChat(true);
  }

  public toggleSort(column) {
    if (this.sortField === column) {
      this.sortDirection = this.sortDirection === 'ASC' ? 'DESC' : 'ASC';
    } else {
      this.sortField = column;
      this.sortDirection = 'ASC';
    }
    this.fetchCandidatesWithActiveChat(true);
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

  public getCandidateChat(candidate: Candidate): JobChat[] {
    return candidate ? [this.candidateChats.get(candidate.id)] : null;
  }

  /**
   * Stores each candidate chat in this.candidateChats, indexed by the candidate id.
   * This can be accessed by {@link getCandidateChat}.
   * <p/>
   * It also puts chats for all candidates into this.allChats.
   * @param chatByCandidate chat for each candidate
   */
  protected processCandidateChats(chatByCandidate: JobChat[]) {
    //Recalculate all chats for new candidates
    this.allChats = [];
    for (let i = 0; i < this.candidates.length; i++) {
      const candidate = this.candidates[i];
      let chat = chatByCandidate[i];
      this.candidateChats.set(candidate.id, chat);
      this.allChats.push(chat);
    }

    //Resubscribe to composite status of all visible chats
    this.subscribeToAllVisibleChats();
  }

  private subscribeToAllVisibleChats() {
    this.unsubscribe();
    //Construct a single observable for all visible chat's read statuses, and subscribe to it
    const chatReadStatus$ = this.chatService.combineChatReadStatuses(this.allChats);
    console.log("Subscribed to chats " + this.allChats.map( chat => chat.id).join(','));
    this.subscription = chatReadStatus$.subscribe(
      {
        next: chatsRead => this.processVisibleChatsReadUpdate(chatsRead),
        error: err => this.error = err
      }
    )
  }

  private processVisibleChatsReadUpdate(chatsRead: boolean) {
    if (this.chatsRead$) {
      console.log("Visible chats read update: " + chatsRead);
      if (this.chatsRead$.value && !chatsRead) {
        //Status from server says all chats read, but there are unread visible chats.
        //Mark all chats read false
        this.chatsRead$.next(false);
      } else if (!this.chatsRead$.value && chatsRead) {
        //All chats are showing not read, but all chats for visible candidates are now read.
        //Fetch from server again to see if there are still some non-visible candidates with unread chats.
        //Don't redo the search - we just want to see if there are any unread chats left in the full
        //search results.
        this.fetchCandidatesWithActiveChat(false);
      }
    }
  }

  private unsubscribe() {
    if (this.subscription) {
      console.log("Unsubscribed from previous visible chats")
      this.subscription.unsubscribe();
      this.subscription = null;
    }
  }

  private fetchChats() {

    let candidateChats$: Observable<JobChat>[] = [];

    for (const candidate of this.candidates) {
      let chat$ = this.chatService.getCandidateProspectChat(candidate.id)

      candidateChats$.push(chat$);
    }

    this.error = null;
    forkJoin(candidateChats$).subscribe(chatByCandidate => {
      this.processCandidateChats(chatByCandidate);
      this.chatsReadProcessingComplete = true;
    },
    error => {
      this.error = error
    })
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

}
