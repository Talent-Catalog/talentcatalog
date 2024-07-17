import {Component, OnInit} from "@angular/core";
import {User} from "../../../model/user";
import {AuthenticationService} from "../../../services/authentication.service";
import {ChatService} from "../../../services/chat.service";
import {JobChat} from "../../../model/chat";

@Component({
  selector: 'app-chat-console',
  templateUrl: './chat-console.component.html',
  styleUrls: ['./chat-console.component.scss']
})
export class ChatConsoleComponent implements OnInit {

  loggedInUser: User;
  error: any;
  sourceCandidateChats: JobChat[];

  ngOnInit(): void {
    this.loggedInUser = this.authenticationService.getLoggedInUser();
    this.getSourceCandidateChats();
  }

  constructor(
    private authenticationService: AuthenticationService,
    private chatService: ChatService
  ) { }

  private getSourceCandidateChats() {
    this.error = null;

    this.chatService.getSourceCandidateChats(this.loggedInUser.id).subscribe(
      (chats) => {this.sourceCandidateChats = chats},
      (error) => {this.error = error}
    );
  }

}
