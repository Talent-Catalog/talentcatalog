import {Component, Input, OnInit, SimpleChanges} from '@angular/core';
import {ChatService} from "../../../services/chat.service";
import {MainSidePanelBase} from "../../util/split/MainSidePanelBase";
import {Candidate} from "../../../model/candidate";
import {JobChat} from "../../../model/chat";
import {Partner} from "../../../model/partner";
import {BehaviorSubject} from "rxjs";
import {SearchCandidateRequest} from "../../../model/search-candidate-request";
import {CandidateService} from "../../../services/candidate.service";
import {SearchResults} from "../../../model/search-results";

@Component({
  selector: 'app-source-candidate-chats',
  templateUrl: './source-candidate-chats.component.html',
  styleUrls: ['./source-candidate-chats.component.scss']
})
export class SourceCandidateChatsComponent extends MainSidePanelBase implements OnInit {

  @Input() loggedInPartner: Partner;
  @Input() chatsRead$!: BehaviorSubject<boolean>;

  error: any;
  selectedCandidate: Candidate;
  selectedCandidateChat: JobChat;
  chatHeader: string = "";
  candidatesWithActiveChats: SearchResults<Candidate>;

  constructor(
    private chatService: ChatService,
    private candidateService: CandidateService
  ) {
    super(5);
  }

  ngOnInit(): void {
    this.fetchCandidatesWithActiveChat()
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

  private fetchCandidatesWithActiveChat() {
    let candidateReq: SearchCandidateRequest = {
      partnerIds: [this.loggedInPartner.id],
    }

    this.candidateService.fetchCandidatesWithActiveChat(candidateReq).subscribe({
      next: candidates => this.candidatesWithActiveChats = candidates,
      error: error => this.error = error
    })
  }

}
