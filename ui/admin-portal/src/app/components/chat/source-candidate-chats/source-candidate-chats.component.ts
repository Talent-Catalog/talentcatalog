import {Component, Input, OnInit, SimpleChanges} from '@angular/core';
import {ChatService} from "../../../services/chat.service";
import {MainSidePanelBase} from "../../util/split/MainSidePanelBase";
import {Candidate} from "../../../model/candidate";
import {JobChat} from "../../../model/chat";
import {Partner} from "../../../model/partner";
import {BehaviorSubject} from "rxjs";
import {CandidateService} from "../../../services/candidate.service";
import {SearchResults} from "../../../model/search-results";
import {ActivatedRoute} from "@angular/router";
import {
  FetchCandidatesWithActiveChatRequest,
} from "../../../model/base";

@Component({
  selector: 'app-source-candidate-chats',
  templateUrl: './source-candidate-chats.component.html',
  styleUrls: ['./source-candidate-chats.component.scss']
})
export class SourceCandidateChatsComponent extends MainSidePanelBase implements OnInit {

  @Input() loggedInPartner: Partner;
  @Input() chatsRead$!: BehaviorSubject<boolean>;

  error: string;
  loading: boolean;
  pageNumber: number;
  selectedCandidate: Candidate;
  selectedCandidateChat: JobChat;
  chatHeader: string = "";
  candidatesWithActiveChats: SearchResults<Candidate>;

  constructor(
    private chatService: ChatService,
    private candidateService: CandidateService,
    private route: ActivatedRoute
  ) {
    super(5);
  }

  ngOnInit(): void {
    this.fetchCandidatesWithActiveChat()

    // TODO check to see where this info gets included (do I need to do it deliberately?)
    //  See SearchTasksComponent.search for how to handle pagination
    // Start listening to route params after everything is loaded.
    this.route.queryParamMap.subscribe(
      params => {
        this.pageNumber = +params.get('pageNumber');
        if (!this.pageNumber) {
          this.pageNumber = 1;
        }
      }
    );
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.selectedCandidate) {
      this.displayChat();
    }
  }

  public onCandidateSelected(candidate: Candidate) {
    this.selectedCandidate = candidate;
  }

  private displayChat() {
    this.fetchCandidateChat();
    this.computeChatHeader();
  }

  private fetchCandidateChat() {
    this.error = null;

    this.chatService.getCandidateProspectChat(this.selectedCandidate.id).subscribe(
      (chat) => {this.selectedCandidateChat = chat},
      (error) => {this.error = error}
    )
  }

  private computeChatHeader() {
    this.chatHeader =
      "Chat with " + this.selectedCandidate.user.firstName +
        this.selectedCandidate.user.lastName + " (" + this.selectedCandidate.candidateNumber + ")";
  }

  public fetchCandidatesWithActiveChat() {
    // See SearchTasksComponent.search for how to manage pagination
    let request: FetchCandidatesWithActiveChatRequest = {
      pageNumber: this.pageNumber - 1,
    }

    this.candidateService.fetchCandidatesWithActiveChat(request).subscribe({
      next: candidates => this.candidatesWithActiveChats = candidates,
      error: error => this.error = error
    })
  }

}
