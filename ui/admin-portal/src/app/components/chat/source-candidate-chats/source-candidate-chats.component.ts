import {Component, Input, OnInit, SimpleChanges} from '@angular/core';
import {ChatService} from "../../../services/chat.service";
import {MainSidePanelBase} from "../../util/split/MainSidePanelBase";
import {Candidate} from "../../../model/candidate";
import {User} from "../../../model/user";
import {JobChat} from "../../../model/chat";

@Component({
  selector: 'app-source-candidate-chats',
  templateUrl: './source-candidate-chats.component.html',
  styleUrls: ['./source-candidate-chats.component.scss']
})
export class SourceCandidateChatsComponent extends MainSidePanelBase implements OnInit {

  @Input() sourceCandidateChats: JobChat[];

  error: any;
  selectedCandidate: Candidate;
  selectedCandidateChat: JobChat;
  loggedInUser: User;
  chatHeader: string = "";

  constructor(
    private chatService: ChatService
  ) {
    super(5);
  }

  ngOnInit(): void {
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

}
