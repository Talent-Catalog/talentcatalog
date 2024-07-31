import {Component, Input, OnInit} from '@angular/core';
import {ChatService} from "../../../services/chat.service";
import {MainSidePanelBase} from "../../util/split/MainSidePanelBase";
import {Candidate} from "../../../model/candidate";
import {JobChat} from "../../../model/chat";
import {Partner} from "../../../model/partner";
import {BehaviorSubject} from "rxjs";

@Component({
  selector: 'app-source-candidate-chats',
  templateUrl: './source-candidate-chats.component.html',
  styleUrls: ['./source-candidate-chats.component.scss']
})
export class SourceCandidateChatsComponent extends MainSidePanelBase implements OnInit {

  @Input() loggedInPartner: Partner;
  @Input() chatsRead$!: BehaviorSubject<boolean>;

  error: any;
  loading: boolean;
  selectedCandidate: Candidate;
  selectedCandidateChat: JobChat;
  chatHeader: string = "";

  constructor(private chatService: ChatService) { super(5); }

  ngOnInit(): void { }

  public onCandidateSelected(candidate: Candidate) {
    this.selectedCandidate = candidate;
    this.displayChat();
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
      "Chat with " + this.selectedCandidate.user.firstName + " " +
      this.selectedCandidate.user.lastName;
  }

}
